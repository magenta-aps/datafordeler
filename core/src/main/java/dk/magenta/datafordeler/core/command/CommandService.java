package dk.magenta.datafordeler.core.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import dk.magenta.datafordeler.core.PluginManager;
import dk.magenta.datafordeler.core.database.ConfigurationSessionManager;
import dk.magenta.datafordeler.core.exception.*;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.role.CommandRole;
import dk.magenta.datafordeler.core.role.SystemRole;
import dk.magenta.datafordeler.core.role.SystemRoleType;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Webservice that receives commands on POST requests, checks job status on GET
 * requests, and cancels jobs on DELETE requests.
 * The basic idea is that a POST request will parsed, and if successful, a row will be put in the Command table.
 * GET requests will look at the table and return the job status
 * DELETE requests will find the associated job in the table and attempt to cancel it
 */
@RequestMapping("/command")
@Controller
public class CommandService {

    private final Logger log = LogManager.getLogger(CommandService.class.getCanonicalName());

    @Autowired
    private ConfigurationSessionManager sessionManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommandWatcher commandWatcher;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private DafoUserManager dafoUserManager;

    /**
     * Check that the user in the loggerHelper has access to the required role, and if not, log the attempt and throw an exception
     *
     * @param loggerHelper LoggerHelper object containing user data
     * @param requiredRole SystemRole to check for
     * @throws AccessDeniedException
     * @throws AccessRequiredException
     */
    protected void checkAndLogAccess(LoggerHelper loggerHelper, SystemRole requiredRole)
            throws AccessDeniedException, AccessRequiredException {
        System.out.println("checkAndLogAccess");
        try {
            this.checkAccess(loggerHelper.getUser(), requiredRole);
        } catch (AccessDeniedException | AccessRequiredException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw (e);
        }
    }


    /**
     * Checks that the user has access to the service
     *
     * @param dafoUserDetails DafoUserDetails object representing the user provided from a SAML token.
     * @throws AccessDeniedException Implementing this method as a noop will make the service publicly accessible.
     */
    protected void checkAccess(DafoUserDetails dafoUserDetails, SystemRole requiredRole)
            throws AccessDeniedException, AccessRequiredException {
        System.out.println("checkAccess");
        dafoUserDetails.checkHasSystemRole(requiredRole);
    }

    /**
     * Look through available CommandRoles, locating one that matches the queries access type, command name, and command data
     *
     * @param requiredAccess SystemRoleType to match, e.g. SystemRoleType.ExecuteCommandRole for command execution
     * @param commandName    Command name. Look for roles with this command name
     * @param commandData    The matched CommandRole must not contain any key-value pair that is not in the CommandData, ie. sent with the command body,
     * @return
     */
    private CommandRole findMatchingRole(SystemRoleType requiredAccess, String commandName, CommandData commandData) {
        for (Plugin plugin : pluginManager.getPlugins()) {
            List<SystemRole> pluginRoles = plugin.getRolesDefinition().getRoles();
            if (pluginRoles != null) {
                for (SystemRole role : pluginRoles) {
                    // To gain access, the user must have the defined role that matches all of the following:
                    // * is an ExecuteCommandRole
                    if (role instanceof CommandRole) {
                        CommandRole commandRole = (CommandRole) role;
                        if (
                            // * has a type that matches the type of access requested
                                commandRole.getType() == requiredAccess &&
                                        // * has a target that matches the command requested (e.g. "pull")
                                        commandRole.getCommandName().equalsIgnoreCase(commandName) &&
                                        // * has a details map that validates the command body
                                        (commandData == null && (commandRole.getDetails() == null || commandRole.getDetails().isEmpty())) ||
                                        (commandData != null && commandData.containsAll(commandRole.getDetails()))
                        ) {
                            return commandRole;
                        }
                    }
                }
            }
        }
        // If no such role is found, we must block all access
        return null;
    }

    private void checkRole(Command command, CommandHandler handler, SystemRoleType roleType, LoggerHelper loggerHelper) throws DataStreamException, InvalidClientInputException, AccessDeniedException, AccessRequiredException {
        // Check that the CommandHandler can handle the command, and that there exists a SystemRole granting access to the command
        CommandData commandData = handler.getCommandData(command.getCommandBody());
        SystemRole requiredRole = this.findMatchingRole(roleType, command.getCommandName(), commandData);
        if (requiredRole == null) {
            loggerHelper.info("No Command Role exists for [SystemRoleType:" + roleType.name() + ", Command: " + command.getCommandName() + ", CommandData: " + commandData + "]");
            throw new AccessDeniedException("No Command Role exists for command '" + command.getCommandName() + "' with data '" + commandData + "'");
        }
        // Check that the user has this SystemRole
        this.checkAndLogAccess(loggerHelper, requiredRole);
    }


    /**
     * GET listener, invoked as GET /command/[id], where [id] is a numeric identifier previously returned from a POST request
     * Return the data pertaining to a job, including received time, issuer, status (queued, running, successful, failed, cancelled)
     *
     * @param request
     * @param response
     * @param commandId Command identifier; this is returned for a POST request, and can be used here
     * @throws IOException
     * @throws HttpNotFoundException
     * @throws InvalidClientInputException
     * @throws InvalidTokenException
     * @throws AccessRequiredException
     * @throws AccessDeniedException
     * @throws DataStreamException
     */
    @RequestMapping(method = RequestMethod.GET, path = "{id}")
    public void doGet(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") Long commandId)
            throws IOException, HttpNotFoundException, InvalidClientInputException, InvalidTokenException, AccessRequiredException, AccessDeniedException, DataStreamException, InvalidCertificateException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request, true);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info("GET request received on address " + request.getServletPath());

        if (commandId >= 0) {
            loggerHelper.info("Request for status on job id " + commandId);
            Command command = this.getCommand(commandId);
            if (command == null) {
                throw new HttpNotFoundException("Job id " + commandId + " not found");
            }
            CommandHandler handler = commandWatcher.getHandler(command.getCommandName());
            if (handler == null) {
                loggerHelper.info("No handler found for command " + command.getCommandName() + " (job id " + commandId + ")");
                throw new InvalidClientInputException("No handler found for command");
            } else {
                this.checkRole(command, handler, SystemRoleType.ReadCommandRole, loggerHelper);
                String output = objectMapper.writeValueAsString(handler.getCommandStatus(command));
                loggerHelper.info("Status on job id " + commandId + " is " + output);
                response.getWriter().write(output);
            }
        } else {
            loggerHelper.info("Request for status on job id " + commandId + ", but no such job exists");
            throw new HttpNotFoundException("Job id " + commandId + " not found");
        }
    }

    @RequestMapping(method = RequestMethod.GET, path = "pull/summary/{plugin}/{state}")
    public void doGetSummary(HttpServletRequest request, HttpServletResponse response, @PathVariable("plugin") String pluginName, @PathVariable("state") String state)
            throws IOException, HttpNotFoundException, InvalidClientInputException, InvalidTokenException, AccessRequiredException, AccessDeniedException, DataStreamException, InvalidCertificateException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request, true);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info("GET request received on address " + request.getServletPath());

        pluginName = pluginName.toLowerCase();
        if (!pluginName.equals("all")) {
            Plugin plugin = pluginManager.getPluginByName(pluginName);
            if (plugin == null) {
                throw new InvalidClientInputException("Plugin " + pluginName + " not found");
            }
        }
        state = state.toLowerCase();
        List<String> validStates = Arrays.asList("latest", "running");
        if (!validStates.contains(state)) {
            throw new InvalidClientInputException("Invalid state '" + state + "', valid choices are: " + validStates);
        }

        List<Command> commands = this.getPullCommandSummary(pluginName, state);
        ArrayNode list = objectMapper.createArrayNode();
        for (Command command : commands) {
            CommandHandler handler = commandWatcher.getHandler(command.getCommandName());
            if (handler == null) {
                loggerHelper.info("No handler found for command " + command.getCommandName() + " (job id " + command.getId() + ")");
                throw new InvalidClientInputException("No handler found for command");
            } else {
                this.checkRole(command, handler, SystemRoleType.ReadCommandRole, loggerHelper);
                JsonNode output = handler.getCommandStatus(command);
                list.add(output);
                loggerHelper.info("Status on job id " + command.getId() + " is " + output);
            }
        }
        response.getWriter().write(objectMapper.writeValueAsString(list));
    }


    /**
     * POST listener, invoked as POST /command/[commandname], where [commandname] is a known command.
     * Currently, only the "pull" command exists, invoked by /command/pull
     * The POST body contains parameters to the command handler, which is free to interpret it how it wants
     * The PullCommandHandler, currently the only one present, reads the body as JSON
     * On a successfully parsed request, the resulting Command object is put in the database, from where it will be picked up by the CommandWatcher
     *
     * @param request
     * @param response
     * @param commandName A string denoting the name of a command, e.g. "pull"
     * @throws IOException
     * @throws InvalidClientInputException
     * @throws InvalidTokenException
     * @throws AccessDeniedException
     * @throws AccessRequiredException
     * @throws DataStreamException
     */
    @RequestMapping(method = RequestMethod.POST, path = "/{command}")
    public void doPost(HttpServletRequest request, HttpServletResponse response, @PathVariable("command") String commandName)
            throws IOException, InvalidClientInputException, InvalidTokenException, AccessDeniedException, AccessRequiredException, DataStreamException, InvalidCertificateException {
        System.out.println("POST TO COMMAND SERVICE");
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request, true);
        System.out.println("user: "+user);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info("POST request received on address " + request.getServletPath());
        loggerHelper.info("Request for command '" + commandName + "'");
        Command command;
        try {
            // Extract Command object from request
            command = Command.fromRequest(request, user, commandName);
        } catch (IOException e) {
            loggerHelper.error("Error reading command body", e);
            throw new InvalidClientInputException("Cannot read command body");
        }
        // Ensure that a CommandHandler exists for this command
        CommandHandler handler = commandWatcher.getHandler(command.getCommandName());
        if (handler == null) {
            throw new InvalidClientInputException("No handler found for command '" + commandName + "'");
        } else {
            this.checkRole(command, handler, SystemRoleType.ExecuteCommandRole, loggerHelper);
            // Put the command in the Database with the "queued" status. The CommandWatcher will pick it up
            command.setStatus(Command.Status.QUEUED);
            this.saveCommand(command);
            String output = this.objectMapper.writeValueAsString(command);
            loggerHelper.info("Command queued: " + output);
            response.getWriter().write(output);
        }
        loggerHelper.info("Request complete");
    }


    /**
     * DELETE listener, invoked as DELETE /command/[id], where [id] is a numeric identifier previously returned from a POST request
     * If a command is found by the given id, a cancel will be attempted and the job status returned (same output as with GET)
     *
     * @param request
     * @param response
     * @param commandId Command identifier; this is returned for a POST request, and can be used here
     * @throws IOException
     * @throws InvalidClientInputException
     * @throws HttpNotFoundException
     * @throws InvalidTokenException
     * @throws DataStreamException
     * @throws AccessDeniedException
     * @throws AccessRequiredException
     */
    @RequestMapping(method = RequestMethod.DELETE, path = "{id}")
    public void doDelete(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") Long commandId)
            throws IOException, InvalidClientInputException, HttpNotFoundException, InvalidTokenException, DataStreamException, AccessDeniedException, AccessRequiredException, InvalidCertificateException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request, true);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info("DELETE request received on address " + request.getServletPath());
        if (commandId >= 0) {
            loggerHelper.info("Request for cancelling of job id '" + commandId + "'");
            Command command = this.getCommand(commandId);
            if (command == null) {
                throw new HttpNotFoundException("Command id " + commandId + " not found");
            }
            CommandHandler handler = commandWatcher.getHandler(command.getCommandName());
            if (handler == null) {
                throw new InvalidClientInputException(
                        "No handler found for command '" + command.getCommandName() + "'"
                );
            } else {
                this.checkRole(command, handler, SystemRoleType.StopCommandRole, loggerHelper);
                // Cancel the command
                commandWatcher.cancelCommand(command);
                String output = this.objectMapper.writeValueAsString(command);
                loggerHelper.info("Status on job id " + commandId + " is " + output);
                response.getWriter().write(output);
            }
        } else {
            loggerHelper.info("Request for cancelling job id " + commandId + ", but no such job exists");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Finds a command object in the database, based on an id
     *
     * @param commandId
     * @return
     */
    private Command getCommand(long commandId) {
        Session session = sessionManager.getSessionFactory().openSession();
        Command command = null;
        try {
            Query<Command> query = session.createQuery("select c from dk.magenta.datafordeler.core.command.Command c where c.id = :id", Command.class);
            query.setParameter("id", commandId);
            command = query.getSingleResult();
        } catch (NoResultException e) {
        }
        session.close();
        return command;
    }

    private List<Command> getPullCommandSummary(String plugin, String state) {
        Session session = sessionManager.getSessionFactory().openSession();
        List<Command> commands = new ArrayList<>();
        String entityKey = "c";
        String whereJoin = " and ";

        StringJoiner where = new StringJoiner(whereJoin);
        HashMap<String, Object> parameters = new HashMap<>();

        where.add(entityKey + ".commandName = :commandName");
        parameters.put("commandName", "pull");

        if (state.equals("running")) {
            where.add("(" + entityKey + ".status = :queued OR " + entityKey + ".status = :processing)");
            parameters.put("queued", Command.Status.QUEUED);
            parameters.put("processing", Command.Status.PROCESSING);
        }

        List<String> plugins;
        if (plugin.equals("all")) {
            plugins = new ArrayList<>();
            for (Plugin p : pluginManager.getPlugins()) {
                plugins.add(p.getName());
            }
        } else {
            plugins = Collections.singletonList(plugin);
        }


        for (String p : plugins) {
            StringJoiner thisWhere = new StringJoiner(whereJoin);
            thisWhere.merge(where);
            HashMap<String, Object> thisParameters = new HashMap<>(parameters);

            thisWhere.add(entityKey + ".commandBody LIKE :pluginName");
            thisParameters.put("pluginName", "%\"plugin\"!:\"" + p + "\"%");

            try {
                Query<Command> query = session.createQuery(
                        "select c from dk.magenta.datafordeler.core.command.Command " + entityKey + " " +
                                "where " + thisWhere + " " +
                                "escape '!' " +
                                "order by " + entityKey + ".handled desc ",
                        Command.class
                );
                for (String parameterName : thisParameters.keySet()) {
                    query.setParameter(parameterName, thisParameters.get(parameterName));
                }
                query.setMaxResults(1);


                List<Command> list = query.getResultList();
                if (!list.isEmpty()) {
                    commands.add(list.get(0));
                }

            } catch (NoResultException e) {
            }
        }
        session.close();
        return commands;
    }

    /**
     * Saves a Command object to the database
     *
     * @param command
     */
    public synchronized void saveCommand(Command command) {
        Session session = this.sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        session.saveOrUpdate(command);
        transaction.commit();
        session.close();
    }
}

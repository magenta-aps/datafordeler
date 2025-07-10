package dk.magenta.datafordeler.cvr.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.command.Command;
import dk.magenta.datafordeler.core.command.CommandData;
import dk.magenta.datafordeler.core.command.CommandHandler;
import dk.magenta.datafordeler.core.command.Worker;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.util.FinalWrapper;
import dk.magenta.datafordeler.cvr.entitymanager.CompanyEntityManager;
import dk.magenta.datafordeler.cvr.entitymanager.CompanyUnitEntityManager;
import dk.magenta.datafordeler.cvr.entitymanager.CvrEntityManager;
import dk.magenta.datafordeler.cvr.entitymanager.ParticipantEntityManager;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.query.CompanyUnitRecordQuery;
import dk.magenta.datafordeler.cvr.query.ParticipantRecordQuery;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import dk.magenta.datafordeler.cvr.records.CompanyUnitRecord;
import dk.magenta.datafordeler.cvr.records.ParticipantRecord;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Component
public class CloseCommandHandler extends CommandHandler {

    @Autowired
    CompanyEntityManager companyEntityManager;
    @Autowired
    CompanyUnitEntityManager companyUnitEntityManager;
    @Autowired
    ParticipantEntityManager participantEntityManager;

    @Autowired
    private SessionManager sessionManager;
    @Autowired
    private ObjectMapper objectMapper;

    public static class CloseCommandData extends CommandData {

        public CloseCommandData() {
        }

        @JsonProperty
        public String type;

        @JsonProperty(required = true)
        public List<String> ids;

        @Override
        public boolean containsAll(Map<String, Object> data) {
            for (String key : data.keySet()) {
                if (key.equals("ids") && this.ids != null) {
                    // Ok for now
                } else {
                    // This must not happen. It means there is an important difference between the incoming map and this object
                    return false;
                }
            }
            return true;
        }

        @Override
        protected Map<String, Object> contents() {
            return Map.of("ids", this.ids, "type", this.type);
        }

    }

    @Override
    protected String getHandledCommand() {
        return "close_bitemp";
    }

    public boolean accept(Command command) {
        return super.accept(command);
    }

    @Override
    public Worker doHandleCommand(Command command) throws DataFordelerException {
        if (this.accept(command)) {
            this.getLog().info("Handling command '" + command.getCommandName() + "'");
            return new Worker() {
                @Override
                public void run() {
                    CloseCommandData commandData;
                    try {
                        commandData = CloseCommandHandler.this.getCommandData(command.getCommandBody());
                    } catch (InvalidClientInputException e) {
                        throw new RuntimeException(e);
                    }

                    try (Session session = sessionManager.getSessionFactory().openSession()) {
                        Transaction transaction = session.beginTransaction();
                        try {
                            final FinalWrapper<Integer> counter = new FinalWrapper<>(0);
                            switch (commandData.type) {
                                case "company":
                                    CompanyRecordQuery companyRecordQuery = new CompanyRecordQuery();
                                    if (!commandData.ids.contains("all")) {
                                        companyRecordQuery.addParameter(CompanyRecordQuery.CVRNUMMER, commandData.ids);
                                    } else {
                                        companyRecordQuery.setPageSize(10000000);
                                    }
                                    Stream<CompanyRecord> companies = QueryManager.getAllEntitiesAsStream(session, companyRecordQuery, CompanyRecord.class);
                                    companies.forEach(companyRecord -> {
                                        System.out.println(counter.getInner()+" "+companyRecord.getCvrNumber());
                                        CloseCommandHandler.this.companyEntityManager.cleanupBitemporalSets(session, Collections.singleton(companyRecord));
                                        CloseCommandHandler.this.companyEntityManager.closeAllEligibleRegistrations(session, Collections.singleton(companyRecord));
                                        session.flush();
                                        counter.setInner(counter.getInner() + 1);
                                    });
                                    break;
                                case "unit":
                                    CompanyUnitRecordQuery companyUnitRecordQuery = new CompanyUnitRecordQuery();
                                    if (!commandData.ids.contains("all")) {
                                        companyUnitRecordQuery.addParameter(CompanyUnitRecordQuery.P_NUMBER, commandData.ids);
                                    } else {
                                        companyUnitRecordQuery.setPageSize(10000000);
                                    }
                                    Stream<CompanyUnitRecord> units = QueryManager.getAllEntitiesAsStream(session, companyUnitRecordQuery, CompanyUnitRecord.class);
                                    units.forEach(companyUnitRecord -> {
                                        System.out.println(counter.getInner()+" "+companyUnitRecord.getUnitNumber());
                                        CloseCommandHandler.this.companyUnitEntityManager.cleanupBitemporalSets(session, Collections.singleton(companyUnitRecord));
                                        CloseCommandHandler.this.companyUnitEntityManager.closeAllEligibleRegistrations(session, Collections.singleton(companyUnitRecord));
                                        session.flush();
                                        counter.setInner(counter.getInner() + 1);
                                    });
                                    break;
                                case "participant":
                                    ParticipantRecordQuery participantRecordQuery = new ParticipantRecordQuery();
                                    if (!commandData.ids.contains("all")) {
                                        participantRecordQuery.addParameter(ParticipantRecordQuery.UNITNUMBER, commandData.ids);
                                    } else {
                                        participantRecordQuery.setPageSize(10000000);
                                    }
                                    Stream<ParticipantRecord> participants = QueryManager.getAllEntitiesAsStream(session, participantRecordQuery, ParticipantRecord.class);
                                    participants.forEach(participantRecord -> {
                                        System.out.println(counter.getInner()+" "+participantRecord.getBusinessKey());
                                        CloseCommandHandler.this.participantEntityManager.cleanupBitemporalSets(session, Collections.singleton(participantRecord));
                                        CloseCommandHandler.this.participantEntityManager.closeAllEligibleRegistrations(session, Collections.singleton(participantRecord));
                                        session.flush();
                                        counter.setInner(counter.getInner() + 1);
                                    });
                                    break;
                            }
                            transaction.commit();
                        } catch (Exception e) {
                            e.printStackTrace();
                            transaction.rollback();
                        }
                        System.out.println("Command completed");
                    }
                }
            };

        }
        return null;
    }

    public CloseCommandData getCommandData(String commandBody) throws InvalidClientInputException {
        try {
            CloseCommandData commandData = this.objectMapper.readValue(commandBody, CloseCommandData.class);
            this.getLog().info("Command data parsed");
            return commandData;
        } catch (IOException e) {
            InvalidClientInputException ex = new InvalidClientInputException("Unable to parse command data '" + commandBody + "'");
            this.getLog().error(ex);
            throw ex;
        }
    }

    public ObjectNode getCommandStatus(Command command) {
        return objectMapper.valueToTree(command);
    }
}

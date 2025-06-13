package dk.magenta.datafordeler.statistik;

import dk.magenta.datafordeler.core.configuration.ConfigurationManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.ConfigurationException;
import dk.magenta.datafordeler.core.plugin.AreaRestrictionDefinition;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import dk.magenta.datafordeler.core.plugin.RolesDefinition;
import dk.magenta.datafordeler.statistik.reportExecution.AssignmentCleaner;
import jakarta.annotation.PostConstruct;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StatistikPlugin extends Plugin {

    StatistikRolesDefinition rolesDefinition = new StatistikRolesDefinition();

    @Override
    public String getName() {
        return "statistik";
    }

    @Override
    public RegisterManager getRegisterManager() {
        return null;
    }

    @Override
    public ConfigurationManager getConfigurationManager() {
        return null;
    }

    @Override
    public RolesDefinition getRolesDefinition() {
        return this.rolesDefinition;
    }

    @Override
    public AreaRestrictionDefinition getAreaRestrictionDefinition() {
        return null;
    }

    @Autowired
    public SessionManager sessionManager;

    @PostConstruct
    public void init() throws ConfigurationException {
        AssignmentCleaner.setup(sessionManager.getSessionFactory(), 7, "0 0 15 * *");
    }
}

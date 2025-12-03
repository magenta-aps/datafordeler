package dk.magenta.datafordeler.core.migration;

import dk.magenta.datafordeler.core.Engine;
import jakarta.annotation.PostConstruct;
import org.hibernate.Session;
import org.reflections.Reflections;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import dk.magenta.datafordeler.core.database.ConfigurationSessionManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.util.StringJoiner;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;
import java.util.List;

@Component
public class Migration {

    @Autowired
    protected SessionManager sessionManager;

    @Autowired
    protected ConfigurationSessionManager configurationSessionManager;

    @Autowired
    private Engine engine;

    @PostConstruct
    public void run() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        if (this.engine.isMigrateEnabled()) {
            this.runForPackage("dk.magenta.datafordeler.cpr");
            this.runForPackage("dk.magenta.datafordeler.cvr");
            this.runForPackage("dk.magenta.datafordeler.geo");
            this.runForPackage("dk.magenta.datafordeler.ger");
            this.runForPackage("dk.magenta.datafordeler.core");
        }
    }

    private void runForPackage(String pack) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Reflections reflections = new Reflections(pack);
        Set<Class<? extends MigrateModel>> classes = reflections.getSubTypesOf(MigrateModel.class);
        HashSet<Class<? extends MigrateModel>> handledClasses;

        handledClasses = new HashSet<>(classes);
        handledClasses.retainAll(this.sessionManager.managedClasses());
        try (Session session = this.sessionManager.getSessionFactory().openSession()) {
            for (Class<? extends MigrateModel> clazz : handledClasses) {
                runForClass(session, clazz);
            }
        }

        handledClasses = new HashSet<>(classes);
        handledClasses.retainAll(this.configurationSessionManager.managedClasses());
        try (Session session = this.configurationSessionManager.getSessionFactory().openSession()) {
            for (Class<? extends MigrateModel> clazz : handledClasses) {
                runForClass(session, clazz);
            }
        }
    }

    protected <T extends MigrateModel> void runForClass(Session session, Class<T> model) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        System.out.println("Running migration for " + model.getCanonicalName());
        StringJoiner s = new StringJoiner(" or ");
        Method updateFields = model.getMethod("updateFields");
        List<String> fields = (List<String>) updateFields.invoke(null);
        for (String field : fields) {
            s.add(field + " is not null");
        }
        Method updateTimestamp = model.getMethod("updateTimestamp");
        String hql = "from " + model.getCanonicalName() + " where " + s;
        Transaction transaction = session.beginTransaction();
        try {
            for (int offset=0; offset<100000000; offset+=1000) {
                Query<T> query = session.createQuery(hql, model);
                query.setFirstResult(offset);
                query.setMaxResults(1000);
                List<T> list = query.getResultList();
                if (list.isEmpty()) {
                    break;
                }
                for (T t : list) {
                    updateTimestamp.invoke(t);
                }
                session.flush();
                session.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
            transaction.rollback();
        }
        transaction.commit();
    }
}

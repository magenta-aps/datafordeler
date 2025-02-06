package dk.magenta.datafordeler.core.io.storagetest;

import dk.magenta.datafordeler.core.database.Entity;
import jakarta.persistence.Table;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.UUID;

@jakarta.persistence.Entity
@Table(name="test_entity")
public class TestEntity extends Entity<TestEntity, TestRegistration> {

    public TestEntity() {}

    public TestEntity(UUID uuid, String domain) {
        super(uuid, domain);
    }

    @Override
    protected TestRegistration createEmptyRegistration() {
        return new TestRegistration();
    }

    public static TestEntity get(Session session, long id) {
        return session.get(TestEntity.class, id);
    }

    public static TestEntity get(Session session, UUID uuid, String domain) {
        Query<TestEntity> query = session.createQuery("select e from TestEntity e join e.identification i where i.id = :id and i.domain = :domain", TestEntity.class);
        query.setParameter("id", uuid);
        query.setParameter("domain", domain);
        return query.getSingleResult();
    }
}

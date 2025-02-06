package dk.magenta.datafordeler.core.io.storagetest;

import dk.magenta.datafordeler.core.database.Registration;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;

@jakarta.persistence.Entity
@Table(name="test_registration")
public class TestRegistration extends Registration<TestEntity, TestRegistration, TestEffect> {

    public TestRegistration() {}

    public TestRegistration(OffsetDateTime registrationFrom, OffsetDateTime registrationTo, int sequenceNumber) {
        super(registrationFrom, registrationTo, sequenceNumber);
    }
    public TestRegistration(TemporalAccessor registrationFrom, TemporalAccessor registrationTo, int sequenceNumber) {
        super(registrationFrom, registrationTo, sequenceNumber);
    }
    public TestRegistration(String registrationFrom, String registrationTo, int sequenceNumber) {
        super(registrationFrom, registrationTo, sequenceNumber);
    }

    @Override
    protected TestEffect createEmptyEffect(OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        return new TestEffect(this, effectFrom, effectTo);
    }
}

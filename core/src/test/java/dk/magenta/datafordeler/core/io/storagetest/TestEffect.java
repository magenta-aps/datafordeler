package dk.magenta.datafordeler.core.io.storagetest;

import dk.magenta.datafordeler.core.database.Effect;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;

@jakarta.persistence.Entity
@Table(name="test_effect")
public class TestEffect extends Effect<TestRegistration, TestEffect, TestData> {

    public TestEffect() {
        super();
    }

    public TestEffect(TestRegistration testRegistration, OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        super(testRegistration, effectFrom, effectTo);
    }
    public TestEffect(TestRegistration testRegistration, TemporalAccessor effectFrom, TemporalAccessor effectTo) {
        super(testRegistration, effectFrom, effectTo);
    }
    public TestEffect(TestRegistration testRegistration, String effectFrom, String effectTo) {
        super(testRegistration, effectFrom, effectTo);
    }
}


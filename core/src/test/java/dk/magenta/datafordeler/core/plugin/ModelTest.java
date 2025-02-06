package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.plugindemo.model.DemoEntityRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

import java.util.UUID;


@ContextConfiguration(classes = Application.class)
public class ModelTest {

    @Test
    public void testEntity() {

        DemoEntityRecord demoEntity1 = new DemoEntityRecord();
        Identification identification1 = new Identification(UUID.randomUUID(), "test");
        demoEntity1.setIdentification(identification1);

        // Test getIdentification
        Assertions.assertEquals(identification1, demoEntity1.getIdentification());

        // Test getDomaene
        Assertions.assertEquals(identification1.getDomain(), demoEntity1.getDomain());

        // Test getUUID
        Assertions.assertEquals(identification1.getUuid(), demoEntity1.getUUID());


        Identification identification2 = new Identification(UUID.randomUUID(), "test");
        DemoEntityRecord demoEntity2 = new DemoEntityRecord(identification2);

        // Test getIdentification
        Assertions.assertEquals(identification2, demoEntity2.getIdentification());

        // Test getDomaene
        Assertions.assertEquals(identification2.getDomain(), demoEntity2.getDomain());

        // Test getUUID
        Assertions.assertEquals(identification2.getUuid(), demoEntity2.getUUID());


        UUID uuid3 = UUID.randomUUID();
        String domain3 = "test";
        DemoEntityRecord demoEntity3 = new DemoEntityRecord(uuid3, domain3);

        // Test getDomaene
        Assertions.assertEquals(domain3, demoEntity3.getDomain());

        // Test getUUID
        Assertions.assertEquals(uuid3, demoEntity3.getUUID());

        UUID uuid4 = UUID.randomUUID();
        String domain4 = "demo";
        DemoEntityRecord demoEntity4 = demoEntity3;
        demoEntity4.setDomain(domain4);
        demoEntity4.setUUID(uuid4);

        // Test getDomaene
        Assertions.assertEquals(domain4, demoEntity4.getDomain());

        // Test getUUID
        Assertions.assertEquals(uuid4, demoEntity4.getUUID());

    }

}

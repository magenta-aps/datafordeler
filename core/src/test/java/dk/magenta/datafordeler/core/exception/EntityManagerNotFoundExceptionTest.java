package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.plugindemo.model.DemoEntityRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

import java.net.URI;


@ContextConfiguration(classes = Application.class)
public class EntityManagerNotFoundExceptionTest {

    @Test
    public void testEntityManagerNotFoundException() throws Exception {
        EntityManagerNotFoundException exception1 = new EntityManagerNotFoundException(DemoEntityRecord.schema);
        Assertions.assertEquals("EntityManager that handles schema " + DemoEntityRecord.schema + " was not found", exception1.getMessage());

        URI uri = new URI("https://data.gl");
        EntityManagerNotFoundException exception2 = new EntityManagerNotFoundException(uri);
        Assertions.assertEquals("EntityManager that handles URI https://data.gl was not found", exception2.getMessage());

        Assertions.assertEquals("datafordeler.plugin.entitymanager_not_found", exception1.getCode());
    }

}

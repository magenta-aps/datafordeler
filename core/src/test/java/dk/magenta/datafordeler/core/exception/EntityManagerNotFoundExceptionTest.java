package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.plugindemo.model.DemoEntityRecord;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
public class EntityManagerNotFoundExceptionTest {

    @Test
    public void testEntityManagerNotFoundException() throws Exception {
        EntityManagerNotFoundException exception1 = new EntityManagerNotFoundException(DemoEntityRecord.schema);
        Assert.assertEquals("EntityManager that handles schema " + DemoEntityRecord.schema + " was not found", exception1.getMessage());

        URI uri = new URI("https://data.gl");
        EntityManagerNotFoundException exception2 = new EntityManagerNotFoundException(uri);
        Assert.assertEquals("EntityManager that handles URI https://data.gl was not found", exception2.getMessage());

        Assert.assertEquals("datafordeler.plugin.entitymanager_not_found", exception1.getCode());
    }

}

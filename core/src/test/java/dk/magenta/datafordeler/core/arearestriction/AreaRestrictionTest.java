package dk.magenta.datafordeler.core.arearestriction;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.plugindemo.DemoPlugin;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = Application.class)
public class AreaRestrictionTest {

    @Autowired
    private DemoPlugin demoPlugin;

    @Test
    public void testAreaRestrictionType() throws Exception {
        String name = "testType";
        String description = "typeDescription";
        AreaRestrictionType type = new AreaRestrictionType(name, description, demoPlugin);

        Assertions.assertEquals(name, type.getName());
        Assertions.assertEquals(description, type.getDescription());
        Assertions.assertEquals(demoPlugin.getName(), type.getServiceName());
        Assertions.assertEquals(demoPlugin.getName() + ":" + name, type.lookupName());

        AreaRestriction areaRestriction = type.addChoice("testArea", "areaDescription", "12345");
        Assertions.assertEquals(1, type.getChoices().size());
        Assertions.assertEquals(areaRestriction, type.getChoices().iterator().next());
        Assertions.assertEquals(type, areaRestriction.getType());
    }

    @Test
    public void testAreaRestriction() throws Exception {
        String name = "testArea";
        String description = "areaDescription";
        String sumiffiik = "12345";
        AreaRestrictionType type = new AreaRestrictionType("testType", "typeDescription", demoPlugin);
        String typeLookup = type.lookupName();

        AreaRestriction areaRestriction = new AreaRestriction(name, description, sumiffiik, type);

        Assertions.assertEquals(name, areaRestriction.getName());
        Assertions.assertEquals(description, areaRestriction.getDescription());
        Assertions.assertEquals(sumiffiik, areaRestriction.getSumifiik());
        Assertions.assertEquals(type, areaRestriction.getType());
        Assertions.assertEquals(typeLookup+":"+name, areaRestriction.lookupName());
        Assertions.assertEquals(areaRestriction, AreaRestriction.lookup(typeLookup+":"+name));
    }

}

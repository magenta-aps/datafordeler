package dk.magenta.datafordeler.core.arearestriction;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.plugindemo.DemoPlugin;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
public class AreaRestrictionTest {

    @Autowired
    private DemoPlugin demoPlugin;

    @Test
    public void testAreaRestrictionType() throws Exception {
        String name = "testType";
        String description = "typeDescription";
        AreaRestrictionType type = new AreaRestrictionType(name, description, demoPlugin);

        Assert.assertEquals(name, type.getName());
        Assert.assertEquals(description, type.getDescription());
        Assert.assertEquals(demoPlugin.getName(), type.getServiceName());
        Assert.assertEquals(demoPlugin.getName() + ":" + name, type.lookupName());

        AreaRestriction areaRestriction = type.addChoice("testArea", "areaDescription", "12345");
        Assert.assertEquals(1, type.getChoices().size());
        Assert.assertEquals(areaRestriction, type.getChoices().iterator().next());
        Assert.assertEquals(type, areaRestriction.getType());
    }

    @Test
    public void testAreaRestriction() throws Exception {
        String name = "testArea";
        String description = "areaDescription";
        String sumiffiik = "12345";
        AreaRestrictionType type = new AreaRestrictionType("testType", "typeDescription", demoPlugin);
        String typeLookup = type.lookupName();

        AreaRestriction areaRestriction = new AreaRestriction(name, description, sumiffiik, type);

        Assert.assertEquals(name, areaRestriction.getName());
        Assert.assertEquals(description, areaRestriction.getDescription());
        Assert.assertEquals(sumiffiik, areaRestriction.getSumifiik());
        Assert.assertEquals(type, areaRestriction.getType());
        Assert.assertEquals(typeLookup+":"+name, areaRestriction.lookupName());
        Assert.assertEquals(areaRestriction, AreaRestriction.lookup(typeLookup+":"+name));
    }

}

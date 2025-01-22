package dk.magenta.datafordeler.core.util;

import dk.magenta.datafordeler.core.Application;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
public class EqualityTest {

    private class EqualityImpl extends Equality {}

    @Test
    public void init() {
        EqualityImpl equality = new EqualityImpl();
    }

    @Test
    public void testEqual() {
        Assert.assertFalse(Equality.equal("abc", null));
        Assert.assertFalse(Equality.equal(null, "abc"));
        Assert.assertFalse(Equality.equal("abc", "bce"));
        Assert.assertFalse(Equality.equal("a", "A"));
        Assert.assertFalse(Equality.equal("1", "2"));
        Assert.assertFalse(Equality.equal("a", "1"));
        Assert.assertFalse(Equality.equal("abc", "æøå"));
        Assert.assertTrue(Equality.equal("a", "a"));
        Assert.assertTrue(Equality.equal("A", "A"));
        Assert.assertTrue(Equality.equal("ø", "ø"));
        Assert.assertTrue(Equality.equal("abc", "abc"));
        Assert.assertTrue(Equality.equal("æøå", "æøå"));
    }

}

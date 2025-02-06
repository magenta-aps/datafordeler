package dk.magenta.datafordeler.core.util;

import dk.magenta.datafordeler.core.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration(classes = Application.class)
public class EqualityTest {

    private class EqualityImpl extends Equality {}

    @Test
    public void init() {
        EqualityImpl equality = new EqualityImpl();
    }

    @Test
    public void testEqual() {
        Assertions.assertFalse(Equality.equal("abc", null));
        Assertions.assertFalse(Equality.equal(null, "abc"));
        Assertions.assertFalse(Equality.equal("abc", "bce"));
        Assertions.assertFalse(Equality.equal("a", "A"));
        Assertions.assertFalse(Equality.equal("1", "2"));
        Assertions.assertFalse(Equality.equal("a", "1"));
        Assertions.assertFalse(Equality.equal("abc", "æøå"));
        Assertions.assertTrue(Equality.equal("a", "a"));
        Assertions.assertTrue(Equality.equal("A", "A"));
        Assertions.assertTrue(Equality.equal("ø", "ø"));
        Assertions.assertTrue(Equality.equal("abc", "abc"));
        Assertions.assertTrue(Equality.equal("æøå", "æøå"));
    }

}

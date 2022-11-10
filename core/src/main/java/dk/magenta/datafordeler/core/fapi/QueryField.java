package dk.magenta.datafordeler.core.fapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryField {

    String EMPTY = "";

    enum FieldType {
        STRING, INT, LONG, BOOLEAN
    }

    FieldType type() default FieldType.STRING;

    /**
     * The querystring name used when querying this field.
     *
     * @return
     */
    String queryName() default EMPTY;

    String[] queryNames() default {};
}

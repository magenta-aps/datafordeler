package dk.magenta.datafordeler.core.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dk.magenta.datafordeler.core.PluginManager;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.FilterDefs;
import org.hibernate.annotations.ParamDef;

import javax.persistence.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Abstract superclass for all object classes, making sure they have an ID
 */
@MappedSuperclass
@Embeddable
public abstract class DatabaseEntry {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    public Long getId() {
        return this.id;
    }

    public static final String REF = "_id";

    @JsonIgnore
    public List<BaseQuery> getAssoc() {
        return Collections.emptyList();
    }
}

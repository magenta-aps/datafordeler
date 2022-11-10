package dk.magenta.datafordeler.core.arearestriction;

import java.util.HashMap;

/**
 * Describes a specific area that can be used when limiting queries to data in DAFO.
 */
public class AreaRestriction {
    private final String name;
    private final String description;
    private final String sumiffiik;
    private final AreaRestrictionType type;
    private final String value;

    private static final HashMap<String, AreaRestriction> lookupMap = new HashMap<>();

    public AreaRestriction(String name, String description, String sumiffiik,
                           AreaRestrictionType type, String value) {
        this.name = name;
        this.description = description;
        this.sumiffiik = sumiffiik;
        this.type = type;
        this.value = value;
        lookupMap.put(this.lookupName(), this);
    }

    public AreaRestriction(String name, String description, String sumiffiik,
                           AreaRestrictionType type) {
        this(name, description, sumiffiik, type, null);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getSumifiik() {
        return sumiffiik;
    }

    public AreaRestrictionType getType() {
        return type;
    }

    public String getValue() {
        return this.value;
    }

    public String lookupName() {
        return this.type.lookupName() + ":" + this.getName();
    }

    public static AreaRestriction lookup(String lookupName) {
        return lookupMap.get(lookupName);
    }

}

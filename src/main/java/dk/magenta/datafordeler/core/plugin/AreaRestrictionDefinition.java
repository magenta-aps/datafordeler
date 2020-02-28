package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.arearestriction.AreaRestriction;
import dk.magenta.datafordeler.core.arearestriction.AreaRestrictionType;

import java.util.*;

public abstract class AreaRestrictionDefinition {

    protected Map<String, AreaRestrictionType> areaRestrictionTypes = new HashMap<>();

    protected abstract Plugin getPlugin();

    public List<AreaRestriction> getAreaRestrictions() {
        return new ArrayList<AreaRestriction>();
    }

    public AreaRestrictionType getAreaRestrictionTypeByName(String name) {
        return this.areaRestrictionTypes.get(name);
    }

    public AreaRestrictionType addAreaRestrictionType(String name, String description) {
        AreaRestrictionType areaRestrictionType = new AreaRestrictionType(
                name, description, this.getPlugin()
        );
        this.areaRestrictionTypes.put(name, areaRestrictionType);
        return areaRestrictionType;
    }

    public Collection<AreaRestrictionType> getAreaRestrictionTypes() {
        return areaRestrictionTypes.values();
    }

    public AreaRestriction getAreaRestriction(String type, String name) {
        AreaRestrictionType areaRestrictionType = this.getAreaRestrictionTypeByName(type);
        if (areaRestrictionType != null) {
            return areaRestrictionType.getRestriction(name);
        }
        return null;
    }

}

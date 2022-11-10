package dk.magenta.datafordeler.core.arearestriction;

import dk.magenta.datafordeler.core.plugin.Plugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Describes a type of area restrictions used by a given service or plugin.
 */
public class AreaRestrictionType {

    private final String name;
    private final String description;
    private final Plugin sourcePlugin;
    private final Map<String, AreaRestriction> choices = new HashMap<>();

    public AreaRestrictionType(String name, String description, Plugin sourcePlugin) {
        this.name = name;
        this.description = description;
        this.sourcePlugin = sourcePlugin;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getServiceName() {
        return sourcePlugin.getName();
    }

    public AreaRestriction addChoice(String name, String description, String sumiffiik) {
        return this.addChoice(name, description, sumiffiik, null);
    }

    public AreaRestriction addChoice(String name, String description, String sumiffiik, String value) {
        AreaRestriction areaRestriction = new AreaRestriction(name, description, sumiffiik, this, value);
        choices.put(name, areaRestriction);
        return areaRestriction;
    }

    public Collection<AreaRestriction> getChoices() {
        return choices.values();
    }

    public String lookupName() {
        return this.getServiceName() + ":" + this.getName();
    }

    public AreaRestriction getRestriction(String name) {
        return this.choices.get(name);
    }

}

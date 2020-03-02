package dk.magenta.datafordeler.plugindemo;

import dk.magenta.datafordeler.core.arearestriction.AreaRestrictionType;
import dk.magenta.datafordeler.core.plugin.AreaRestrictionDefinition;
import dk.magenta.datafordeler.core.plugin.Plugin;

public class DemoAreaRestrictionDefinition extends AreaRestrictionDefinition {

    private DemoPlugin plugin;

    @Override
    protected Plugin getPlugin() {
        return this.plugin;
    }

    public DemoAreaRestrictionDefinition(DemoPlugin plugin) {
        this.plugin = plugin;
        AreaRestrictionType cardinalDirections = this.addAreaRestrictionType(
                "Cardinal directions",
                "The four cardinal directions"
        );
        cardinalDirections.addChoice("North", "The north quarter", null);
        cardinalDirections.addChoice("East", "The east quarter", null);
        cardinalDirections.addChoice("West", "The west quarter", null);
        cardinalDirections.addChoice("South", "The south quarter", null);
    }
}


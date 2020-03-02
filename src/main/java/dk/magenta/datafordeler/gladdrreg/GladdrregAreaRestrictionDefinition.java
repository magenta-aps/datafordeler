package dk.magenta.datafordeler.gladdrreg;

import dk.magenta.datafordeler.core.plugin.AreaRestrictionDefinition;
import dk.magenta.datafordeler.core.plugin.Plugin;

class GladdrregAreaRestrictionDefinition extends AreaRestrictionDefinition {
    private GladdrregPlugin plugin;

    public GladdrregAreaRestrictionDefinition(GladdrregPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected Plugin getPlugin() {
        return plugin;
    }
}

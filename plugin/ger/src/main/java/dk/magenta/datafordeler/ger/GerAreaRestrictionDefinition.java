package dk.magenta.datafordeler.ger;

import dk.magenta.datafordeler.core.arearestriction.AreaRestrictionType;
import dk.magenta.datafordeler.core.plugin.AreaRestrictionDefinition;
import dk.magenta.datafordeler.core.plugin.Plugin;

public class GerAreaRestrictionDefinition extends AreaRestrictionDefinition {

    private final GerPlugin plugin;

    public static final String RESTRICTIONTYPE_KOMMUNEKODER = "kommunekoder";
    public static final String RESTRICTION_KOMMUNE_KUJALLEQ = "Kujalleq";
    public static final String RESTRICTION_KOMMUNE_QAASUITSUP = "Qaasuitsup";
    public static final String RESTRICTION_KOMMUNE_QEQQATA = "Qeqqata";
    public static final String RESTRICTION_KOMMUNE_SERMERSOOQ = "Sermersooq";
    public static final String RESTRICTION_KOMMUNE_PITUFFIK = "Pituffik";
    public static final String RESTRICTION_KOMMUNE_NATIONALPARK = "Nationalpark";


    public GerAreaRestrictionDefinition(GerPlugin plugin) {
        this.plugin = plugin;
        AreaRestrictionType kommunekodeRestriction = this.addAreaRestrictionType(RESTRICTIONTYPE_KOMMUNEKODER, "Afgrænsning på kommunekode");
        kommunekodeRestriction.addChoice(RESTRICTION_KOMMUNE_KUJALLEQ, "Kujalleq kommune", null, "955");
        kommunekodeRestriction.addChoice(RESTRICTION_KOMMUNE_QAASUITSUP, "Qaasuitsup kommune", null, "958");
        kommunekodeRestriction.addChoice(RESTRICTION_KOMMUNE_QEQQATA, "Qeqqata kommune", null, "957");
        kommunekodeRestriction.addChoice(RESTRICTION_KOMMUNE_SERMERSOOQ, "Sermersooq kommune", null, "956");
        kommunekodeRestriction.addChoice(RESTRICTION_KOMMUNE_PITUFFIK, "Pituffik (udenf.komm.ind.)", null, "961");
        kommunekodeRestriction.addChoice(RESTRICTION_KOMMUNE_NATIONALPARK, "Nationalpark (udenf.komm.ind.)", null, "961");
    }

    @Override
    protected Plugin getPlugin() {
        return this.plugin;
    }
}

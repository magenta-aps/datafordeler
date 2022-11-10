package dk.magenta.datafordeler.cpr;

import dk.magenta.datafordeler.core.arearestriction.AreaRestrictionType;
import dk.magenta.datafordeler.core.plugin.AreaRestrictionDefinition;
import dk.magenta.datafordeler.core.plugin.Plugin;

public class CprAreaRestrictionDefinition extends AreaRestrictionDefinition {

    private final CprPlugin plugin;

    public static final String RESTRICTIONTYPE_KOMMUNEKODER = "kommunekoder";
    public static final String RESTRICTION_KOMMUNE_KUJALLEQ = "Kujalleq";
    public static final String RESTRICTION_KOMMUNE_QAASUITSUP = "Qaasuitsup";
    public static final String RESTRICTION_KOMMUNE_QEQQATA = "Qeqqata";
    public static final String RESTRICTION_KOMMUNE_SERMERSOOQ = "Sermersooq";
    public static final String RESTRICTION_KOMMUNE_PITUFFIK = "Pituffik";
    public static final String RESTRICTION_KOMMUNE_NATIONALPARK = "Nationalpark";
    public static final String RESTRICTION_KOMMUNE_AVANNAATA = "Avannaata";
    public static final String RESTRICTION_KOMMUNE_QEQERTALIK = "Qeqertalik";


    public CprAreaRestrictionDefinition(CprPlugin plugin) {
        this.plugin = plugin;
        AreaRestrictionType kommunekodeRestriction = this.addAreaRestrictionType(RESTRICTIONTYPE_KOMMUNEKODER, "Afgrænsning på kommunekode");
        kommunekodeRestriction.addChoice(RESTRICTION_KOMMUNE_KUJALLEQ, "Kujalleq kommune", null, "955");
        kommunekodeRestriction.addChoice(RESTRICTION_KOMMUNE_QAASUITSUP, "Qaasuitsup kommune", null, "958");
        kommunekodeRestriction.addChoice(RESTRICTION_KOMMUNE_QEQQATA, "Qeqqata kommune", null, "957");
        kommunekodeRestriction.addChoice(RESTRICTION_KOMMUNE_SERMERSOOQ, "Sermersooq kommune", null, "956");
        kommunekodeRestriction.addChoice(RESTRICTION_KOMMUNE_PITUFFIK, "Pituffik (udenf.komm.ind.)", null, "961");
        kommunekodeRestriction.addChoice(RESTRICTION_KOMMUNE_NATIONALPARK, "Nationalpark (udenf.komm.ind.)", null, "961");
        kommunekodeRestriction.addChoice(RESTRICTION_KOMMUNE_QEQERTALIK, "Kommune Qeqertalik", null, "959");
        kommunekodeRestriction.addChoice(RESTRICTION_KOMMUNE_AVANNAATA, "Avannaata Kommunia", null, "960");
    }

    @Override
    protected Plugin getPlugin() {
        return this.plugin;
    }
}

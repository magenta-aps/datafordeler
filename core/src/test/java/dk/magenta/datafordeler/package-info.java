@FilterDefs({
        @FilterDef(name = Bitemporal.FILTER_EFFECTFROM_AFTER, parameters = {@ParamDef(name = Bitemporal.FILTERPARAM_EFFECTFROM_AFTER, type = OffsetDateTime.class)}),
        @FilterDef(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, parameters = {@ParamDef(name = Bitemporal.FILTERPARAM_EFFECTFROM_BEFORE, type = OffsetDateTime.class)}),
        @FilterDef(name = Bitemporal.FILTER_EFFECTTO_AFTER, parameters = {@ParamDef(name = Bitemporal.FILTERPARAM_EFFECTTO_AFTER, type = OffsetDateTime.class)}),
        @FilterDef(name = Bitemporal.FILTER_EFFECTTO_BEFORE, parameters = {@ParamDef(name = Bitemporal.FILTERPARAM_EFFECTTO_BEFORE, type = OffsetDateTime.class)}),
        @FilterDef(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, parameters = {@ParamDef(name = Monotemporal.FILTERPARAM_REGISTRATIONFROM_AFTER, type = OffsetDateTime.class)}),
        @FilterDef(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, parameters = {@ParamDef(name = Monotemporal.FILTERPARAM_REGISTRATIONFROM_BEFORE, type = OffsetDateTime.class)}),
        @FilterDef(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, parameters = {@ParamDef(name = Monotemporal.FILTERPARAM_REGISTRATIONTO_AFTER, type = OffsetDateTime.class)}),
        @FilterDef(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, parameters = {@ParamDef(name = Monotemporal.FILTERPARAM_REGISTRATIONTO_BEFORE, type = OffsetDateTime.class)}),
        @FilterDef(name = Nontemporal.FILTER_LASTUPDATED_AFTER, parameters = {@ParamDef(name = Nontemporal.FILTERPARAM_LASTUPDATED_AFTER, type = OffsetDateTime.class)}),
        @FilterDef(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, parameters = {@ParamDef(name = Nontemporal.FILTERPARAM_LASTUPDATED_BEFORE, type = OffsetDateTime.class)}),
        @FilterDef(name = DataItem.FILTER_RECORD_AFTER, parameters = {@ParamDef(name = DataItem.FILTERPARAM_RECORD_AFTER, type = OffsetDateTime.class)})
})

package dk.magenta.datafordeler;

import dk.magenta.datafordeler.core.database.Bitemporal;
import dk.magenta.datafordeler.core.database.DataItem;
import dk.magenta.datafordeler.core.database.Monotemporal;
import dk.magenta.datafordeler.core.database.Nontemporal;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.FilterDefs;
import org.hibernate.annotations.ParamDef;
import java.time.OffsetDateTime;

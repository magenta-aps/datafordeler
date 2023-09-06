package dk.magenta.datafordeler.core.util;

import dk.magenta.datafordeler.core.fapi.BaseQuery;

import java.time.OffsetDateTime;

public class BitemporalityQuery {
    public OffsetDateTime registrationFromAfter;
    public OffsetDateTime registrationFromBefore;
    public OffsetDateTime registrationToAfter;
    public OffsetDateTime registrationToBefore;
    public OffsetDateTime effectFromAfter;
    public OffsetDateTime effectFromBefore;
    public OffsetDateTime effectToAfter;
    public OffsetDateTime effectToBefore;

    public BitemporalityQuery(OffsetDateTime registrationFromAfter, OffsetDateTime registrationFromBefore, OffsetDateTime registrationToAfter, OffsetDateTime registrationToBefore, OffsetDateTime effectFromAfter, OffsetDateTime effectFromBefore, OffsetDateTime effectToAfter, OffsetDateTime effectToBefore) {
        this.registrationFromAfter = registrationFromAfter;
        this.registrationFromBefore = registrationFromBefore;
        this.registrationToAfter = registrationToAfter;
        this.registrationToBefore = registrationToBefore;
        this.effectFromAfter = effectFromAfter;
        this.effectFromBefore = effectFromBefore;
        this.effectToAfter = effectToAfter;
        this.effectToBefore = effectToBefore;
    }

    public BitemporalityQuery(BaseQuery baseQuery) {
        this(
                baseQuery.getRegistrationFromAfter(),
                baseQuery.getRegistrationFromBefore(),
                baseQuery.getRegistrationToAfter(),
                baseQuery.getRegistrationToBefore(),
                baseQuery.getEffectFromAfter(),
                baseQuery.getEffectFromBefore(),
                baseQuery.getEffectToAfter(),
                baseQuery.getEffectToBefore()
        );
    }

    public boolean matchesRegistrationFromAfter(Bitemporality bitemporality) {
        if (this.registrationFromAfter != null && bitemporality != null) {
            return bitemporality.registrationFrom != null && !bitemporality.registrationFrom.isBefore(this.registrationFromAfter);
        }
        return true;
    }
    public boolean matchesRegistrationFromBefore(Bitemporality bitemporality) {
        if (this.registrationFromBefore != null && bitemporality != null) {
            return bitemporality.registrationFrom == null || !bitemporality.registrationFrom.isAfter(this.registrationFromBefore);
        }
        return true;
    }
    public boolean matchesRegistrationToAfter(Bitemporality bitemporality) {
        if (this.registrationToAfter != null && bitemporality != null) {
            return bitemporality.registrationTo == null || !bitemporality.registrationTo.isBefore(this.registrationToAfter);
        }
        return true;
    }
    public boolean matchesRegistrationToBefore(Bitemporality bitemporality) {
        if (this.registrationToBefore != null && bitemporality != null) {
            return bitemporality.registrationTo != null && !bitemporality.registrationTo.isAfter(this.registrationToBefore);
        }
        return true;
    }

    public boolean matchesEffectFromAfter(Bitemporality bitemporality) {
        if (this.effectFromAfter != null && bitemporality != null) {
            return bitemporality.effectFrom != null && !bitemporality.effectFrom.isBefore(this.effectFromAfter);
        }
        return true;
    }
    public boolean matchesEffectFromBefore(Bitemporality bitemporality) {
        if (this.effectFromBefore != null && bitemporality != null) {
            return bitemporality.effectFrom == null || !bitemporality.effectFrom.isAfter(this.effectFromBefore);
        }
        return true;
    }
    public boolean matchesEffectToAfter(Bitemporality bitemporality) {
        if (this.effectToAfter != null && bitemporality != null) {
            return bitemporality.effectTo == null || !bitemporality.effectTo.isBefore(this.effectToAfter);
        }
        return true;
    }
    public boolean matchesEffectToBefore(Bitemporality bitemporality) {
        if (this.effectToBefore != null && bitemporality != null) {
            return bitemporality.effectTo != null && !bitemporality.effectTo.isAfter(this.effectToBefore);
        }
        return true;
    }

    public boolean matchesRegistration(Bitemporality bitemporality) {
        return this.matchesRegistrationFromAfter(bitemporality) &&
                this.matchesRegistrationFromBefore(bitemporality) &&
                this.matchesRegistrationToAfter(bitemporality) &&
                this.matchesRegistrationToBefore(bitemporality);
    }
    public boolean matchesEffect(Bitemporality bitemporality) {
        return this.matchesEffectFromAfter(bitemporality) &&
                this.matchesEffectFromBefore(bitemporality) &&
                this.matchesEffectToAfter(bitemporality) &&
                this.matchesEffectToBefore(bitemporality);
    }

    public boolean matches(Bitemporality bitemporality) {
        return this.matchesRegistration(bitemporality) &&
                this.matchesEffect(bitemporality);
    }

    @Override
    public String toString() {
        return "BitemporalityQuery{" +
                "registrationFromAfter=" + registrationFromAfter +
                ", registrationFromBefore=" + registrationFromBefore +
                ", registrationToAfter=" + registrationToAfter +
                ", registrationToBefore=" + registrationToBefore +
                ", effectFromAfter=" + effectFromAfter +
                ", effectFromBefore=" + effectFromBefore +
                ", effectToAfter=" + effectToAfter +
                ", effectToBefore=" + effectToBefore +
                '}';
    }
}

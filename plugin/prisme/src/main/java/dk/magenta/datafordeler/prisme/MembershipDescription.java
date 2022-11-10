package dk.magenta.datafordeler.prisme;

import java.util.HashSet;
import java.util.Objects;

public class MembershipDescription {
    public String organizationType;
    public String organizationName;
    public String memberFunction;

    public MembershipDescription(String organizationType, String organizationName, String memberFunction) {
        this.organizationType = organizationType;
        this.organizationName = organizationName;
        this.memberFunction = memberFunction;
    }

    public boolean match(MembershipDescription template) {
        return this.match(this.organizationType, template.organizationType)
                && this.match(this.organizationName, template.organizationName)
                && this.match(this.memberFunction, template.memberFunction);
    }

    private boolean match(String subject, String template) {
        return template == null || template.equals("*") || Objects.equals(template, subject);
    }

    private static final HashSet<MembershipDescription> templates = new HashSet<>();

    static {
        templates.add(new MembershipDescription("LEDELSESORGAN", "Direktion", "adm. dir"));
        templates.add(new MembershipDescription("*", "Reelle ejere", "Reel ejer"));
        templates.add(new MembershipDescription("FULDT_ANSVARLIG_DELTAGERE", "Interessenter", "INTERESSENTER"));
    }

    public boolean isOwner() {
        for (MembershipDescription template : templates) {
            if (this.match(template)) {
                return true;
            }
        }
        return false;
    }

}



package dk.magenta.datafordeler.eskat.utils;

import dk.magenta.datafordeler.cvr.records.CompanyParticipantRelationRecord;
import dk.magenta.datafordeler.cvr.records.CompanyStatusRecord;
import dk.magenta.datafordeler.cvr.records.unversioned.CompanyStatus;
import dk.magenta.datafordeler.eskat.output.ParticipantObject;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ParticipantUnwrapper {

    /**
     * Convert a list of participants from the database into a list of participants with required attributes
     * @param relations
     * @param cpr
     * @param personName
     * @return
     */
    public static List<ParticipantObject> CompanyParticipantRelationRecord(List<CompanyParticipantRelationRecord> relations, String cpr, String personName) {

        ArrayList<ParticipantObject> list = new ArrayList<ParticipantObject>();

        for(CompanyParticipantRelationRecord relation : relations) {
            CompanyStatusRecord companyStatus = relation.getRelationCompanyRecord().getCompanyStatus().stream().filter(f -> f.getEffectTo()==null).findFirst().orElse(null);

            if(companyStatus!=null) {

                ParticipantObject participantObject = new ParticipantObject(relation.getRelationCompanyRecord().getCvrNumber() + "",
                        cpr, personName,
                        relation.getRelationCompanyRecord().getNames().iterator().next().getName() + "",
                        companyStatus.getStatus(),
                        DateConverter.dateConvert(relation.getRegistrationFrom()), DateConverter.dateConvert(relation.getRegistrationTo()),
                        DateConverter.dateConvert(companyStatus.getEffectFrom()), DateConverter.dateConvert(companyStatus.getEffectTo()));
                list.add(participantObject);
            }
        }
        return list;
    }
}

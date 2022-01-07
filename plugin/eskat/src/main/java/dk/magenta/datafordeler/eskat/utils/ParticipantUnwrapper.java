package dk.magenta.datafordeler.eskat.utils;

import dk.magenta.datafordeler.cvr.records.CompanyParticipantRelationRecord;
import dk.magenta.datafordeler.cvr.records.CompanyStatusRecord;
import dk.magenta.datafordeler.eskat.output.ParticipantEntity;

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
    public static List<ParticipantEntity> CompanyParticipantRelationRecord(List<CompanyParticipantRelationRecord> relations, String cpr, String personName) {

        ArrayList<ParticipantEntity> list = new ArrayList<ParticipantEntity>();

        for(CompanyParticipantRelationRecord relation : relations) {
            CompanyStatusRecord companyStatus = relation.getRelationCompanyRecord().getCompanyStatus().stream().filter(f -> f.getEffectTo()==null).findFirst().orElse(null);

            if(companyStatus!=null) {

                ParticipantEntity participantObject = new ParticipantEntity(relation.getRelationCompanyRecord().getCvrNumber() + "",
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

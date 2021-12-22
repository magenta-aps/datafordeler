package dk.magenta.datafordeler.eskat.utils;

import dk.magenta.datafordeler.cvr.records.CompanyParticipantRelationRecord;
import dk.magenta.datafordeler.eskat.output.ParticipantObject;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ParticipantUnwrapper {

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");



    public static List<ParticipantObject> CompanyParticipantRelationRecord(List<CompanyParticipantRelationRecord> relations, String cpr, String personName) {

        ArrayList<ParticipantObject> list = new ArrayList<ParticipantObject>();






        for(CompanyParticipantRelationRecord relation : relations) {


            if(relation.getRelationCompanyRecord().getCompanyStatus().size() > 0) {

                ParticipantObject p = new ParticipantObject(relation.getRelationCompanyRecord().getCvrNumber() + "",
                        cpr, personName,
                        relation.getRelationCompanyRecord().getNames().iterator().next().getName() + "",
                        relation.getRelationCompanyRecord().getCompanyStatus().iterator().next().getStatus(),
                        dateConvert(relation.getRegistrationFrom()), dateConvert(relation.getRegistrationTo()),
                        dateConvert(relation.getRelationCompanyRecord().getCompanyStatus().iterator().next().getEffectFrom()),
                        dateConvert(relation.getRelationCompanyRecord().getCompanyStatus().iterator().next().getEffectTo()));

                list.add(p);
            }
        }
        return list;
    }


    private static String dateConvert(OffsetDateTime datetime) {
        if(datetime==null) {
            return null;
        } else {
            return datetime.format(formatter);
        }
    }

}

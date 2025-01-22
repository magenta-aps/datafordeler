package dk.magenta.datafordeler.cpr.records.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.util.Bitemporality;
import dk.magenta.datafordeler.core.util.BitemporalityQuery;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.person.GenericParentOutputDTO;
import dk.magenta.datafordeler.cpr.records.person.data.AddressDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.ParentDataRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;

@Component
public class PersonRecordOutputWrapper extends CprRecordOutputWrapper<PersonEntity> {

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    private void register() {
        this.register(PersonEntity.class);
    }

    @Value("${pitu.base.url}")
    private String pituBaseUrl;


    @Override
    public ObjectMapper getObjectMapper() {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return this.objectMapper;
    }


    private static ObjectNode convert(Pair<String, ObjectNode> input) {
        String key = input.getFirst();
        ObjectNode data = input.getSecond();
        switch (key) {
            case PersonEntity.IO_FIELD_MOTHER:
            case PersonEntity.IO_FIELD_MOTHER_VERIFICATION:
                data.put("foraelderrolle", "MOR");
                break;
            case PersonEntity.IO_FIELD_FATHER:
            case PersonEntity.IO_FIELD_FATHER_VERIFICATION:
                data.put("foraelderrolle", "FAR_MEDMOR");
                break;
        }
        return data;
    }

    @Override
    protected ObjectNode fallbackOutput(Mode mode, OutputContainer recordOutput, BitemporalityQuery mustMatch) {
        if (mode == Mode.LEGACY) {

            HashMap<String, String> keyConversion = new HashMap<>();
            keyConversion.put(PersonEntity.IO_FIELD_MOTHER, "foraeldreoplysning");
            keyConversion.put(PersonEntity.IO_FIELD_FATHER, "foraeldreoplysning");
            keyConversion.put(PersonEntity.IO_FIELD_MOTHER_VERIFICATION, "foraeldreverifikation");
            keyConversion.put(PersonEntity.IO_FIELD_FATHER_VERIFICATION, "foraeldreverifikation");
            keyConversion.put(PersonEntity.IO_FIELD_CORE, "person");

            keyConversion.put("fødselsted", "fødselsdata");
            keyConversion.put("fødselstidspunkt", "fødselsdata");

            return recordOutput.getRDV(mustMatch, keyConversion, PersonRecordOutputWrapper::convert);
        }
        return null;
    }

    @Override
    public Object wrapResult(PersonEntity record, BaseQuery query, Mode mode) {
        BitemporalityQuery mustMatch = new BitemporalityQuery(query);
        return this.getNode(record, mustMatch, mode);
    }

    @Override
    protected void fillContainer(OutputContainer container, PersonEntity record, Mode mode) {
        container.addNontemporal(PersonEntity.IO_FIELD_CPR_NUMBER, record.getPersonnummer());
        container.addBitemporal(PersonEntity.IO_FIELD_ADDRESS_CONAME, record.getConame(), true);
        container.addBitemporal(PersonEntity.IO_FIELD_ADDRESS, record.getAddress());
        container.addBitemporal(PersonEntity.IO_FIELD_ADDRESS_NAME, record.getAddressName());
        container.addBitemporal(PersonEntity.IO_FIELD_BIRTHPLACE, record.getBirthPlace());
        if (!Mode.DATAONLY.equals(mode)) {
            container.addBitemporal(PersonEntity.IO_FIELD_BIRTHPLACE_VERIFICATION, record.getBirthPlaceVerification(), true);
            container.addBitemporal(PersonEntity.IO_FIELD_CHURCH_VERIFICATION, record.getChurchRelationVerification(), true);
            container.addBitemporal(PersonEntity.IO_FIELD_CITIZENSHIP_VERIFICATION, record.getCitizenshipVerification(), true);
            container.addBitemporal(PersonEntity.IO_FIELD_CIVILSTATUS_AUTHORITYTEXT, record.getCivilstatusAuthorityText());
            container.addBitemporal(PersonEntity.IO_FIELD_CIVILSTATUS_VERIFICATION, record.getCivilstatusVerification());
            container.addBitemporal(PersonEntity.IO_FIELD_NAME_AUTHORITY_TEXT, record.getNameAuthorityText());
            container.addBitemporal(PersonEntity.IO_FIELD_NAME_VERIFICATION, record.getNameVerification());
            container.addBitemporal(PersonEntity.IO_FIELD_MOTHER_VERIFICATION, record.getMotherVerification());
            container.addBitemporal(PersonEntity.IO_FIELD_FATHER_VERIFICATION, record.getFatherVerification());
            container.addBitemporal(PersonEntity.IO_FIELD_MOTHER, record.getMother());
            container.addBitemporal(PersonEntity.IO_FIELD_FATHER, record.getFather());
        } else {
            container.addNontemporal(PersonEntity.IO_FIELD_MOTHER, getParentOutputDTO(record.getMother().iterator()));
            container.addNontemporal(PersonEntity.IO_FIELD_FATHER, getParentOutputDTO(record.getFather().iterator()));
        }
        container.addBitemporal(PersonEntity.IO_FIELD_CHILDREN, record.getChildren());
        container.addBitemporal(PersonEntity.IO_FIELD_BIRTHTIME, record.getBirthTime());
        container.addBitemporal(PersonEntity.IO_FIELD_CHURCH, record.getChurchRelation(), true);
        container.addBitemporal(PersonEntity.IO_FIELD_CITIZENSHIP, record.getCitizenship(), true);
        container.addBitemporal(PersonEntity.IO_FIELD_CIVILSTATUS, record.getCivilstatus());
        container.addBitemporal(PersonEntity.IO_FIELD_FOREIGN_ADDRESS, record.getForeignAddress());
        container.addBitemporal(PersonEntity.IO_FIELD_FOREIGN_ADDRESS_EMIGRATION, record.getEmigration(), true);
        container.addBitemporal(PersonEntity.IO_FIELD_MOVE_MUNICIPALITY, record.getMunicipalityMove());
        container.addBitemporal(PersonEntity.IO_FIELD_NAME, record.getName());
        container.addBitemporal(PersonEntity.IO_FIELD_CORE, record.getCore(), true);
        container.addBitemporal(PersonEntity.IO_FIELD_PNR, record.getPersonNumber(), true);
        container.addBitemporal(PersonEntity.IO_FIELD_POSITION, record.getPosition(), true);
        container.addBitemporal(PersonEntity.IO_FIELD_STATUS, record.getStatus(), true);
        container.addBitemporal(PersonEntity.IO_FIELD_GUARDIAN, record.getGuardian(), true);
        container.addBitemporal(PersonEntity.IO_FIELD_PROTECTION, record.getProtection());
    }

    public Map<Class, List<String>> getEligibleModifierNames() {
        HashMap<Class, List<String>> map = new HashMap<>();
        ArrayList<String> addressModifiers = new ArrayList<>();
        addressModifiers.add("geo_municipality");
        addressModifiers.add("geo_road");
        addressModifiers.add("geo_accessaddress");
        addressModifiers.add("geo_locality");
        addressModifiers.add("cpr_road");
        map.put(AddressDataRecord.class, addressModifiers);
        return map;
    }

    public HashSet<GenericParentOutputDTO> getParentOutputDTO(Iterator<ParentDataRecord> parent) {
        HashSet<GenericParentOutputDTO> parentList = new HashSet();
        while (parent.hasNext()) {
            GenericParentOutputDTO genericParentOutputDTO = new GenericParentOutputDTO();
            ParentDataRecord p = parent.next();
            genericParentOutputDTO.setPnr(p.getCprNumber());
            genericParentOutputDTO.setPituBaseUrl(pituBaseUrl);
            parentList.add(genericParentOutputDTO);
        }
        return parentList;
    }

}

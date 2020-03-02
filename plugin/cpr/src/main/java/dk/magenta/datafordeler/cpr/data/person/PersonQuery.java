package dk.magenta.datafordeler.cpr.data.person;

import dk.magenta.datafordeler.core.database.LookupDefinition;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.cpr.data.CprQuery;
import dk.magenta.datafordeler.cpr.data.person.data.PersonAddressData;
import dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData;
import dk.magenta.datafordeler.cpr.data.person.data.PersonNameData;

import java.util.*;

/**
 * Container for a query for Persons, defining fields and database lookup
 */
public class PersonQuery extends CprQuery<PersonEntity> {

    public static final String PERSONNUMMER = PersonEntity.IO_FIELD_CPR_NUMBER;
    public static final String FORNAVNE = PersonNameData.IO_FIELD_FIRST_NAMES;
    public static final String EFTERNAVN = PersonNameData.IO_FIELD_LAST_NAME;
    public static final String KOMMUNEKODE = PersonAddressData.IO_FIELD_MUNICIPALITY_CODE;

    @QueryField(type = QueryField.FieldType.STRING, queryName = PERSONNUMMER)
    private List<String> personnumre = new ArrayList<>();

    public Collection<String> getPersonnumre() {
        return this.personnumre;
    }

    public void addPersonnummer(String personnummer) {
        this.personnumre.add(personnummer);
        if (personnummer != null) {
            this.increaseDataParamCount();
        }
    }

    public void setPersonnummer(String personnummer) {
        this.personnumre.clear();
        this.addPersonnummer(personnummer);
    }


    @QueryField(type = QueryField.FieldType.STRING, queryName = FORNAVNE)
    private String fornavn;

    public String getFornavn() {
        return fornavn;
    }

    public void setFornavn(String fornavn) {
        this.fornavn = fornavn;
        if (fornavn != null) {
            this.increaseDataParamCount();
        }
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = EFTERNAVN)
    private String efternavn;

    public String getEfternavn() {
        return efternavn;
    }

    public void setEfternavn(String efternavn) {
        this.efternavn = efternavn;
        if (efternavn != null) {
            this.increaseDataParamCount();
        }
    }



    @QueryField(type = QueryField.FieldType.STRING, queryName = KOMMUNEKODE)
    private List<String> kommunekoder = new ArrayList<>();

    public Collection<String> getKommunekoder() {
        return this.kommunekoder;
    }

    public void addKommunekode(String kommunekode) {
        this.kommunekoder.add(kommunekode);
    }

    public void addKommunekode(int kommunekode) {
        this.addKommunekode(String.format("%03d", kommunekode));
    }

    public void clearKommunekode() {
        this.kommunekoder.clear();
    }



    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(PERSONNUMMER, this.personnumre);
        map.put(FORNAVNE, this.fornavn);
        map.put(EFTERNAVN, this.efternavn);
        map.put(KOMMUNEKODE, this.kommunekoder);
        return map;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) {
        if (parameters.containsKey(PERSONNUMMER)) {
            for (String personnummer : parameters.get(PERSONNUMMER)) {
                this.addPersonnummer(personnummer);
            }
        }
        this.setFornavn(parameters.getFirst(FORNAVNE));
        this.setEfternavn(parameters.getFirst(EFTERNAVN));
        if (parameters.containsKey(KOMMUNEKODE)) {
            for (String kommunekode : parameters.get(KOMMUNEKODE)) {
                this.addKommunekode(kommunekode);
            }
        }
    }

    @Override
    public Class<PersonEntity> getEntityClass() {
        return PersonEntity.class;
    }

    @Override
    public Class getDataClass() {
        return PersonBaseData.class;
    }


    @Override
    public LookupDefinition getLookupDefinition() {
        LookupDefinition lookupDefinition = super.getLookupDefinition();
        if (!this.personnumre.isEmpty()) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator + PersonEntity.DB_FIELD_CPR_NUMBER, this.personnumre, String.class);
        }
        if (this.fornavn != null) {
            lookupDefinition.put(PersonBaseData.DB_FIELD_NAME + LookupDefinition.separator + PersonNameData.DB_FIELD_FIRST_NAMES, this.fornavn, String.class);
        }
        if (this.efternavn != null) {
            lookupDefinition.put(PersonBaseData.DB_FIELD_NAME + LookupDefinition.separator + PersonNameData.DB_FIELD_LAST_NAME, this.efternavn, String.class);
        }
        if (!this.kommunekoder.isEmpty()) {
            lookupDefinition.put(PersonBaseData.DB_FIELD_ADDRESS + LookupDefinition.separator + PersonAddressData.DB_FIELD_MUNICIPALITY_CODE, this.kommunekoder, Integer.class);
        }
        if (!this.getKommunekodeRestriction().isEmpty()) {
            lookupDefinition.put(PersonBaseData.DB_FIELD_ADDRESS + LookupDefinition.separator + PersonAddressData.DB_FIELD_MUNICIPALITY_CODE, this.getKommunekodeRestriction(), Integer.class);
        }
        return lookupDefinition;
    }

}

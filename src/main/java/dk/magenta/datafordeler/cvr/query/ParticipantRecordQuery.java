package dk.magenta.datafordeler.cvr.query;

import dk.magenta.datafordeler.core.database.BaseLookupDefinition;
import dk.magenta.datafordeler.core.database.LookupDefinition;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.cvr.records.*;
import dk.magenta.datafordeler.cvr.records.unversioned.Municipality;

import java.util.*;

/**
 * Container for a query for Participants, defining fields and database lookup
 */
public class ParticipantRecordQuery extends BaseQuery {

    public static final String UNITNUMBER = ParticipantRecord.IO_FIELD_UNIT_NUMBER;
    public static final String NAVN = ParticipantRecord.IO_FIELD_NAMES;
    public static final String KOMMUNEKODE = Municipality.IO_FIELD_CODE;



    @QueryField(type = QueryField.FieldType.LONG, queryName = UNITNUMBER)
    private List<String> enhedsNummer = new ArrayList<>();

    public Collection<String> getEnhedsNummer() {
        return enhedsNummer;
    }

    public void addEnhedsNummer(String enhedsNummer) {
        if (enhedsNummer != null) {
            this.enhedsNummer.add(enhedsNummer);
            this.increaseDataParamCount();
        }
    }

    public void setEnhedsNummer(String enhedsNumre) {
        this.enhedsNummer.clear();
        this.addEnhedsNummer(enhedsNumre);
    }

    public void setEnhedsNummer(Collection<String> enhedsNumre) {
        this.enhedsNummer.clear();
        if (enhedsNumre != null) {
            for (String enhedsNummer : enhedsNumre) {
                this.addEnhedsNummer(enhedsNummer);
            }
        }
    }

    public void clearEnhedsNummer() {
        this.enhedsNummer.clear();
    }



    @QueryField(type = QueryField.FieldType.STRING, queryName = KOMMUNEKODE)
    private List<String> kommunekode = new ArrayList<>();

    public Collection<String> getKommuneKode() {
        return kommunekode;
    }

    public void addKommuneKode(String kommunekode) {
        if (kommunekode != null) {
            this.kommunekode.add(kommunekode);
            this.increaseDataParamCount();
        }
    }

    public void addKommuneKode(int kommunekode) {
        this.addKommuneKode(String.format("%03d", kommunekode));
    }

    public void setKommuneKode(String kommunekode) {
        this.kommunekode.clear();
        this.addKommuneKode(kommunekode);
    }

    public void setKommuneKode(Collection<String> kommunekoder) {
        this.kommunekode.clear();
        if (kommunekoder != null) {
            for (String kommunekode : kommunekoder) {
                this.addKommuneKode(kommunekode);
            }
        }
    }

    public void clearKommuneKode() {
        this.kommunekode.clear();
    }



    @QueryField(type = QueryField.FieldType.STRING, queryName = NAVN)
    private List<String> navn = new ArrayList<>();

    public List<String> getNavn() {
        return this.navn;
    }

    public void addNavn(String navn) {
        if (navn != null) {
            this.navn.add(navn);
            this.increaseDataParamCount();
        }
    }

    public void setNavn(String navn) {
        this.navn.clear();
        this.addNavn(navn);
    }

    public void setNavn(Collection<String> navne) {
        this.navn.clear();
        if (navne != null) {
            for (String navn : navne) {
                this.addNavn(navn);
            }
        }
    }

    public void clearNavn() {
        this.navn.clear();
    }






    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(UNITNUMBER, this.enhedsNummer);
        map.put(NAVN, this.navn);
        map.put(KOMMUNEKODE, this.kommunekode);
        return map;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) {
        this.setEnhedsNummer(parameters.getI(UNITNUMBER));
        this.setNavn(parameters.getI(NAVN));
        this.setKommuneKode(parameters.getI(KOMMUNEKODE));
    }
    @Override
    public BaseLookupDefinition getLookupDefinition() {
        BaseLookupDefinition lookupDefinition = new CvrRecordLookupDefinition(this);

        if (this.getEnhedsNummer() != null && !this.getEnhedsNummer().isEmpty()) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator + ParticipantRecord.DB_FIELD_UNIT_NUMBER, this.getEnhedsNummer(), Long.class);
        }

        if (this.getNavn() != null && !this.getNavn().isEmpty()) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator + ParticipantRecord.DB_FIELD_NAMES + LookupDefinition.separator + SecNameRecord.DB_FIELD_NAME, this.getNavn(), String.class);
        }

        if (this.getKommuneKode() != null && !this.getKommuneKode().isEmpty()) {
            StringJoiner sj = new StringJoiner(LookupDefinition.separator);
            sj.add(LookupDefinition.entityref);
            sj.add(ParticipantRecord.DB_FIELD_LOCATION_ADDRESS);
            sj.add(AddressRecord.DB_FIELD_MUNICIPALITY);
            sj.add(AddressMunicipalityRecord.DB_FIELD_MUNICIPALITY);
            sj.add(Municipality.DB_FIELD_CODE);
            lookupDefinition.put(sj.toString(), this.getKommuneKode(), Integer.class);
        }
        if (this.getKommunekodeRestriction() != null && !this.getKommunekodeRestriction().isEmpty()) {
            StringJoiner sj = new StringJoiner(LookupDefinition.separator);
            sj.add(LookupDefinition.entityref);
            sj.add(ParticipantRecord.DB_FIELD_LOCATION_ADDRESS);
            sj.add(AddressRecord.DB_FIELD_MUNICIPALITY);
            sj.add(AddressMunicipalityRecord.DB_FIELD_MUNICIPALITY);
            sj.add(Municipality.DB_FIELD_CODE);
            lookupDefinition.put(sj.toString(), this.getKommunekodeRestriction(), Integer.class);
        }
        return lookupDefinition;
    }

    @Override
    protected Object castFilterParam(Object input, String filter) {
        return super.castFilterParam(input, filter);
    }
}

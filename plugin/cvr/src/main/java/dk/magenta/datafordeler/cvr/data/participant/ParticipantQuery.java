package dk.magenta.datafordeler.cvr.data.participant;

import dk.magenta.datafordeler.core.database.LookupDefinition;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.cvr.data.CvrQuery;
import dk.magenta.datafordeler.cvr.data.shared.AddressData;
import dk.magenta.datafordeler.cvr.data.shared.IntegerData;
import dk.magenta.datafordeler.cvr.data.shared.TextData;
import dk.magenta.datafordeler.cvr.data.unversioned.Address;
import dk.magenta.datafordeler.cvr.data.unversioned.Municipality;

import java.util.*;

/**
 * Container for a query for Participants, defining fields and database lookup
 */
public class ParticipantQuery extends CvrQuery<ParticipantEntity> {

    public static final String UNITNUMBER = ParticipantEntity.IO_FIELD_PARTICIPANT_NUMBER;
    public static final String NAVN = ParticipantBaseData.IO_FIELD_NAMES;
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
    public Class<ParticipantEntity> getEntityClass() {
        return ParticipantEntity.class;
    }

    @Override
    public Class getDataClass() {
        return ParticipantBaseData.class;
    }



    @Override
    public LookupDefinition getLookupDefinition() {
        LookupDefinition lookupDefinition = new LookupDefinition(this, ParticipantBaseData.class);

        if (this.enhedsNummer != null && !this.enhedsNummer.isEmpty()) {
            lookupDefinition.put(ParticipantBaseData.DB_FIELD_UNIT_NUMBER + LookupDefinition.separator + IntegerData.DB_FIELD_VALUE, this.enhedsNummer, Long.class);
        }

        if (this.navn != null && !this.navn.isEmpty()) {
            lookupDefinition.put(ParticipantBaseData.DB_FIELD_NAMES + LookupDefinition.separator + TextData.DB_FIELD_VALUE, this.navn, String.class);
        }

        if (this.kommunekode != null && !this.kommunekode.isEmpty()) {
            StringJoiner sj = new StringJoiner(LookupDefinition.separator);
            sj.add(ParticipantBaseData.DB_FIELD_LOCATION_ADDRESS);
            sj.add(AddressData.DB_FIELD_ADDRESS);
            sj.add(Address.DB_FIELD_MUNICIPALITY);
            sj.add(Municipality.DB_FIELD_CODE);
            lookupDefinition.put(sj.toString(), this.kommunekode, Integer.class);
        }
        if (this.getKommunekodeRestriction() != null && !this.getKommunekodeRestriction().isEmpty()) {
            StringJoiner sj = new StringJoiner(LookupDefinition.separator);
            sj.add(ParticipantBaseData.DB_FIELD_LOCATION_ADDRESS);
            sj.add(AddressData.DB_FIELD_ADDRESS);
            sj.add(Address.DB_FIELD_MUNICIPALITY);
            sj.add(Municipality.DB_FIELD_CODE);
            lookupDefinition.put(sj.toString(), this.getKommunekodeRestriction(), Integer.class);
        }
        return lookupDefinition;
    }

}

package dk.magenta.datafordeler.cvr.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.database.BaseLookupDefinition;
import dk.magenta.datafordeler.core.database.LookupDefinition;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.*;
import dk.magenta.datafordeler.cpr.records.person.data.PersonDataEventDataRecord;
import dk.magenta.datafordeler.cvr.DirectLookup;
import dk.magenta.datafordeler.cvr.records.*;
import dk.magenta.datafordeler.cvr.records.unversioned.CompanyForm;
import dk.magenta.datafordeler.cvr.records.unversioned.Municipality;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * Container for a query for Companies, defining fields and database lookup
 */
public class CompanyRecordQuery extends BaseQuery {

    public static final String CVRNUMMER = CompanyRecord.IO_FIELD_CVR_NUMBER;
    public static final String REKLAMEBESKYTTELSE = CompanyRecord.IO_FIELD_ADVERTPROTECTION;
    public static final String NAVN = CompanyRecord.IO_FIELD_NAMES;
    public static final String TELEFONNUMMER = CompanyRecord.IO_FIELD_PHONE;
    public static final String TELEFAXNUMMER = CompanyRecord.IO_FIELD_FAX;
    public static final String EMAILADRESSE = CompanyRecord.IO_FIELD_EMAIL;
    public static final String VIRKSOMHEDSFORM = CompanyRecord.IO_FIELD_FORM;
    public static final String KOMMUNEKODE = Municipality.IO_FIELD_CODE;
    public static final String VEJKODE = AddressRecord.IO_FIELD_ROADCODE;
    public static final String HUSNUMMER = "husnummer";
    public static final String ETAGE = AddressRecord.IO_FIELD_FLOOR;
    public static final String DOOR = AddressRecord.IO_FIELD_DOOR;
    public static final String LASTUPDATED = CvrBitemporalRecord.IO_FIELD_LAST_UPDATED;
    public static final String COMPANYDATAEVENT = PersonDataEventDataRecord.DB_FIELD_FIELD;
    public static final String COMPANYDATAEVENTTIME = PersonDataEventDataRecord.DB_FIELD_TIMESTAMP;

    @QueryField(type = QueryField.FieldType.STRING, queryName = CVRNUMMER)
    private List<String> cvrNumre = new ArrayList<>();

    public Collection<String> getCvrNumre() {
        return cvrNumre;
    }

    public void addCvrNummer(String cvrnummer) {
        if (cvrnummer != null) {
            this.cvrNumre.add(cvrnummer);
            this.updatedParameters();
        }
    }

    public void setCvrNumre(String cvrNumre) {
        this.clearCvrNumre();
        this.addCvrNummer(cvrNumre);
    }

    public void setCvrNumre(Collection<String> cvrNumre) {
        this.clearCvrNumre();
        if (cvrNumre != null) {
            for (String cvrNummer : cvrNumre) {
                this.addCvrNummer(cvrNummer);
            }
        }
    }

    public void clearCvrNumre() {
        this.cvrNumre.clear();
        this.updatedParameters();
    }


    @QueryField(type = QueryField.FieldType.STRING, queryName = REKLAMEBESKYTTELSE)
    private String reklamebeskyttelse;

    public String getReklamebeskyttelse() {
        return this.reklamebeskyttelse;
    }

    public void setReklamebeskyttelse(String reklamebeskyttelse) {
        this.clearReklamebeskyttelse();
        if (reklamebeskyttelse != null) {
            this.reklamebeskyttelse = reklamebeskyttelse;
            this.updatedParameters();
        }
    }

    public void clearReklamebeskyttelse() {
        this.reklamebeskyttelse = null;
    }




    @QueryField(type = QueryField.FieldType.STRING, queryName = NAVN)
    private List<String> virksomhedsnavn = new ArrayList<>();

    public List<String> getVirksomhedsnavn() {
        return this.virksomhedsnavn;
    }

    public void addVirksomhedsnavn(String virksomhedsnavn) {
        if (virksomhedsnavn != null) {
            this.virksomhedsnavn.add(virksomhedsnavn);
            this.updatedParameters();
        }
    }

    public void setVirksomhedsnavn(String virksomhedsnavn) {
        this.clearVirksomhedsnavn();
        this.addVirksomhedsnavn(virksomhedsnavn);
    }

    public void setVirksomhedsnavn(Collection<String> virksomhedsnavne) {
        this.clearVirksomhedsnavn();
        if (virksomhedsnavne != null) {
            for (String virksomhedsnavn : virksomhedsnavne) {
                this.addVirksomhedsnavn(virksomhedsnavn);
            }
        }
    }

    public void clearVirksomhedsnavn() {
        this.virksomhedsnavn.clear();
        this.updatedParameters();
    }



    @QueryField(type = QueryField.FieldType.STRING, queryName = TELEFONNUMMER)
    private List<String> telefonnummer = new ArrayList<>();

    public List<String> getTelefonnummer() {
        return this.telefonnummer;
    }

    public void addTelefonnummer(String telefonnummer) {
        if (telefonnummer != null) {
            this.telefonnummer.add(telefonnummer);
            this.updatedParameters();
        }
    }

    public void setTelefonnummer(String telefonnummer) {
        this.clearTelefonnummer();
        this.addTelefonnummer(telefonnummer);
    }

    public void setTelefonnummer(Collection<String> telefonnumre) {
        this.clearTelefonnummer();
        if (telefonnumre != null) {
            for (String telefonnummer : telefonnumre) {
                this.addTelefonnummer(telefonnummer);
            }
        }
    }

    public void clearTelefonnummer() {
        this.telefonnummer.clear();
        this.updatedParameters();
    }



    @QueryField(type = QueryField.FieldType.STRING, queryName = TELEFAXNUMMER)
    private List<String> telefaxnummer = new ArrayList<>();

    public List<String> getTelefaxnummer() {
        return this.telefaxnummer;
    }

    public void addTelefaxnummer(String telefaxnummer) {
        if (telefaxnummer != null) {
            this.telefaxnummer.add(telefaxnummer);
            this.updatedParameters();
        }
    }

    public void setTelefaxnummer(String telefaxnummer) {
        this.clearTelefaxnummer();
        this.addTelefaxnummer(telefaxnummer);
    }

    public void setTelefaxnummer(Collection<String> telefaxnumre) {
        this.clearTelefaxnummer();
        if (telefaxnumre != null) {
            for (String telefaxnummer : telefaxnumre) {
                this.addTelefaxnummer(telefaxnummer);
            }
        }
    }

    public void clearTelefaxnummer() {
        this.telefaxnummer.clear();
        this.updatedParameters();
    }



    @QueryField(type = QueryField.FieldType.STRING, queryName = EMAILADRESSE)
    private List<String> emailadresse = new ArrayList<>();

    public List<String> getEmailadresse() {
        return this.emailadresse;
    }

    public void addEmailadresse(String emailadresse) {
        if (emailadresse != null) {
            this.emailadresse.add(emailadresse);
            this.updatedParameters();
        }
    }

    public void setEmailadresse(String emailadresse) {
        this.clearEmailadresse();
        this.addEmailadresse(emailadresse);
    }

    public void setEmailadresse(Collection<String> emailadresser) {
        this.emailadresse.clear();
        if (emailadresser != null) {
            for (String emailadresse : emailadresser) {
                this.addEmailadresse(emailadresse);
            }
        }
    }

    public void clearEmailadresse() {
        this.emailadresse.clear();
        this.updatedParameters();
    }



    @QueryField(type = QueryField.FieldType.STRING, queryName = VIRKSOMHEDSFORM)
    private List<String> virksomhedsform = new ArrayList<>();

    public List<String> getVirksomhedsform() {
        return this.virksomhedsform;
    }

    public void addVirksomhedsform(String virksomhedsform) {
        if (virksomhedsform != null) {
            this.virksomhedsform.add(virksomhedsform);
            this.updatedParameters();
        }
    }

    public void addVirksomhedsform(int virksomhedsform) {
        this.addVirksomhedsform(Integer.toString(virksomhedsform));
    }

    public void setVirksomhedsform(String virksomhedsform) {
        this.clearVirksomhedsform();
        this.addVirksomhedsform(virksomhedsform);
    }

    public void setVirksomhedsform(int virksomhedsform) {
        this.setVirksomhedsform(Integer.toString(virksomhedsform));
    }

    public void setVirksomhedsform(Collection<String> virksomhedsformer) {
        this.clearVirksomhedsform();
        if (virksomhedsformer != null) {
            for (String virksomhedsform : virksomhedsformer) {
                this.addVirksomhedsform(virksomhedsform);
            }
        }
    }

    public void clearVirksomhedsform() {
        this.virksomhedsform.clear();
        this.updatedParameters();
    }



    @QueryField(type = QueryField.FieldType.STRING, queryName = KOMMUNEKODE)
    private List<String> kommunekode = new ArrayList<>();

    public Collection<String> getKommuneKode() {
        return this.kommunekode;
    }

    public void addKommuneKode(String kommunekode) {
        if (kommunekode != null) {
            this.kommunekode.add(kommunekode);
            this.updatedParameters();
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
        this.clearKommuneKoder();
        if (kommunekoder != null) {
            for (String kommunekode : kommunekoder) {
                this.addKommuneKode(kommunekode);
            }
        }
    }

    public void setKommuneKode(int kommunekode) {
        this.setKommuneKode(String.format("%03d", kommunekode));
    }

    public void clearKommuneKoder() {
        this.kommunekode.clear();
        this.updatedParameters();
    }





    @QueryField(type = QueryField.FieldType.STRING, queryName = VEJKODE)
    private List<String> vejkode = new ArrayList<>();

    public Collection<String> getVejKode() {
        return this.vejkode;
    }

    public void addVejKode(String vejkode) {
        if (vejkode != null) {
            this.vejkode.add(vejkode);
            this.updatedParameters();
        }
    }

    public void addVejKode(int vejkode) {
        this.addVejKode(String.format("%03d", vejkode));
    }

    public void setVejkode(String vejkode) {
        this.vejkode.clear();
        this.addVejKode(vejkode);
    }

    public void setVejkode(Collection<String> vejkoder) {
        this.clearVejKoder();
        if (vejkoder != null) {
            for (String vejkode : vejkoder) {
                this.addVejKode(vejkode);
            }
        }
    }

    public void setVejKode(int vejkode) {
        this.setVejkode(String.format("%03d", vejkode));
    }

    public void clearVejKoder() {
        this.vejkode.clear();
        this.updatedParameters();
    }






    @QueryField(type = QueryField.FieldType.STRING, queryName = HUSNUMMER)
    private List<String> husnummer = new ArrayList<>();

    public Collection<String> getHusnummer() {
        return this.husnummer;
    }

    public void addHusnummer(String husnummer) {
        if (husnummer != null) {
            this.husnummer.add(husnummer);
            this.updatedParameters();
        }
    }

    public void setHusnummer(String husnummer) {
        this.husnummer.clear();
        this.addHusnummer(husnummer);
    }

    public void setHusnummer(Collection<String> husnumre) {
        this.clearHusnummer();
        if (husnumre != null) {
            for (String husnummer : husnumre) {
                this.addHusnummer(husnummer);
            }
        }
    }

    public void clearHusnummer() {
        this.husnummer.clear();
        this.updatedParameters();
    }



    @QueryField(type = QueryField.FieldType.STRING, queryName = ETAGE)
    private List<String> etage = new ArrayList<>();

    public Collection<String> getEtage() {
        return this.etage;
    }

    public void addEtage(String etage) {
        if (etage != null) {
            this.etage.add(etage);
            this.updatedParameters();
        }
    }

    public void setEtage(String etage) {
        this.etage.clear();
        this.addEtage(etage);
    }

    public void setEtage(Collection<String> etager) {
        this.clearEtager();
        if (etager != null) {
            for (String etage : etager) {
                this.addEtage(etage);
            }
        }
    }

    public void clearEtager() {
        this.etage.clear();
        this.updatedParameters();
    }






    @QueryField(type = QueryField.FieldType.STRING, queryName = DOOR)
    private List<String> door = new ArrayList<>();

    public Collection<String> getDoor() {
        return this.door;
    }

    public void addDoor(String door) {
        if (door != null) {
            this.door.add(door);
            this.updatedParameters();
        }
    }

    public void setDoor(String door) {
        this.door.clear();
        this.addDoor(door);
    }

    public void setDoor(Collection<String> doors) {
        this.clearDoor();
        if (doors != null) {
            for (String door : doors) {
                this.addDoor(door);
            }
        }
    }

    public void clearDoor() {
        this.door.clear();
        this.updatedParameters();
    }

    

    private List<String> organizationType = new ArrayList<>();

    public Collection<String> getOrganizationType() {
        return this.organizationType;
    }

    public void addOrganizationType(String organizationType) {
        if (organizationType != null) {
            this.organizationType.add(organizationType);
            this.updatedParameters();
        }
    }

    public void setOrganizationType(int organizationType) {
        this.organizationType.clear();
        this.addOrganizationType(Integer.toString(organizationType));
    }
    public void setOrganizationType(String organizationType) {
        this.organizationType.clear();
        this.addOrganizationType(organizationType);
    }

    public void setOrganizationType(Collection<String> organizationTypes) {
        this.clearOrganizationType();
        if (organizationType != null) {
            for (String organizationType : organizationTypes) {
                this.addOrganizationType(organizationType);
            }
        }
    }

    public void clearOrganizationType() {
        this.organizationType.clear();
        this.updatedParameters();
    }


    @QueryField(type = QueryField.FieldType.STRING, queryName = LASTUPDATED)
    private String lastUpdated;
    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }





    @QueryField(type = QueryField.FieldType.STRING, queryName = COMPANYDATAEVENT)
    private List<String> companydataevents = new ArrayList<>();

    public Collection<String> getDataEvents() {
        return this.companydataevents;
    }

    public void addDataEvent(String persondataevent) {
        this.companydataevents.add(persondataevent);
        if (persondataevent != null) {
            this.updatedParameters();
        }
    }

    public void setDataEvent(String personevent) {
        this.clearDataEvents();
        this.addDataEvent(personevent);
    }

    public void clearDataEvents() {
        this.companydataevents.clear();
        this.updatedParameters();
    }

    public void setDataEvents(Collection<String> personevent) {
        this.clearDataEvents();
        if (personevent != null) {
            this.companydataevents.addAll(personevent);
            this.updatedParameters();
        }
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = COMPANYDATAEVENTTIME)
    private OffsetDateTime companyrecordeventTimeAfter;

    public void setDataEventTimeAfter(OffsetDateTime companyrecordeventTimeAfter) {
        this.companyrecordeventTimeAfter = companyrecordeventTimeAfter;
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = COMPANYDATAEVENTTIME)
    private OffsetDateTime companyrecordeventTimeBefore;

    public void setDataEventTimeBefore(OffsetDateTime companyrecordeventTimeBefore) {
        this.companyrecordeventTimeBefore = companyrecordeventTimeBefore;
    }


    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(CVRNUMMER, this.cvrNumre);
        map.put(REKLAMEBESKYTTELSE, this.reklamebeskyttelse);
        map.put(NAVN, this.virksomhedsnavn);
        map.put(TELEFONNUMMER, this.telefonnummer);
        map.put(TELEFAXNUMMER, this.telefaxnummer);
        map.put(EMAILADRESSE, this.emailadresse);
        map.put(VIRKSOMHEDSFORM, this.virksomhedsform);
        map.put(KOMMUNEKODE, this.kommunekode);
        map.put(VEJKODE, this.vejkode);
        map.put(HUSNUMMER, this.husnummer);
        map.put(ETAGE, this.etage);
        map.put(DOOR, this.door);
        map.put(LASTUPDATED, this.lastUpdated);
        return map;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) {
        this.setCvrNumre(parameters.getI(CVRNUMMER));
        this.setReklamebeskyttelse(parameters.getFirst(REKLAMEBESKYTTELSE));
        this.setVirksomhedsnavn(parameters.getI(NAVN));
        this.setTelefonnummer(parameters.getI(TELEFONNUMMER));
        this.setTelefaxnummer(parameters.getI(TELEFAXNUMMER));
        this.setEmailadresse(parameters.getI(EMAILADRESSE));
        this.setVirksomhedsform(parameters.getI(VIRKSOMHEDSFORM));
        this.setKommuneKode(parameters.getI(KOMMUNEKODE));
        this.setVejkode(parameters.getI(VEJKODE));
        this.setHusnummer(parameters.getI(HUSNUMMER));
        this.setEtage(parameters.getI(ETAGE));
        this.setDoor(parameters.getI(DOOR));
        this.setLastUpdated(parameters.getFirst(LASTUPDATED));
    }


    @Override
    public BaseLookupDefinition getLookupDefinition() {
        CvrRecordLookupDefinition lookupDefinition = new CvrRecordLookupDefinition(this);

        if (this.getCvrNumre() != null && !this.getCvrNumre().isEmpty()) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator + CompanyRecord.DB_FIELD_CVR_NUMBER, this.getCvrNumre(), Integer.class);
        }
        if (this.getVirksomhedsform() != null && !this.getVirksomhedsform().isEmpty()) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator + CompanyRecord.DB_FIELD_FORM + LookupDefinition.separator + FormRecord.DB_FIELD_FORM + LookupDefinition.separator + CompanyForm.DB_FIELD_CODE, this.getVirksomhedsform(), String.class);
        }
        if (this.getReklamebeskyttelse() != null) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator + CompanyRecord.DB_FIELD_ADVERTPROTECTION, this.getReklamebeskyttelse(), Boolean.class);
        }
        if (this.getVirksomhedsnavn() != null && !this.getVirksomhedsnavn().isEmpty()) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator + CompanyRecord.DB_FIELD_NAMES + LookupDefinition.separator + SecNameRecord.DB_FIELD_NAME, this.getVirksomhedsnavn(), String.class);
        }
        if (this.getTelefonnummer() != null && !this.getTelefonnummer().isEmpty()) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator + CompanyRecord.DB_FIELD_PHONE + LookupDefinition.separator + ContactRecord.DB_FIELD_DATA, this.getTelefonnummer(), String.class);
        }
        if (this.getTelefaxnummer() != null && !this.getTelefaxnummer().isEmpty()) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator + CompanyRecord.DB_FIELD_FAX + LookupDefinition.separator +ContactRecord.DB_FIELD_DATA, this.getTelefaxnummer(), String.class);
        }
        if (this.getEmailadresse() != null && !this.getEmailadresse().isEmpty()) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator + CompanyRecord.DB_FIELD_EMAIL + LookupDefinition.separator + ContactRecord.DB_FIELD_DATA, this.getEmailadresse(), String.class);
        }

        String addressMunicipalityPath =
                LookupDefinition.entityref + LookupDefinition.separator +
                CompanyRecord.DB_FIELD_LOCATION_ADDRESS + LookupDefinition.separator +
                AddressRecord.DB_FIELD_MUNICIPALITY + LookupDefinition.separator +
                AddressMunicipalityRecord.DB_FIELD_MUNICIPALITY;
        boolean joinedAddress = false;

        if (this.getKommuneKode() != null && !this.getKommuneKode().isEmpty()) {
            lookupDefinition.put(addressMunicipalityPath + LookupDefinition.separator + Municipality.DB_FIELD_CODE, this.getKommuneKode(), Integer.class);
            joinedAddress = true;
        }
        if (this.getKommunekodeRestriction() != null && !this.getKommunekodeRestriction().isEmpty()) {
            lookupDefinition.put(addressMunicipalityPath + LookupDefinition.separator + Municipality.DB_FIELD_CODE, this.getKommunekodeRestriction(), Integer.class);
            joinedAddress = true;
        }


        if (this.getVejKode() != null && !this.getVejKode().isEmpty()) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator +
                    CompanyRecord.DB_FIELD_LOCATION_ADDRESS + LookupDefinition.separator + AddressRecord.DB_FIELD_ROADCODE, this.getVejKode(), Integer.class);
            joinedAddress = true;
        }
        if (this.getEtage() != null && !this.getEtage().isEmpty()) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator +
                    CompanyRecord.DB_FIELD_LOCATION_ADDRESS + LookupDefinition.separator + AddressRecord.DB_FIELD_FLOOR, this.getEtage(), String.class);
            joinedAddress = true;
        }
        if (this.getDoor() != null && !this.getDoor().isEmpty()) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator +
                    CompanyRecord.DB_FIELD_LOCATION_ADDRESS + LookupDefinition.separator + AddressRecord.DB_FIELD_FLOOR, this.getDoor(), String.class);
            joinedAddress = true;
        }

        return lookupDefinition;
    }

    @Override
    protected boolean isEmpty() {
        return this.cvrNumre.isEmpty() && this.virksomhedsform.isEmpty() && this.reklamebeskyttelse == null && this.virksomhedsnavn.isEmpty() &&
                this.telefonnummer.isEmpty() && this.telefaxnummer.isEmpty() && this.emailadresse.isEmpty() && this.kommunekode.isEmpty() &&
                this.vejkode.isEmpty() && this.husnummer.isEmpty() && this.etage.isEmpty() && this.door.isEmpty();
    }

    @Override
    protected Object castFilterParam(Object input, String filter) {
        return super.castFilterParam(input, filter);
    }

    @Override
    public String getEntityClassname() {
        return CompanyRecord.class.getCanonicalName();
    }

    @Override
    public String getEntityIdentifier() {
        return "cvr_company";
    }

    private static HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.put("cvr", CompanyRecord.DB_FIELD_CVR_NUMBER);
        joinHandles.put("formcode", CompanyRecord.DB_FIELD_FORM + BaseQuery.separator + FormRecord.DB_FIELD_FORM + BaseQuery.separator + CompanyForm.DB_FIELD_CODE);
        joinHandles.put("advertprotection", CompanyRecord.DB_FIELD_ADVERTPROTECTION);
        joinHandles.put("name", CompanyRecord.DB_FIELD_NAMES + BaseQuery.separator + SecNameRecord.DB_FIELD_NAME);
        joinHandles.put("phone", CompanyRecord.DB_FIELD_PHONE + BaseQuery.separator + ContactRecord.DB_FIELD_DATA);
        joinHandles.put("fax", CompanyRecord.DB_FIELD_FAX + BaseQuery.separator +ContactRecord.DB_FIELD_DATA);
        joinHandles.put("email", CompanyRecord.DB_FIELD_EMAIL + BaseQuery.separator + ContactRecord.DB_FIELD_DATA);
        joinHandles.put("municipalitycode", CompanyRecord.DB_FIELD_LOCATION_ADDRESS + BaseQuery.separator + AddressRecord.DB_FIELD_MUNICIPALITY + BaseQuery.separator + AddressMunicipalityRecord.DB_FIELD_MUNICIPALITY + BaseQuery.separator + Municipality.DB_FIELD_CODE);
        joinHandles.put("roadcode", CompanyRecord.DB_FIELD_LOCATION_ADDRESS + BaseQuery.separator + AddressRecord.DB_FIELD_ROADCODE);
        joinHandles.put("housenumberfrom", CompanyRecord.DB_FIELD_LOCATION_ADDRESS + BaseQuery.separator + AddressRecord.DB_FIELD_HOUSE_FROM);
        joinHandles.put("housenumberto", CompanyRecord.DB_FIELD_LOCATION_ADDRESS + BaseQuery.separator + AddressRecord.DB_FIELD_HOUSE_TO);
        joinHandles.put("floor", CompanyRecord.DB_FIELD_LOCATION_ADDRESS + BaseQuery.separator + AddressRecord.DB_FIELD_FLOOR);
        joinHandles.put("door", CompanyRecord.DB_FIELD_LOCATION_ADDRESS + BaseQuery.separator + AddressRecord.DB_FIELD_DOOR);
        joinHandles.put("participantUnitNumber", CompanyRecord.DB_FIELD_PARTICIPANTS + BaseQuery.separator + CompanyParticipantRelationRecord.DB_FIELD_PARTICIPANT_RELATION + BaseQuery.separator + RelationParticipantRecord.DB_FIELD_UNITNUMBER);
        joinHandles.put("participantOrganizationType", CompanyRecord.DB_FIELD_PARTICIPANTS + BaseQuery.separator + CompanyParticipantRelationRecord.DB_FIELD_ORGANIZATIONS + BaseQuery.separator + OrganizationRecord.DB_FIELD_MAIN_TYPE);
        joinHandles.put("lastUpdated", CvrBitemporalRecord.DB_FIELD_LAST_UPDATED);
        joinHandles.put("companyrecordeventTime.GTE", CompanyRecord.DB_FIELD_DATAEVENT + LookupDefinition.separator + CompanyDataEventRecord.DB_FIELD_TIMESTAMP);
        joinHandles.put("companyrecordeventTime.LTE", CompanyRecord.DB_FIELD_DATAEVENT + LookupDefinition.separator + CompanyDataEventRecord.DB_FIELD_TIMESTAMP);
    }

    @Override
    protected Map<String, String> joinHandles() {
        return joinHandles;
    }

    protected void setupConditions() throws QueryBuildException {
        this.addCondition("cvr", this.cvrNumre, Integer.class);
        this.addCondition("formcode", this.virksomhedsform);
        this.addCondition("advertprotection", this.reklamebeskyttelse != null ? Collections.singletonList(this.reklamebeskyttelse) : Collections.emptyList(), Boolean.class);
        this.addCondition("name", this.virksomhedsnavn);
        this.addCondition("phone", this.telefonnummer);
        this.addCondition("fax", this.telefaxnummer);
        this.addCondition("email", this.emailadresse);
        this.addCondition("municipalitycode", this.kommunekode, Integer.class);
        this.addCondition("roadcode", this.vejkode, Integer.class);
        this.addCondition("floor", this.etage);
        this.addCondition("door", this.door);
        this.addCondition("municipalitycode", this.getKommunekodeRestriction(), Integer.class);

        if (!this.husnummer.isEmpty()) {
            // Match hvis housenumberfrom == value   eller   housenumberfrom <= value og housenumberto >= value
            MultiCondition multiCondition = new MultiCondition(this.getCondition(), "OR");
            this.addCondition(multiCondition);
            this.makeCondition(multiCondition, "housenumberfrom", Condition.Operator.EQ, this.husnummer, Integer.class, false);
            MultiCondition rangeCondition = new MultiCondition(multiCondition);
            multiCondition.add(rangeCondition);
            this.makeCondition(rangeCondition, "housenumberfrom", Condition.Operator.LTE, this.husnummer, Integer.class, false);
            this.makeCondition(rangeCondition, "housenumberto", Condition.Operator.GTE, this.husnummer, Integer.class, false);
        }

        if (this.organizationType != null) {
            this.addCondition("participantOrganizationType", this.organizationType);
        }
        if (this.lastUpdated != null) {
            this.addCondition("lastUpdated", Condition.Operator.GT, Collections.singletonList(this.lastUpdated), OffsetDateTime.class, false);
        }
        this.addCondition("companyrecordeventTime.GTE", Condition.Operator.GTE, this.companyrecordeventTimeAfter, OffsetDateTime.class, true);
        this.addCondition("companyrecordeventTime.LTE", Condition.Operator.LTE, this.companyrecordeventTimeBefore, OffsetDateTime.class, true);
    }


    public ObjectNode getDirectLookupQuery(ObjectMapper objectMapper) {
        boolean anySet = false;
        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode query = DirectLookup.addObject(objectMapper, root, "query");
        ObjectNode bool = DirectLookup.addObject(objectMapper, query, "bool");
        ArrayNode must = DirectLookup.addList(objectMapper, bool, "must");

        if (this.getCvrNumre() != null && !this.getCvrNumre().isEmpty()) {
            ObjectNode wrap = DirectLookup.addObject(objectMapper, must);
            ObjectNode terms = DirectLookup.addObject(objectMapper, wrap, "terms");
            ArrayNode cvrNumber = DirectLookup.addList(objectMapper, terms, "Vrvirksomhed.cvrNummer");
            for (String cvr : this.getCvrNumre()) {
                cvrNumber.add(cvr);
            }
            anySet = true;
        }

        if (!anySet) {
            return null;
        }

        return root;
    }
}

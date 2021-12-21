package dk.magenta.datafordeler.cvr.query;

import dk.magenta.datafordeler.core.exception.QueryBuildException;
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
    public static final String VEJKODE = AddressRecord.IO_FIELD_ROADCODE;



    @QueryField(type = QueryField.FieldType.LONG, queryName = UNITNUMBER)
    private List<String> enhedsNummer = new ArrayList<>();

    public Collection<String> getEnhedsNummer() {
        return enhedsNummer;
    }

    public void addEnhedsNummer(String enhedsNummer) {
        if (enhedsNummer != null) {
            this.enhedsNummer.add(enhedsNummer);
            this.updatedParameters();
        }
    }

    public void setEnhedsNummer(String enhedsNumre) {
        this.clearEnhedsNummer();
        this.addEnhedsNummer(enhedsNumre);
    }

    public void setEnhedsNummer(Collection<String> enhedsNumre) {
        this.clearEnhedsNummer();
        if (enhedsNumre != null) {
            for (String enhedsNummer : enhedsNumre) {
                this.addEnhedsNummer(enhedsNummer);
            }
        }
    }

    public void clearEnhedsNummer() {
        this.enhedsNummer.clear();
        this.updatedParameters();
    }



    @QueryField(type = QueryField.FieldType.STRING, queryName = KOMMUNEKODE)
    private List<String> kommunekode = new ArrayList<>();

    public Collection<String> getKommuneKode() {
        return kommunekode;
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
        this.clearKommuneKode();
        this.addKommuneKode(kommunekode);
    }

    public void setKommuneKode(Collection<String> kommunekoder) {
        this.clearKommuneKode();
        if (kommunekoder != null) {
            for (String kommunekode : kommunekoder) {
                this.addKommuneKode(kommunekode);
            }
        }
    }

    public void clearKommuneKode() {
        this.kommunekode.clear();
        this.updatedParameters();
    }




    @QueryField(type = QueryField.FieldType.STRING, queryName = VEJKODE)
    private List<String> vejkode = new ArrayList<>();

    public Collection<String> getVejkode() {
        return vejkode;
    }

    public void addVejkode(String vejkode) {
        if (vejkode != null) {
            this.vejkode.add(vejkode);
            this.updatedParameters();
        }
    }

    public void addVejkode(int vejkode) {
        this.addVejkode(String.format("%03d", vejkode));
    }

    public void setVejkode(String vejkode) {
        this.clearVejkode();
        this.addVejkode(vejkode);
    }

    public void setVejkode(Collection<String> vejkoder) {
        this.clearVejkode();
        if (vejkoder != null) {
            for (String vejkode : vejkoder) {
                this.addVejkode(vejkode);
            }
        }
    }

    public void clearVejkode() {
        this.vejkode.clear();
        this.updatedParameters();
    }



    @QueryField(type = QueryField.FieldType.STRING, queryName = NAVN)
    private List<String> navn = new ArrayList<>();

    public List<String> getNavn() {
        return this.navn;
    }

    public void addNavn(String navn) {
        if (navn != null) {
            this.navn.add(navn);
            this.updatedParameters();
        }
    }

    public void setNavn(String navn) {
        this.clearNavn();
        this.addNavn(navn);
    }

    public void setNavn(Collection<String> navne) {
        this.clearNavn();
        if (navne != null) {
            for (String navn : navne) {
                this.addNavn(navn);
            }
        }
    }

    public void clearNavn() {
        this.navn.clear();
        this.updatedParameters();
    }



    @QueryField(type = QueryField.FieldType.STRING, queryName = NAVN)
    private List<String> businessKey = new ArrayList<>();

    public List<String> getBusinessKey() {
        return this.businessKey;
    }

    public void addBusinessKey(String businessKey) {
        if (businessKey != null) {
            this.businessKey.add(businessKey);
            this.updatedParameters();
        }
    }

    public void setBusinessKey(String businessKey) {
        this.clearBusinessKey();
        this.addBusinessKey(businessKey);
    }

    public void setBusinessKey(Collection<String> businessKeys) {
        this.clearBusinessKey();
        if (businessKeys != null) {
            for (String businessKey : businessKeys) {
                this.addBusinessKey(businessKey);
            }
        }
    }

    public void clearBusinessKey() {
        this.businessKey.clear();
        this.updatedParameters();
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = NAVN)
    private List<String> cvrnumber = new ArrayList<>();

    public List<String> getCvrnumber() {
        return this.cvrnumber;
    }

    public void addCvrnumber(String vrnumber) {
        if (cvrnumber != null) {
            this.cvrnumber.add(vrnumber);
            this.updatedParameters();
        }
    }

    public void setCvrnumber(String businessKey) {
        this.clearCvrnumber();
        this.addCvrnumber(businessKey);
    }

    public void setCvrnumber(Collection<String> businessKeys) {
        this.clearCvrnumber();
        if (cvrnumber != null) {
            for (String cvrnumber : cvrnumber) {
                this.addCvrnumber(cvrnumber);
            }
        }
    }

    public void clearCvrnumber() {
        this.cvrnumber.clear();
        this.updatedParameters();
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = NAVN)
    private List<String> companyNames = new ArrayList<>();

    public List<String> getCompanyNames() {
        return this.companyNames;
    }

    public void addCompanyNames(String vrnumber) {
        if (companyNames != null) {
            this.companyNames.add(vrnumber);
            this.updatedParameters();
        }
    }

    public void setCompanyNames(String businessKey) {
        this.clearCompanyNames();
        this.addCompanyNames(businessKey);
    }

    public void setCompanyNames(Collection<String> businessKeys) {
        this.clearCompanyNames();
        if (companyNames != null) {
            for (String cvrnumber : companyNames) {
                this.addCompanyNames(cvrnumber);
            }
        }
    }

    public void clearCompanyNames() {
        this.companyNames.clear();
        this.updatedParameters();
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = NAVN)
    private List<String> companyStatuses = new ArrayList<>();

    public List<String> getStatuses() {
        return this.companyStatuses;
    }

    public void addStatuses(String vrnumber) {
        if (companyStatuses != null) {
            this.companyStatuses.add(vrnumber);
            this.updatedParameters();
        }
    }

    public void setStatuses(String businessKey) {
        this.clearCompanyNames();
        this.addCompanyNames(businessKey);
    }

    public void setStatuses(Collection<String> businessKeys) {
        this.clearCompanyNames();
        if (companyStatuses != null) {
            for (String cvrnumber : companyStatuses) {
                this.addCompanyNames(cvrnumber);
            }
        }
    }

    public void clearStatuses() {
        this.companyStatuses.clear();
        this.updatedParameters();
    }



    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(UNITNUMBER, this.enhedsNummer);
        map.put(NAVN, this.navn);
        map.put(KOMMUNEKODE, this.kommunekode);
        map.put(VEJKODE, this.vejkode);
        return map;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) {
        this.setEnhedsNummer(parameters.getI(UNITNUMBER));
        this.setNavn(parameters.getI(NAVN));
        this.setKommuneKode(parameters.getI(KOMMUNEKODE));
        this.setBusinessKey(parameters.getI("businessKey"));
    }

    @Override
    protected boolean isEmpty() {
        return this.enhedsNummer.isEmpty() && this.navn.isEmpty() && this.kommunekode.isEmpty() && this.vejkode.isEmpty() && this.businessKey.isEmpty();
    }

    @Override
    protected Object castFilterParam(Object input, String filter) {
        return super.castFilterParam(input, filter);
    }

    @Override
    public String getEntityClassname() {
        return ParticipantRecord.class.getCanonicalName();
    }

    @Override
    public String getEntityIdentifier() {
        return "cvr_participant";
    }


    private static HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.put("unit", ParticipantRecord.DB_FIELD_UNIT_NUMBER);
        joinHandles.put("name", ParticipantRecord.DB_FIELD_NAMES + BaseQuery.separator + SecNameRecord.DB_FIELD_NAME);
        joinHandles.put("municipalitycode", ParticipantRecord.DB_FIELD_LOCATION_ADDRESS + BaseQuery.separator + AddressRecord.DB_FIELD_MUNICIPALITY + BaseQuery.separator + AddressMunicipalityRecord.DB_FIELD_MUNICIPALITY + BaseQuery.separator + Municipality.DB_FIELD_CODE);
        joinHandles.put("roadcode", ParticipantRecord.DB_FIELD_LOCATION_ADDRESS + BaseQuery.separator + AddressRecord.DB_FIELD_ROADCODE);
        joinHandles.put("businessKey", ParticipantRecord.DB_FIELD_BUSINESS_KEY);
        joinHandles.put("CompanyParticipantRelationRecord", ParticipantRecord.DB_FIELD_COMPANY_RELATION + BaseQuery.separator + CompanyParticipantRelationRecord.DB_FIELD_COMPANY_RELATION + BaseQuery.separator + RelationParticipantRecord.DB_FIELD_UNITNUMBER);
        joinHandles.put("cvrNumber", ParticipantRecord.DB_FIELD_COMPANY_RELATION + BaseQuery.separator + CompanyParticipantRelationRecord.DB_FIELD_COMPANY_RELATION + BaseQuery.separator + "cvrNumber");
        joinHandles.put("companyNames", ParticipantRecord.DB_FIELD_COMPANY_RELATION + BaseQuery.separator + CompanyParticipantRelationRecord.DB_FIELD_COMPANY_RELATION + BaseQuery.separator + "names" + BaseQuery.separator + "name");
        joinHandles.put("companyStatus", ParticipantRecord.DB_FIELD_COMPANY_RELATION + BaseQuery.separator + CompanyParticipantRelationRecord.DB_FIELD_COMPANY_RELATION + BaseQuery.separator + "companyStatus" + BaseQuery.separator + "status");
    }

    @Override
    protected Map<String, String> joinHandles() {
        return joinHandles;
    }

    protected void setupConditions() throws QueryBuildException {
        this.addCondition("unit", this.enhedsNummer, Long.class);
        this.addCondition("name", this.navn);
        this.addCondition("municipalitycode", this.kommunekode, Integer.class);
        this.addCondition("roadcode", this.vejkode, Integer.class);
        this.addCondition("municipalitycode", this.getKommunekodeRestriction(), Integer.class);
        this.addCondition("businessKey", this.getBusinessKey(), Long.class);
        this.addCondition("cvrNumber", this.getCvrnumber(), Long.class);
        this.addCondition("names", this.getCvrnumber(), Long.class);
        this.addCondition("companyNames", this.getCompanyNames(), String.class);
        this.addCondition("companyStatus", this.getStatuses(), String.class);
    }
}

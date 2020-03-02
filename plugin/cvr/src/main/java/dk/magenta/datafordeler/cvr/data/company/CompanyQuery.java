package dk.magenta.datafordeler.cvr.data.company;

import dk.magenta.datafordeler.core.database.LookupDefinition;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.cvr.data.CvrQuery;
import dk.magenta.datafordeler.cvr.data.shared.AddressData;
import dk.magenta.datafordeler.cvr.data.shared.BooleanData;
import dk.magenta.datafordeler.cvr.data.shared.ContactData;
import dk.magenta.datafordeler.cvr.data.unversioned.Address;
import dk.magenta.datafordeler.cvr.data.unversioned.CompanyForm;
import dk.magenta.datafordeler.cvr.data.unversioned.Municipality;

import java.util.*;

/**
 * Container for a query for Companies, defining fields and database lookup
 */
public class CompanyQuery extends CvrQuery<CompanyEntity> {

    public static final String CVRNUMMER = CompanyEntity.IO_FIELD_CVR;
    public static final String REKLAMEBESKYTTELSE = CompanyBaseData.IO_FIELD_ADVERTPROTECTION;
    public static final String NAVN = CompanyBaseData.IO_FIELD_NAME;
    public static final String TELEFONNUMMER = CompanyBaseData.IO_FIELD_PHONENUMBER;
    public static final String TELEFAXNUMMER = CompanyBaseData.IO_FIELD_FAXNUMBER;
    public static final String EMAILADRESSE = CompanyBaseData.IO_FIELD_EMAIL;
    public static final String VIRKSOMHEDSFORM = CompanyBaseData.IO_FIELD_FORM;
    public static final String KOMMUNEKODE = Municipality.IO_FIELD_CODE;

    @QueryField(type = QueryField.FieldType.STRING, queryName = CVRNUMMER)
    private List<String> cvrNumre = new ArrayList<>();

    public Collection<String> getCvrNumre() {
        return cvrNumre;
    }

    public void addCvrNummer(String cvrnummer) {
        if (cvrnummer != null) {
            this.cvrNumre.add(cvrnummer);
            this.increaseDataParamCount();
        }
    }

    public void setCvrNumre(String cvrNumre) {
        this.cvrNumre.clear();
        this.addCvrNummer(cvrNumre);
    }

    public void setCvrNumre(Collection<String> cvrNumre) {
        this.cvrNumre.clear();
        if (cvrNumre != null) {
            for (String cvrNummer : cvrNumre) {
                this.addCvrNummer(cvrNummer);
            }
        }
    }

    public void clearCvrNumre() {
        this.cvrNumre.clear();
    }


    @QueryField(type = QueryField.FieldType.STRING, queryName = REKLAMEBESKYTTELSE)
    private String reklamebeskyttelse;

    public String getReklamebeskyttelse() {
        return this.reklamebeskyttelse;
    }

    public void setReklamebeskyttelse(String reklamebeskyttelse) {
        this.reklamebeskyttelse = reklamebeskyttelse;
        if (reklamebeskyttelse != null) {
            this.increaseDataParamCount();
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
            this.increaseDataParamCount();
        }
    }

    public void setVirksomhedsnavn(String virksomhedsnavn) {
        this.virksomhedsnavn.clear();
        this.addVirksomhedsnavn(virksomhedsnavn);
    }

    public void setVirksomhedsnavn(Collection<String> virksomhedsnavne) {
        this.virksomhedsnavn.clear();
        if (virksomhedsnavne != null) {
            for (String virksomhedsnavn : virksomhedsnavne) {
                this.addVirksomhedsnavn(virksomhedsnavn);
            }
        }
    }

    public void clearVirksomhedsnavn() {
        this.virksomhedsnavn.clear();
    }



    @QueryField(type = QueryField.FieldType.STRING, queryName = TELEFONNUMMER)
    private List<String> telefonnummer = new ArrayList<>();

    public List<String> getTelefonnummer() {
        return this.telefonnummer;
    }

    public void addTelefonnummer(String telefonnummer) {
        if (telefonnummer != null) {
            this.telefonnummer.add(telefonnummer);
            this.increaseDataParamCount();
        }
    }

    public void setTelefonnummer(String telefonnummer) {
        this.telefonnummer.clear();
        this.addTelefonnummer(telefonnummer);
    }

    public void setTelefonnummer(Collection<String> telefonnumre) {
        this.telefonnummer.clear();
        if (telefonnumre != null) {
            for (String telefonnummer : telefonnumre) {
                this.addTelefonnummer(telefonnummer);
            }
        }
    }

    public void clearTelefonnummer() {
        this.telefonnummer.clear();
    }



    @QueryField(type = QueryField.FieldType.STRING, queryName = TELEFAXNUMMER)
    private List<String> telefaxnummer = new ArrayList<>();

    public List<String> getTelefaxnummer() {
        return this.telefaxnummer;
    }

    public void addTelefaxnummer(String telefaxnummer) {
        if (telefaxnummer != null) {
            this.telefaxnummer.add(telefaxnummer);
            this.increaseDataParamCount();
        }
    }

    public void setTelefaxnummer(String telefaxnummer) {
        this.telefaxnummer.clear();
        this.addTelefaxnummer(telefaxnummer);
    }

    public void setTelefaxnummer(Collection<String> telefaxnumre) {
        this.telefaxnummer.clear();
        if (telefaxnumre != null) {
            for (String telefaxnummer : telefaxnumre) {
                this.addTelefaxnummer(telefaxnummer);
            }
        }
    }

    public void clearTelefaxnummer() {
        this.telefaxnummer.clear();
    }



    @QueryField(type = QueryField.FieldType.STRING, queryName = EMAILADRESSE)
    private List<String> emailadresse = new ArrayList<>();

    public List<String> getEmailadresse() {
        return this.emailadresse;
    }

    public void addEmailadresse(String emailadresse) {
        if (emailadresse != null) {
            this.emailadresse.add(emailadresse);
            this.increaseDataParamCount();
        }
    }

    public void setEmailadresse(String emailadresse) {
        this.emailadresse.clear();
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
    }



    @QueryField(type = QueryField.FieldType.STRING, queryName = VIRKSOMHEDSFORM)
    private List<String> virksomhedsform = new ArrayList<>();

    public List<String> getVirksomhedsform() {
        return this.virksomhedsform;
    }

    public void addVirksomhedsform(String virksomhedsform) {
        if (virksomhedsform != null) {
            this.virksomhedsform.add(virksomhedsform);
            this.increaseDataParamCount();
        }
    }

    public void addVirksomhedsform(int virksomhedsform) {
        this.addVirksomhedsform(Integer.toString(virksomhedsform));
    }

    public void setVirksomhedsform(String virksomhedsform) {
        this.virksomhedsform.clear();
        this.addVirksomhedsform(virksomhedsform);
    }

    public void setVirksomhedsform(int virksomhedsform) {
        this.setVirksomhedsform(Integer.toString(virksomhedsform));
    }

    public void setVirksomhedsform(Collection<String> virksomhedsformer) {
        this.virksomhedsform.clear();
        if (virksomhedsformer != null) {
            for (String virksomhedsform : virksomhedsformer) {
                this.addVirksomhedsform(virksomhedsform);
            }
        }
    }

    public void clearVirksomhedsform() {
        this.virksomhedsform.clear();
    }



    @QueryField(type = QueryField.FieldType.STRING, queryName = KOMMUNEKODE)
    private List<String> kommunekode = new ArrayList<>();

    public Collection<String> getKommuneKode() {
        return this.kommunekode;
    }

    public void addKommuneKode(String kommunekode) {
        this.kommunekode.add(kommunekode);
        if (kommunekode != null) {
            this.kommunekode.add(kommunekode);
            this.increaseDataParamCount();
        }
    }

    public void addKommuneKode(int kommunekode) {
        this.addKommuneKode(String.format("%03d", kommunekode));
    }

    public void setKommunekode(String kommunekode) {
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

    public void setKommuneKode(int kommunekode) {
        this.setKommunekode(String.format("%03d", kommunekode));
    }

    public void clearKommuneKoder() {
        this.kommunekode.clear();
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
        return map;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) {
        this.setCvrNumre(parameters.get(CVRNUMMER));
        this.setReklamebeskyttelse(parameters.getFirst(REKLAMEBESKYTTELSE));
        this.setVirksomhedsnavn(parameters.getI(NAVN));
        this.setTelefonnummer(parameters.getI(TELEFONNUMMER));
        this.setTelefaxnummer(parameters.getI(TELEFAXNUMMER));
        this.setEmailadresse(parameters.getI(EMAILADRESSE));
        this.setVirksomhedsform(parameters.getI(VIRKSOMHEDSFORM));
        this.setKommuneKode(parameters.getI(KOMMUNEKODE));
    }

    @Override
    public Class<CompanyEntity> getEntityClass() {
        return CompanyEntity.class;
    }

    @Override
    public Class getDataClass() {
        return CompanyBaseData.class;
    }


    @Override
    public LookupDefinition getLookupDefinition() {
        LookupDefinition lookupDefinition = super.getLookupDefinition();

        if (!this.cvrNumre.isEmpty()) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator + CompanyEntity.DB_FIELD_CVR, this.cvrNumre, Integer.class);
        }
        if (this.virksomhedsform != null && !this.virksomhedsform.isEmpty()) {
            lookupDefinition.put(CompanyBaseData.DB_FIELD_FORM + LookupDefinition.separator + CompanyFormData.DB_FIELD_FORM + LookupDefinition.separator + CompanyForm.DB_FIELD_CODE, this.virksomhedsform, String.class);
        }
        if (this.reklamebeskyttelse != null) {
            lookupDefinition.put(CompanyBaseData.DB_FIELD_ADVERTPROTECTION + LookupDefinition.separator + BooleanData.DB_FIELD_VALUE, this.reklamebeskyttelse, Boolean.class);
        }
        if (this.virksomhedsnavn != null && !this.virksomhedsnavn.isEmpty()) {
            lookupDefinition.put(CompanyBaseData.DB_FIELD_NAME, this.virksomhedsnavn, String.class);
        }
        if (this.telefonnummer != null && !this.telefonnummer.isEmpty()) {
            lookupDefinition.put(CompanyBaseData.DB_FIELD_PHONENUMBER + LookupDefinition.separator + ContactData.DB_FIELD_VALUE, this.telefonnummer, String.class);
        }
        if (this.telefaxnummer != null && !this.telefaxnummer.isEmpty()) {
            lookupDefinition.put(CompanyBaseData.DB_FIELD_FAXNUMBER + LookupDefinition.separator + ContactData.DB_FIELD_VALUE, this.telefaxnummer, String.class);
        }
        if (this.emailadresse != null && !this.emailadresse.isEmpty()) {
            lookupDefinition.put(CompanyBaseData.DB_FIELD_EMAIL + LookupDefinition.separator + ContactData.DB_FIELD_VALUE, this.emailadresse, String.class);
        }
        if (this.kommunekode != null && !this.kommunekode.isEmpty()) {
            StringJoiner sj = new StringJoiner(LookupDefinition.separator);
            sj.add(CompanyBaseData.DB_FIELD_LOCATION_ADDRESS);
            sj.add(AddressData.DB_FIELD_ADDRESS);
            sj.add(Address.DB_FIELD_MUNICIPALITY);
            sj.add(Municipality.DB_FIELD_CODE);
            lookupDefinition.put(sj.toString(), this.kommunekode, Integer.class);
        }

        if (this.getKommunekodeRestriction() != null && !this.getKommunekodeRestriction().isEmpty()) {
            StringJoiner sj = new StringJoiner(LookupDefinition.separator);
            sj.add(CompanyBaseData.DB_FIELD_LOCATION_ADDRESS);
            sj.add(AddressData.DB_FIELD_ADDRESS);
            sj.add(Address.DB_FIELD_MUNICIPALITY);
            sj.add(Municipality.DB_FIELD_CODE);
            lookupDefinition.put(sj.toString(), this.getKommunekodeRestriction(), Integer.class);
        }
        return lookupDefinition;
    }
}

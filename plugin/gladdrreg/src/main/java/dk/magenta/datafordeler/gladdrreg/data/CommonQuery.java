package dk.magenta.datafordeler.gladdrreg.data;

import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.database.LookupDefinition;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.Query;
import dk.magenta.datafordeler.core.fapi.QueryField;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lars on 19-05-17.
 */
public abstract class CommonQuery<E extends Entity> extends Query<E> {

    public static final String ACTIVE = "active";
    public static final String NOTE = "note";
    public static final String REGISTRATION_USER = "registration_user";
    public static final String STATE = "state";

    // Contain all fields that are searchable

    @QueryField(type = QueryField.FieldType.BOOLEAN, queryName = ACTIVE)
    private Boolean active;

    @QueryField(type = QueryField.FieldType.STRING, queryName = NOTE)
    private String note;

    @QueryField(type = QueryField.FieldType.STRING, queryName = REGISTRATION_USER)
    private String registration_user;

    @QueryField(type = QueryField.FieldType.STRING, queryName = STATE)
    private String state;

    public boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
        if (active != null) {
            this.increaseDataParamCount();
        }
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
        if (note != null) {
            this.increaseDataParamCount();
        }
    }

    public String getRegistration_user() {
        return registration_user;
    }

    public void setRegistration_user(String registration_user) {
        this.registration_user = registration_user;
        if (registration_user != null) {
            this.increaseDataParamCount();
        }
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
        if (state != null) {
            this.increaseDataParamCount();
        }
    }

    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("active", this.active);
        map.put("note", this.note);
        map.put("registration_user", this.registration_user);
        map.put("state", this.state);
        return map;
    }

    @Override
    public LookupDefinition getLookupDefinition() {
        LookupDefinition lookupDefinition = super.getLookupDefinition();
        if (this.active != null) {
            lookupDefinition.put("active", this.active, Boolean.class);
        }
        if (this.note != null) {
            lookupDefinition.put("note", this.note, String.class);
        }
        if (this.registration_user != null) {
            lookupDefinition.put("registration_user", this.registration_user, String.class);
        }
        if (this.state != null) {
            lookupDefinition.put("state", this.state, String.class);
        }
        return lookupDefinition;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) {
        this.setActive(Query.booleanFromString(parameters.getFirstI(ACTIVE)));
        this.setNote(parameters.getFirstI(NOTE));
        this.setRegistration_user(parameters.getFirstI(REGISTRATION_USER));
        this.setState(parameters.getFirstI(STATE));
    }

}

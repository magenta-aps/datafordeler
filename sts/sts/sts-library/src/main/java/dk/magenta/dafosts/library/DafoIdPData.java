package dk.magenta.dafosts.library;

import org.opensaml.saml2.metadata.EntityDescriptor;

import java.time.LocalDateTime;

public class DafoIdPData {
    private int databaseId;
    private int status;
    private LocalDateTime updated;
    private String name;
    String entityId;
    private int idpType;
    private String metadataXml;
    private String userprofileAttribute;
    private int userprofileAttributeFormat;
    private int userprofileAdjustmentFilterType;
    private String userprofileAdjustmentFilterValue;

    public DafoIdPData(
            int databaseId, int status, LocalDateTime updated, String name, String entityId, int idpType,
            String metadataXml, String userprofileAttribute, int userprofileAttributeFormat,
            int userprofileAdjustmentFilterType, String userprofileAdjustmentFilterValue
    ) {
        this.databaseId = databaseId;
        this.status = status;
        this.updated = updated;
        this.name = name;
        this.entityId = entityId;
        this.idpType = idpType;
        this.metadataXml = metadataXml;
        this.userprofileAttribute = userprofileAttribute;
        this.userprofileAttributeFormat = userprofileAttributeFormat;
        this.userprofileAdjustmentFilterType = userprofileAdjustmentFilterType;
        this.userprofileAdjustmentFilterValue = userprofileAdjustmentFilterValue;
    }

    public int getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(int databaseId) {
        this.databaseId = databaseId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public int getIdpType() {
        return idpType;
    }

    public void setIdpType(int idpType) {
        this.idpType = idpType;
    }

    public String getMetadataXml() {
        return metadataXml;
    }

    public void setMetadataXml(String metadataXml) {
        this.metadataXml = metadataXml;
    }

    public String getUserprofileAttribute() {
        return userprofileAttribute;
    }

    public void setUserprofileAttribute(String userprofileAttribute) {
        this.userprofileAttribute = userprofileAttribute;
    }

    public int getUserprofileAttributeFormat() {
        return userprofileAttributeFormat;
    }

    public void setUserprofileAttributeFormat(int userprofileAttributeFormat) {
        this.userprofileAttributeFormat = userprofileAttributeFormat;
    }

    public int getUserprofileAdjustmentFilterType() {
        return userprofileAdjustmentFilterType;
    }

    public void setUserprofileAdjustmentFilterType(int userprofileAdjustmentFilterType) {
        this.userprofileAdjustmentFilterType = userprofileAdjustmentFilterType;
    }

    public String getUserprofileAdjustmentFilterValue() {
        return userprofileAdjustmentFilterValue;
    }

    public void setUserprofileAdjustmentFilterValue(String userprofileAdjustmentFilterValue) {
        this.userprofileAdjustmentFilterValue = userprofileAdjustmentFilterValue;
    }
}

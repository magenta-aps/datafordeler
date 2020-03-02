package dk.magenta.datafordeler.gladdrreg.data.locality;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.gladdrreg.data.GladdrregOutputWrapper;

public class LocalityOutputWrapper extends GladdrregOutputWrapper<LocalityEntity, LocalityRegistration, LocalityEffect, LocalityData> {

    @Override
    public void wrapData(ObjectNode objectNode, LocalityData dataItem) {
        super.wrapData(objectNode, dataItem);
        this.put(objectNode, LocalityData.IO_FIELD_CODE, dataItem.getCode());
        this.put(objectNode, LocalityData.IO_FIELD_TYPE, dataItem.getType());
        this.put(objectNode, LocalityData.IO_FIELD_STATE, dataItem.getLocalityState());
        this.put(objectNode, LocalityData.IO_FIELD_ABBREV, dataItem.getAbbrev());
        this.put(objectNode, LocalityData.IO_FIELD_NAME, dataItem.getName());
        this.put(objectNode, LocalityData.IO_FIELD_DISTRICT, dataItem.getDistrict());
        this.put(objectNode, LocalityData.IO_FIELD_MUNICIPALITY, dataItem.getMunicipality());
        this.put(objectNode, LocalityData.IO_FIELD_POSTALCODE, dataItem.getPostalCode());
    }

}

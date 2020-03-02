package dk.magenta.datafordeler.gladdrreg.data.municipality;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.gladdrreg.data.GladdrregOutputWrapper;

public class MunicipalityOutputWrapper extends GladdrregOutputWrapper<MunicipalityEntity, MunicipalityRegistration, MunicipalityEffect, MunicipalityData> {

    @Override
    public void wrapData(ObjectNode objectNode, MunicipalityData dataItem) {
        super.wrapData(objectNode, dataItem);
        this.put(objectNode, MunicipalityData.IO_FIELD_CODE, dataItem.getCode());
        this.put(objectNode, MunicipalityData.IO_FIELD_ABBREV, dataItem.getAbbrev());
        this.put(objectNode, MunicipalityData.IO_FIELD_NAME, dataItem.getName());
    }

}

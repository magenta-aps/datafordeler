package dk.magenta.datafordeler.gladdrreg.data.district;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.gladdrreg.data.GladdrregOutputWrapper;

public class DistrictOutputWrapper extends GladdrregOutputWrapper<DistrictEntity, DistrictRegistration, DistrictEffect, DistrictData> {

    @Override
    public void wrapData(ObjectNode objectNode, DistrictData dataItem) {
        super.wrapData(objectNode, dataItem);
        this.put(objectNode, DistrictData.IO_FIELD_CODE, dataItem.getCode());
        this.put(objectNode, DistrictData.IO_FIELD_ABBREV, dataItem.getAbbrev());
        this.put(objectNode, DistrictData.IO_FIELD_NAME, dataItem.getName());
    }
}

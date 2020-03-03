package dk.magenta.datafordeler.gladdrreg.data.road;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.gladdrreg.data.GladdrregOutputWrapper;

public class RoadOutputWrapper extends GladdrregOutputWrapper<RoadEntity, RoadRegistration, RoadEffect, RoadData> {

    @Override
    public void wrapData(ObjectNode objectNode, RoadData dataItem) {
        super.wrapData(objectNode, dataItem);
        this.put(objectNode, RoadData.IO_FIELD_CODE, dataItem.getCode());
        this.put(objectNode, RoadData.IO_FIELD_NAME, dataItem.getName());
        this.put(objectNode, RoadData.IO_FIELD_CPRNAME, dataItem.getCprName());
        this.put(objectNode, RoadData.IO_FIELD_SHORTNAME, dataItem.getShortName());
        this.put(objectNode, RoadData.IO_FIELD_ALTNAME, dataItem.getAlternateName());
        this.put(objectNode, RoadData.IO_FIELD_LOCATION, dataItem.getLocation());
        this.put(objectNode, RoadData.IO_FIELD_MUNICIPALITY, dataItem.getMunicipality());
    }

}

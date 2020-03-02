package dk.magenta.datafordeler.gladdrreg.data.bnumber;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.gladdrreg.data.GladdrregOutputWrapper;

public class BNumberOutputWrapper extends GladdrregOutputWrapper<BNumberEntity, BNumberRegistration, BNumberEffect, BNumberData> {

    @Override
    public void wrapData(ObjectNode objectNode, BNumberData dataItem) {
        super.wrapData(objectNode, dataItem);
        this.put(objectNode, BNumberData.IO_FIELD_CODE, dataItem.getCode());
        this.put(objectNode, BNumberData.IO_FIELD_TYPE, dataItem.getType());
        this.put(objectNode, BNumberData.IO_FIELD_CALLNAME, dataItem.getCallname());
        this.put(objectNode, BNumberData.IO_FIELD_LOCATION, dataItem.getLocation());
        this.put(objectNode, BNumberData.IO_FIELD_MUNICIPALITY, dataItem.getMunicipality());
    }

}

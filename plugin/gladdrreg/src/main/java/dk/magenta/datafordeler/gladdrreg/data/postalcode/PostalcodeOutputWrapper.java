package dk.magenta.datafordeler.gladdrreg.data.postalcode;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.gladdrreg.data.GladdrregOutputWrapper;

public class PostalcodeOutputWrapper extends GladdrregOutputWrapper<PostalCodeEntity, PostalCodeRegistration, PostalCodeEffect, PostalCodeData> {

    @Override
    public void wrapData(ObjectNode objectNode, PostalCodeData dataItem) {
        super.wrapData(objectNode, dataItem);
        this.put(objectNode, PostalCodeData.IO_FIELD_CODE, dataItem.getCode());
        this.put(objectNode, PostalCodeData.IO_FIELD_NAME, dataItem.getName());
    }

}

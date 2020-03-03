package dk.magenta.datafordeler.gladdrreg.data.state;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.gladdrreg.data.GladdrregOutputWrapper;

public class StateOutputWrapper extends GladdrregOutputWrapper<StateEntity, StateRegistration, StateEffect, StateData> {

    @Override
    public void wrapData(ObjectNode objectNode, StateData dataItem) {
        super.wrapData(objectNode, dataItem);
        this.put(objectNode, StateData.IO_FIELD_CODE, dataItem.getCode());
        this.put(objectNode, StateData.IO_FIELD_NAME, dataItem.getName());
        this.put(objectNode, StateData.IO_FIELD_DESCRIPTION, dataItem.getDescription());
    }

}

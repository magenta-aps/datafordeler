package dk.magenta.datafordeler.gladdrreg.data.address;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.gladdrreg.data.GladdrregOutputWrapper;

public class AddressOutputWrapper extends GladdrregOutputWrapper<AddressEntity, AddressRegistration, AddressEffect, AddressData> {

    @Override
    public void wrapData(ObjectNode objectNode, AddressData dataItem) {
        super.wrapData(objectNode, dataItem);
        this.put(objectNode, AddressData.IO_FIELD_HOUSENUMBER, dataItem.getHouseNumber());
        this.put(objectNode, AddressData.IO_FIELD_FLOOR, dataItem.getFloor());
        this.put(objectNode, AddressData.IO_FIELD_ROOM, dataItem.getRoom());
        this.put(objectNode, AddressData.IO_FIELD_BNUMBER, dataItem.getbNumber());
        this.put(objectNode, AddressData.IO_FIELD_MUNICIPALITY, dataItem.getMunicipality());
        this.put(objectNode, AddressData.IO_FIELD_ROAD, dataItem.getRoad());
        this.put(objectNode, AddressData.IO_FIELD_RESIDENCE, dataItem.getResidence());
    }

}

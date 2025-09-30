package dk.magenta.datafordeler.geo.data.unitaddress;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.geo.data.SumiffiikRawData;
import dk.magenta.datafordeler.geo.data.common.GeoMonotemporalRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UnitAddressRawData extends SumiffiikRawData {

    public Map<String, Integer> usageMap = Map.of(
            "Ukendt", 0,
            "Boligenhed", 1,
            "Erhvervsenhed", 2,
            "Liberal erhvervsenhed", 3,
            "Teknikenhed", 4
    );

    protected Logger log = LogManager.getLogger(this.getClass().getCanonicalName());

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class UnitAddressRawProperties extends RawProperties {

        @JsonProperty("access_adress_id")
        public String accessAddressSumiffiik;

        @JsonProperty("Enhedsnummer")
        public String unitNumber;

        @JsonProperty("Enhedsanvendelse")
        public String usage;

        @JsonProperty("Etage")
        public String floor;

        //This attribute is becomming deprecated
        @JsonProperty("Dor_lejlighedsnummer")
        public String door;

        @JsonProperty("Objektstatus")
        public Integer objectStatus;

        @JsonProperty("Datakilde")
        public Integer source;

        public UUID getAccessAddressSumiffiikAsUUID() {
            return SumiffiikRawData.getSumiffiikAsUUID(this.accessAddressSumiffiik);
        }

        @JsonProperty("EnhedsadresseSumiffik")
        public void setSumiffiikId(String sumiffiikId) {
            this.sumiffiikId = sumiffiikId;
        }

    }

    @JsonProperty("properties")
    public UnitAddressRawProperties properties;

    @JsonProperty("attributes")
    public void setAttributes(UnitAddressRawProperties attributes) {
        this.properties = attributes;
    }

    @Override
    public List<GeoMonotemporalRecord> getMonotemporalRecords() {
        ArrayList<GeoMonotemporalRecord> records = new ArrayList<>();
        records.add(
                new UnitAddressFloorRecord(this.properties.floor)
        );
        records.add(
                new UnitAddressDoorRecord(this.properties.door)
        );
        if (usageMap.containsKey(this.properties.usage)) {
            records.add(
                    new UnitAddressUsageRecord(usageMap.get(this.properties.usage))
            );
        } else {
            log.error("Unknown usage: " + this.properties.usage + " at objectId "+this.properties.objectId);
        }
        records.add(
                new UnitAddressNumberRecord(this.properties.unitNumber)
        );
        records.add(
                new UnitAddressStatusRecord(this.properties.objectStatus)
        );
        /*records.add(
                new UnitAddressImportRecord(this.properties.importComplete)
        );*/
        records.add(
                new UnitAddressSourceRecord(this.properties.source)
        );

        for (GeoMonotemporalRecord record : records) {
            record.setEditor(this.properties.editor);
            record.setRegistrationFrom(this.properties.editDate);
        }

        return records;
    }

    @Override
    public RawProperties getProperties() {
        return this.properties;
    }

}

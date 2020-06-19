package dk.magenta.datafordeler.cpr.records.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.JsonModifier;
import dk.magenta.datafordeler.core.fapi.ResultSet;
import dk.magenta.datafordeler.core.util.Bitemporality;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.person.data.AddressDataRecord;
import dk.magenta.datafordeler.cpr.records.road.data.RoadEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RoadRecordOutputWrapper extends CprRecordOutputWrapper<RoadEntity> {

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    private void register() {
        this.register(RoadEntity.class);
    }

    @Override
    public ObjectMapper getObjectMapper() {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return this.objectMapper;
    }

    @Override
    protected JsonModifier getModifier(ResultSet resultSet) {
        return new RoadOutputJsonModifier(this, resultSet);
    }

    @Override
    public Object wrapResult(RoadEntity record, BaseQuery query, Mode mode) {
        CprBitemporality mustContain = new CprBitemporality(query.getRegistrationFrom(), query.getRegistrationTo(), query.getEffectFrom(), query.getEffectTo());
        return this.getNode(record, mustContain, mode);
    }

    @Override
    protected void fillContainer(OutputContainer container, RoadEntity record, Mode mode) {
        container.addNontemporal(RoadEntity.IO_FIELD_MUNIPALITY_CODE, record.getMunicipalityCode());
        container.addNontemporal(RoadEntity.IO_FIELD_ROAD_CODE, record.getRoadcode());
        container.addBitemporal(RoadEntity.IO_FIELD_POST_CODE, record.getPostcode(), true);
        container.addBitemporal(RoadEntity.IO_FIELD_CITY_CODE, record.getCity());
        container.addBitemporal(RoadEntity.IO_FIELD_MEMO_CODE, record.getMemo());
    }

    @Override
    protected ObjectNode fallbackOutput(Mode mode, OutputContainer outputContainer, Bitemporality bitemporality) {
        return null;
    }

}

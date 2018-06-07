package dk.magenta.datafordeler.cpr.data.residence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.fapi.OutputWrapper;
import dk.magenta.datafordeler.core.fapi.Query;
import dk.magenta.datafordeler.core.util.ListHashMap;
import dk.magenta.datafordeler.core.util.OffsetDateTimeAdapter;
import dk.magenta.datafordeler.cpr.data.residence.ResidenceEffect;
import dk.magenta.datafordeler.cpr.data.residence.ResidenceEntity;
import dk.magenta.datafordeler.cpr.data.residence.ResidenceRegistration;
import dk.magenta.datafordeler.cpr.data.residence.data.*;
import dk.magenta.datafordeler.cpr.records.Bitemporality;
import dk.magenta.datafordeler.cpr.records.BitemporalityComparator;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ResidenceOutputWrapper extends OutputWrapper<ResidenceEntity> {

    private ObjectMapper objectMapper;

    @Override
    public Object wrapResult(ResidenceEntity input, Query query) {
        objectMapper = new ObjectMapper();
        ObjectNode root = objectMapper.createObjectNode();
        root.put(ResidenceEntity.IO_FIELD_UUID, input.getUUID().toString());
        root.put(ResidenceEntity.IO_FIELD_DOMAIN, input.getDomain());
        Bitemporality overlap = new Bitemporality(query.getRegistrationFrom(), query.getRegistrationTo(), query.getEffectFrom(), query.getEffectTo());
        ArrayNode registreringer = this.getRegistrations(input, overlap);
        root.set(ResidenceEntity.IO_FIELD_REGISTRATIONS, registreringer);
        return root;
    }

    public ArrayNode getRegistrations(ResidenceEntity entity, Bitemporality mustOverlap) {

        ArrayNode registrationsNode = objectMapper.createArrayNode();
        //HashMap<Bitemporality, ObjectNode> data = new HashMap<>();
        ListHashMap<Bitemporality, ResidenceBaseData> data = new ListHashMap<>();

        // Populér map med bitemp -> json
        // Loop over alle registrationBorders
        //     apply data i rækkefølge (sorteret efter bitemp)

        for (ResidenceRegistration registration : entity.getRegistrations()) {
            for (ResidenceEffect virkning : registration.getEffects()) {
                //ObjectNode dataPiece = objectMapper.createObjectNode();
                Bitemporality bitemporality = new Bitemporality(registration.getRegistrationFrom(), registration.getRegistrationTo(), virkning.getEffectFrom(), virkning.getEffectTo());
                for (ResidenceBaseData residenceBaseData : virkning.getDataItems()) {
                    data.add(bitemporality, residenceBaseData);
                }
            }
        }

        ListHashMap<OffsetDateTime, Bitemporality> startTerminators = new ListHashMap<>();
        ListHashMap<OffsetDateTime, Bitemporality> endTerminators = new ListHashMap<>();

        for (Bitemporality bitemporality : data.keySet()) {
            startTerminators.add(bitemporality.registrationFrom, bitemporality);
            endTerminators.add(bitemporality.registrationTo, bitemporality);
        }

        HashSet<OffsetDateTime> allTerminators = new HashSet<>();
        allTerminators.addAll(startTerminators.keySet());
        allTerminators.addAll(endTerminators.keySet());
        // Create a sorted list of all timestamps where Bitemporalities either begin or end
        ArrayList<OffsetDateTime> terminators = new ArrayList<>(allTerminators);
        terminators.sort(Comparator.nullsFirst(OffsetDateTime::compareTo));
        terminators.add(null);

        HashSet<Bitemporality> presentBitemporalities = new HashSet<>();

        for (int i = 0; i < terminators.size(); i++) {
            OffsetDateTime t = terminators.get(i);
            List<Bitemporality> startingHere = startTerminators.get(t);
            List<Bitemporality> endingHere = (t != null) ? endTerminators.get(t) : null;
            if (startingHere != null) {
                presentBitemporalities.addAll(startingHere);
            }
            if (endingHere != null) {
                presentBitemporalities.removeAll(endingHere);
            }
            if (i < terminators.size() - 1) {
                OffsetDateTime next = terminators.get(i + 1);
                if (!presentBitemporalities.isEmpty()) {
                    if (mustOverlap == null || mustOverlap.overlapsRegistration(t, next)) {
                        ObjectNode registrationNode = objectMapper.createObjectNode();
                        registrationsNode.add(registrationNode);
                        registrationNode.put("registreringFra", formatTime(t));
                        registrationNode.put("registreringTil", formatTime(next));

                        ArrayList<Bitemporality> sortedBitemporalities = new ArrayList<>(presentBitemporalities);
                        sortedBitemporalities.sort(effectComparator);

                        for (Bitemporality bitemporality : sortedBitemporalities) {

                            ArrayList<ResidenceBaseData> dataItems = data.get(bitemporality);

                            for (ResidenceBaseData dataItem : dataItems) {
                                OffsetDateTime timestamp = dataItem.getLastUpdated();

                                ObjectNode dataNode = this.createDataNode(bitemporality, timestamp);

                                dataNode.put(ResidenceBaseData.IO_FIELD_MUNICIPALITY_CODE, dataItem.getKommunekode());
                                dataNode.put(ResidenceBaseData.IO_FIELD_ROAD_CODE, dataItem.getVejkode());
                                dataNode.put(ResidenceBaseData.IO_FIELD_HOUSENUMBER, dataItem.getHusnummer());
                                dataNode.put(ResidenceBaseData.IO_FIELD_FLOOR, dataItem.getEtage());
                                dataNode.put(ResidenceBaseData.IO_FIELD_DOOR, dataItem.getSideDoer());

                                addEffectDataToRegistration(registrationNode, "data", dataNode);
                            }
                        }
                    }
                }
            }
        }
        return registrationsNode;
    }

    protected static final Comparator<Bitemporality> effectComparator =
            Comparator.nullsFirst(new BitemporalityComparator(BitemporalityComparator.Type.EFFECT_FROM))
                    .thenComparing(Comparator.nullsLast(new BitemporalityComparator(BitemporalityComparator.Type.EFFECT_TO)));


    protected static String formatTime(OffsetDateTime time) {
        return formatTime(time, false);
    }

    protected static String formatTime(OffsetDateTime time, boolean asDateOnly) {
        if (time == null) return null;
        return time.format(asDateOnly ? DateTimeFormatter.ISO_LOCAL_DATE : DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    protected static String formatTime(LocalDate time) {
        if (time == null) return null;
        return time.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    protected void addEffectDataToRegistration(ObjectNode output, String key, ObjectNode value) {
        if (!output.has(key) || output.get(key).isNull()) {
            output.set(key, objectMapper.createArrayNode());
        }
        ((ArrayNode) output.get(key)).add(value);
    }

    protected ObjectNode createDataNode(Bitemporality bitemporality, OffsetDateTime lastUpdated) {
        return createDataNode(bitemporality, true, lastUpdated);
    }

    protected ObjectNode createDataNode(Bitemporality bitemporality, boolean includeVirkningTil, OffsetDateTime lastUpdated) {
        ObjectNode output = objectMapper.createObjectNode();
        if (bitemporality != null) {
            output.put(
                    ResidenceEffect.IO_FIELD_EFFECT_FROM,
                    OffsetDateTimeAdapter.toString(bitemporality.effectFrom)
            );
            if (includeVirkningTil) {
                output.put(
                        ResidenceEffect.IO_FIELD_EFFECT_TO,
                        OffsetDateTimeAdapter.toString(bitemporality.effectTo)
                );
            }
        }
        output.put(
                ResidenceBaseData.IO_FIELD_LAST_UPDATED,
                lastUpdated != null ? lastUpdated.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null
        );
        return output;
    }
    
}

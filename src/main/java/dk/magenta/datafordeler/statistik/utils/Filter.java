package dk.magenta.datafordeler.statistik.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.fapi.Query;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.statistik.services.StatisticsService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

public class Filter {

    public OffsetDateTime effectAt;

    public OffsetDateTime after;

    public OffsetDateTime before;

    public OffsetDateTime registrationAfter;

    public OffsetDateTime registrationBefore;

    public LocalDate originAfter;

    public LocalDate originBefore;

    public OffsetDateTime livingInGreenlandAtDate;

    public List<String> onlyPnr;

    public Filter() {
    }


    public Filter(HttpServletRequest request) {
        this.registrationAfter = Query.parseDateTime(request.getParameter(StatisticsService.REGISTRATION_AFTER));
        this.registrationBefore = Query.parseDateTime(request.getParameter(StatisticsService.REGISTRATION_BEFORE));
        this.effectAt = Query.parseDateTime(request.getParameter(StatisticsService.EFFECT_DATE_PARAMETER));
        this.before = Query.parseDateTime(request.getParameter(StatisticsService.BEFORE_DATE_PARAMETER));
        this.after = Query.parseDateTime(request.getParameter(StatisticsService.AFTER_DATE_PARAMETER));
        this.originAfter = parseLocaldate(request.getParameter(StatisticsService.ORIGIN_AFTER));
        this.originBefore = parseLocaldate(request.getParameter(StatisticsService.ORIGIN_BEFORE));
        this.livingInGreenlandAtDate = Query.parseDateTime(request.getParameter(StatisticsService.INCLUSION_DATE_PARAMETER));
        String[] pnr = request.getParameterValues("pnr");
        if (pnr != null && pnr.length > 0) {
            this.onlyPnr = Arrays.asList(pnr);
        }
    }

    private static LocalDate parseLocaldate(String localdate) {
        if (localdate != null) {
            return LocalDate.parse(localdate);
        }
        return null;
    }

    public Filter(ObjectNode node) {
        if (node.has(StatisticsService.REGISTRATION_AFTER)) {
            this.registrationAfter = Query.parseDateTime(node.get(StatisticsService.REGISTRATION_AFTER).asText());
        }
        if (node.has(StatisticsService.REGISTRATION_BEFORE)) {
            this.registrationBefore = Query.parseDateTime(node.get(StatisticsService.REGISTRATION_BEFORE).asText());
        }
        if (node.has(StatisticsService.EFFECT_DATE_PARAMETER)) {
            this.effectAt = Query.parseDateTime(node.get(StatisticsService.EFFECT_DATE_PARAMETER).asText());
        }
        if (node.has(StatisticsService.BEFORE_DATE_PARAMETER)) {
            this.before = Query.parseDateTime(node.get(StatisticsService.BEFORE_DATE_PARAMETER).asText());
        }
        if (node.has(StatisticsService.AFTER_DATE_PARAMETER)) {
            this.after = Query.parseDateTime(node.get(StatisticsService.AFTER_DATE_PARAMETER).asText());
        }
        if (node.has(StatisticsService.ORIGIN_BEFORE)) {
            this.originBefore = LocalDate.parse(node.get(StatisticsService.ORIGIN_BEFORE).asText());
        }
        if (node.has(StatisticsService.ORIGIN_AFTER)) {
            this.originAfter = LocalDate.parse(node.get(StatisticsService.ORIGIN_AFTER).asText());
        }
        if (node.has(StatisticsService.INCLUSION_DATE_PARAMETER)) {
            this.livingInGreenlandAtDate = Query.parseDateTime(node.get(StatisticsService.INCLUSION_DATE_PARAMETER).asText());
        }
        if (node.has(StatisticsService.INCLUSION_DATE_PARAMETER)) {
            JsonNode pnr = node.get("pnr");
            if (pnr != null) {
                ArrayList<String> pnrs = new ArrayList<>();
                if (pnr.isArray()) {
                    ArrayNode pnrA = (ArrayNode) pnr;
                    for (JsonNode p : pnrA) {
                        if (p.isValueNode()) {
                            pnrs.add(p.asText());
                        }
                    }
                } else if (pnr.isValueNode()) {
                    pnrs.add(pnr.asText());
                }
                if (!pnrs.isEmpty()) {
                    this.onlyPnr = pnrs;
                }
            }
        }
    }

    public Filter(OffsetDateTime effectAt) {
        this.effectAt = effectAt;
    }

    public boolean accept(CprBitemporalRecord record) {
        if (this.after != null && record.getEffectFrom() != null && record.getEffectFrom().isBefore(this.after)) return false;
        if (this.before != null && record.getEffectFrom() != null && record.getEffectFrom().isAfter(this.before)) return false;
        if (this.registrationAfter != null && record.getRegistrationFrom() != null && record.getRegistrationFrom().isBefore(this.registrationAfter)) return false;
        if (this.registrationBefore != null && record.getRegistrationFrom() != null && record.getRegistrationFrom().isAfter(this.registrationBefore)) return false;
        if (this.originAfter != null && record.getOriginDate() != null && record.getOriginDate().isBefore(this.originAfter)) return false;
        if (this.originBefore != null && record.getOriginDate() != null && record.getOriginDate().isAfter(this.originBefore)) return false;
        return true;
    }
}

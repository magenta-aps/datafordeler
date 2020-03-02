package dk.magenta.datafordeler.prisme;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.database.Effect;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.OutputWrapper;
import dk.magenta.datafordeler.cvr.data.company.CompanyBaseData;
import dk.magenta.datafordeler.cvr.data.company.CompanyEffect;
import dk.magenta.datafordeler.cvr.data.company.CompanyEntity;
import dk.magenta.datafordeler.cvr.data.company.CompanyRegistration;
import dk.magenta.datafordeler.cvr.data.unversioned.Address;
import dk.magenta.datafordeler.cvr.data.unversioned.Industry;
import dk.magenta.datafordeler.cvr.data.unversioned.Municipality;
import dk.magenta.datafordeler.cvr.data.unversioned.PostCode;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class CompanyOutputWrapperPrisme extends OutputWrapper<CompanyEntity> {

    private ObjectMapper objectMapper;

    private LookupService lookupService;

    public void setLookupService(LookupService lookupService) {
        this.lookupService = lookupService;
    }

    @Override
    public ObjectNode wrapResult(CompanyEntity input, BaseQuery query) {

        objectMapper = new ObjectMapper();

        NodeWrapper root = new NodeWrapper(objectMapper.createObjectNode());
        root.put("cvrNummer", input.getCvrNumber());

        OffsetDateTime highestStatusTime = OffsetDateTime.MIN;

        List<CompanyRegistration> companyRegistrations = input.getRegistrations();
        CompanyRegistration companyRegistration = companyRegistrations.get(companyRegistrations.size() - 1);
        List<CompanyEffect> companyEffects = companyRegistration.getEffects();
        companyEffects.sort(Comparator.comparing(Effect::getEffectTo, Comparator.nullsLast(Comparator.naturalOrder())));

        for (CompanyEffect virkning : companyEffects) {
            OffsetDateTime effectFrom = virkning.getEffectFrom();
            List<CompanyBaseData> dataItems = virkning.getDataItems();
            boolean statusEliglible = (effectFrom != null && effectFrom.isAfter(highestStatusTime));
            for (CompanyBaseData companyBaseData : dataItems) {
                this.wrapDataObject(root, companyBaseData);
                if (statusEliglible) {
                    if (companyBaseData.getStatusCode() != null) {
                        highestStatusTime = effectFrom;
                    }
                }
            }
        }
        if (highestStatusTime.isAfter(OffsetDateTime.MIN)) {
            root.put("statuskodedato", highestStatusTime.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        return root.getNode();
    }

    protected void wrapDataObject(NodeWrapper output, CompanyBaseData dataItem) {

        output.put("navn", dataItem.getCompanyName());

        Industry industry = dataItem.getPrimaryIndustry();
        if (industry != null) {
            output.put("forretningsområde", industry.getIndustryText());
        }

        String statusCode = dataItem.getStatusCode();
        output.put("statuskode", statusCode);

        Address address = dataItem.getPostalAddress();
        if (address == null) {
            address = dataItem.getLocationAddress();
        }
        if (address != null) {
            int municipalityCode = 0;
            int roadCode = address.getRoadCode();
            Municipality municipality = address.getMunicipality();
            if (municipality != null) {
                output.put("myndighedskode", municipality.getCode());
                output.put("kommune", municipality.getName());
                municipalityCode = municipality.getCode();
            }
            if (roadCode > 0) {
                output.put("vejkode", roadCode);
                if (municipalityCode > 0 && this.lookupService != null) {
                    Lookup lookup = lookupService.doLookup(municipalityCode, roadCode);
                    if (lookup.localityCode != 0) {
                        output.put("stedkode", lookup.localityCode);
                    }
                }
            }
            String addressFormatted = address.getAddressFormatted();
            if (addressFormatted != null && !addressFormatted.isEmpty()) {
                output.put("adresse", addressFormatted);
            }
            if (address.getPostBox() > 0) {
                output.put("postboks", address.getPostBox());
            }

            PostCode postCode = address.getPost();
            if (postCode != null) {
                output.put("postnummer", postCode.getPostCode());
                output.put("bynavn", postCode.getPostDistrict());
            }

            output.put("landekode", address.getCountryCode());

            String coName = address.getCoName();
            if (coName != null) {
                output.put("co", coName);
            }
        }

    }

}

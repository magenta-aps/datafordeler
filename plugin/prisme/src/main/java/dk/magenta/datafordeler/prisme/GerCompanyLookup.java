package dk.magenta.datafordeler.prisme;

import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.ger.data.company.CompanyEntity;
import dk.magenta.datafordeler.ger.data.company.CompanyQuery;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
public class GerCompanyLookup {

    public HashSet<CompanyEntity> lookup(Session session, Collection<String> cvrNumbers) {
        CompanyQuery query = new CompanyQuery();
        for (String cvrNumber : cvrNumbers) {
            query.addGerNr(cvrNumber);
        }
        List<CompanyEntity> companyEntities = QueryManager.getAllEntities(session, query, CompanyEntity.class);
        return new HashSet<>(companyEntities);
    }

    private final HashMap<UUID, String> statusMap = new HashMap<>();

    @PostConstruct
    public void init() {
        this.statusMap.put(UUID.fromString("c088142e-af1b-4762-9253-ea630e555c97"), "fremtid: fremtid");
        this.statusMap.put(UUID.fromString("0a842bb3-e7d9-4417-ba71-ac391e22f66e"), "udenretsvirkning: udenretsvirkning");
        this.statusMap.put(UUID.fromString("ac791524-f83e-4e06-ace6-bf5334eb71b7"), "oploest: oploest");
        this.statusMap.put(UUID.fromString("F62902BA-780E-48A9-8DA2-63ACBAA59201"), "Ingen");
        this.statusMap.put(UUID.fromString("CD6868E2-9840-434B-9B59-BA41470AA054"), "Aktiv: Normal");
        this.statusMap.put(UUID.fromString("D07ADE3A-3658-4E34-B48D-3746DA832E9A"), "Aktiv: Aktive");
        this.statusMap.put(UUID.fromString("C2A2DD7D-82B5-45F9-96B5-0ED0EDAD0392"), "Aktiv: Under tvangsopløsning");
        this.statusMap.put(UUID.fromString("714393BD-A7C0-465E-A263-049A9B335DD2"), "Aktiv: Under frivillige likvidation");
        this.statusMap.put(UUID.fromString("C67D5B86-1DB6-406E-8D5C-3A218EBE4B87"), "Aktiv: Under konkurs");
        this.statusMap.put(UUID.fromString("6588D7DF-DF81-4EE4-912F-53D1F5A50856"), "Aktiv: Under rekonstruktion");
        this.statusMap.put(UUID.fromString("48D1F7D5-C8CA-4370-A82A-23725DC7AB55"), "Aktiv: Under reassumering");
        this.statusMap.put(UUID.fromString("C589794B-907B-4A47-A99B-DB36754F5593"), "Aktiv: Omdannet");
        this.statusMap.put(UUID.fromString("86250A68-891B-4476-A54B-E37FD8B0924F"), "Ophørt");
        this.statusMap.put(UUID.fromString("C6143CB1-B28F-4F93-A8E2-5B79D8AC3B85"), "Ophørt");
        this.statusMap.put(UUID.fromString("2986BAB7-9D01-4373-9883-C77C2A6E09CD"), "Ophørt");
        this.statusMap.put(UUID.fromString("922C8F99-DBAA-42F8-A4E7-B0ECF176EB3D"), "Ophørt");
        this.statusMap.put(UUID.fromString("BCA8A717-D441-4BF9-B9F1-8740DA3E8B19"), "Ophørt");
        this.statusMap.put(UUID.fromString("B7DC1179-1B08-4BDD-AF22-CFC3646E7B3E"), "Ophørt");
        this.statusMap.put(UUID.fromString("B8D92DBB-A697-4551-A5F9-FA683B48B5AD"), "Ophørt");
        this.statusMap.put(UUID.fromString("16D09890-0415-4E25-BBC4-7DFFDB5E5F92"), "Ophørt");
        this.statusMap.put(UUID.fromString("5A9BF70C-C6A7-4CB5-A349-F20241E09D6D"), "Ophørt");
        this.statusMap.put(UUID.fromString("553CEABD-8399-4425-A60B-4A6B4F9E87F1"), "Ophørt");
        this.statusMap.put(UUID.fromString("C4DD742B-85DB-4249-BB39-3BFA3A36C8E8"), "Ophørt");
    }

    public String getStatus(UUID uuid) {
        return this.statusMap.get(uuid);
    }

}

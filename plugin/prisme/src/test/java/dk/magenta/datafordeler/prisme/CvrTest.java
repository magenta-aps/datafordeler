package dk.magenta.datafordeler.prisme;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cvr.access.CvrAreaRestrictionDefinition;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static org.mockito.Mockito.when;

@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CvrTest extends TestBase {

    @Test
    public void testCompanyPrisme() throws IOException, DataFordelerException {
        loadAllGeoAdress(sessionManager);
        loadCompany(cvrPlugin, sessionManager, objectMapper);

        try {

            TestUserDetails testUserDetails = new TestUserDetails();


            HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                    "/prisme/cvr/1/" + 25052943,
                    HttpMethod.GET,
                    httpEntity,
                    String.class
            );
            Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());


            testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
            this.applyAccess(testUserDetails);
            response = restTemplate.exchange(
                    "/prisme/cvr/1/" + 25052943,
                    HttpMethod.GET,
                    httpEntity,
                    String.class
            );
            Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());


            testUserDetails.giveAccess(
                    cvrPlugin.getAreaRestrictionDefinition().getAreaRestrictionTypeByName(
                            CvrAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER
                    ).getRestriction(
                            CvrAreaRestrictionDefinition.RESTRICTION_KOMMUNE_SERMERSOOQ
                    )
            );
            this.applyAccess(testUserDetails);
            response = restTemplate.exchange(
                    "/prisme/cvr/1/" + 25052943,
                    HttpMethod.GET,
                    httpEntity,
                    String.class
            );
            Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());


            testUserDetails.giveAccess(
                    cvrPlugin.getAreaRestrictionDefinition().getAreaRestrictionTypeByName(
                            CvrAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER
                    ).getRestriction(
                            CvrAreaRestrictionDefinition.RESTRICTION_KOMMUNE_KUJALLEQ
                    )
            );
            testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
            this.applyAccess(testUserDetails);
            response = restTemplate.exchange(
                    "/prisme/cvr/1/" + 25052943 + "?returnParticipantDetails=1",
                    HttpMethod.GET,
                    httpEntity,
                    String.class
            );
            Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        } finally {
            cleanup();
        }
    }


    @Test
    public void testGerFallback() throws IOException, DataFordelerException {
        this.cvrRecordService.setEnableDirectLookup(false);
        this.cvrRecordService.setEnableGerLookup(true);
        TestUserDetails testUserDetails = new TestUserDetails();
        this.loadGerCompany(gerPlugin, sessionManager);
        this.loadGerParticipant(gerPlugin, sessionManager);
        this.loadAllGeoAdress(sessionManager);

        testUserDetails.giveAccess(
                cvrPlugin.getAreaRestrictionDefinition().getAreaRestrictionTypeByName(
                        CvrAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER
                ).getRestriction(
                        CvrAreaRestrictionDefinition.RESTRICTION_KOMMUNE_KUJALLEQ
                )
        );
        testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> response;
        response = restTemplate.exchange(
                "/prisme/cvr/1/" + 12345678 + "?returnParticipantDetails=1",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
    }


    @Test
    public void testCompanyBulkPrisme() throws Exception {

        loadAllGeoAdress(sessionManager);
        OffsetDateTime start = OffsetDateTime.now();
        loadManyCompanies(cvrPlugin, sessionManager, 5, 0);
        OffsetDateTime middle = OffsetDateTime.now();
        Thread.sleep(10);
        loadManyCompanies(cvrPlugin, sessionManager, 5, 5);
        OffsetDateTime end = OffsetDateTime.now();

        try {
            TestUserDetails testUserDetails = new TestUserDetails();


            testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
            this.applyAccess(testUserDetails);

            ObjectNode body = objectMapper.createObjectNode();
            body.put("cvrNumber", "10000009");
            HttpEntity<String> httpEntity = new HttpEntity<>(body.toString(), new HttpHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                    "/prisme/cvr/1/",
                    HttpMethod.POST,
                    httpEntity,
                    String.class
            );
            Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
            Assertions.assertEquals(1, objectMapper.readTree(response.getBody()).size());


            body = objectMapper.createObjectNode();
            ArrayNode cvrList = objectMapper.createArrayNode();
            cvrList.add("10000002");
            cvrList.add("10000005");
            body.set("cvrNumber", cvrList);
            httpEntity = new HttpEntity<String>(body.toString(), new HttpHeaders());
            response = restTemplate.exchange(
                    "/prisme/cvr/1/",
                    HttpMethod.POST,
                    httpEntity,
                    String.class
            );
            Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
            Assertions.assertEquals(2, objectMapper.readTree(response.getBody()).size());


            body = objectMapper.createObjectNode();
            cvrList = objectMapper.createArrayNode();
            cvrList.add("10000000");
            cvrList.add("10000001");
            cvrList.add("10000002");
            cvrList.add("10000003");
            cvrList.add("10000004");
            cvrList.add("10000005");
            cvrList.add("10000006");
            cvrList.add("10000007");
            cvrList.add("10000008");
            cvrList.add("10000009");
            body.set("cvrNumber", cvrList);
            httpEntity = new HttpEntity<String>(body.toString(), new HttpHeaders());
            long tic = Instant.now().toEpochMilli();
            response = restTemplate.exchange(
                    "/prisme/cvr/1/",
                    HttpMethod.POST,
                    httpEntity,
                    String.class
            );
            Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
            Assertions.assertEquals(10, objectMapper.readTree(response.getBody()).size());


            body = objectMapper.createObjectNode();
            cvrList = objectMapper.createArrayNode();
            cvrList.add("10000000");
            cvrList.add("10000001");
            cvrList.add("10000002");
            cvrList.add("10000003");
            cvrList.add("10000004");
            cvrList.add("10000005");
            cvrList.add("10000006");
            cvrList.add("10000007");
            cvrList.add("10000008");
            cvrList.add("10000009");
            body.set("cvrNumber", cvrList);
            // "updatedSince" matches on dafoUpdated field, not on lastUpdated field
            body.put("updatedSince", start.minusSeconds(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            httpEntity = new HttpEntity<String>(body.toString(), new HttpHeaders());
            response = restTemplate.exchange(
                    "/prisme/cvr/1/",
                    HttpMethod.POST,
                    httpEntity,
                    String.class
            );
            Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
            Assertions.assertEquals(10, objectMapper.readTree(response.getBody()).size());


            body = objectMapper.createObjectNode();
            cvrList = objectMapper.createArrayNode();
            cvrList.add("10000000");
            cvrList.add("10000001");
            cvrList.add("10000002");
            cvrList.add("10000003");
            cvrList.add("10000004");
            cvrList.add("10000005");
            cvrList.add("10000006");
            cvrList.add("10000007");
            cvrList.add("10000008");
            cvrList.add("10000009");
            body.set("cvrNumber", cvrList);
            body.put("updatedSince", end.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            httpEntity = new HttpEntity<String>(body.toString(), new HttpHeaders());
            response = restTemplate.exchange(
                    "/prisme/cvr/1/",
                    HttpMethod.POST,
                    httpEntity,
                    String.class
            );
            Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
            Assertions.assertEquals(0, objectMapper.readTree(response.getBody()).size());

        } finally {
            cleanup();
        }
    }

    private void applyAccess(TestUserDetails testUserDetails) {
        when(dafoUserManager.getFallbackUser()).thenReturn(testUserDetails);
    }

}

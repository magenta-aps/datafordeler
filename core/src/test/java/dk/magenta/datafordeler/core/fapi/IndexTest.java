package dk.magenta.datafordeler.core.fapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.HttpStatusException;
import dk.magenta.datafordeler.plugindemo.DemoPlugin;
import dk.magenta.datafordeler.plugindemo.fapi.DemoEntityService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;


@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
public class IndexTest {

    @Autowired
    private DemoEntityService entityService;

    @Autowired
    private DemoPlugin plugin;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testServiceDescriptor() {
        String path = "/test";
        ServiceDescriptor serviceDescriptor = entityService.getServiceDescriptor(path);
        Assertions.assertTrue(serviceDescriptor instanceof RestServiceDescriptor);
        Assertions.assertEquals(plugin, serviceDescriptor.getPlugin());
        Assertions.assertEquals("rest", serviceDescriptor.getType());
        Assertions.assertEquals(path, serviceDescriptor.getServiceAddress());
        Assertions.assertEquals(path, serviceDescriptor.getMetaAddress());
        Assertions.assertEquals("postnummer", serviceDescriptor.getServiceName());
        HashMap<String, String> mappedFields = new HashMap<>();
        for (ServiceDescriptor.ServiceQueryField queryField : serviceDescriptor.getFields()) {
            for (String name : queryField.names) {
                mappedFields.put(name, queryField.type);
            }
        }
        Assertions.assertEquals("int", mappedFields.get("postnr"));
        Assertions.assertEquals("string", mappedFields.get("bynavn"));
        Assertions.assertEquals("string", mappedFields.get("registrationFromAfter"));
        Assertions.assertEquals("string", mappedFields.get("registrationFromBefore"));
        Assertions.assertEquals("string", mappedFields.get("registrationToAfter"));
        Assertions.assertEquals("string", mappedFields.get("registrationToBefore"));
        Assertions.assertEquals("string", mappedFields.get("effectFromAfter"));
        Assertions.assertEquals("string", mappedFields.get("effectFromBefore"));
        Assertions.assertEquals("string", mappedFields.get("effectToAfter"));
        Assertions.assertEquals("string", mappedFields.get("effectToBefore"));
        Assertions.assertEquals("int", mappedFields.get("pageSize"));
        Assertions.assertEquals("int", mappedFields.get("page"));
    }

    @Test
    public void testIndex() throws URISyntaxException, HttpStatusException, DataStreamException, IOException {
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> resp = this.restTemplate.exchange("/demo/postnummer/1/rest", HttpMethod.GET, httpEntity, String.class);
        JsonNode json = objectMapper.readTree(resp.getBody());
        Assertions.assertNotNull(json);
        Assertions.assertTrue(json instanceof ObjectNode);
        ObjectNode object = (ObjectNode) json;
        Assertions.assertEquals("postnummer", object.get("service_name").textValue());
        Assertions.assertEquals("/demo/postnummer/1/rest/{UUID}", object.get("fetch_url").textValue());
        Assertions.assertEquals("/demo/postnummer/1/rest/search", object.get("search_url").textValue());
        Assertions.assertEquals("https://redmine.magenta-aps.dk/projects/dafodoc/wiki/API", object.get("declaration_url").textValue());
        Assertions.assertEquals("rest", object.get("type").textValue());
        Assertions.assertEquals("/demo/postnummer/1/rest", object.get("metadata_url").textValue());
        ArrayNode queryFields = (ArrayNode) object.get("search_queryfields");
        HashMap<String, String> mappedFields = new HashMap<>();
        Iterator<JsonNode> queryFieldNodes = queryFields.elements();
        while (queryFieldNodes.hasNext()) {
            JsonNode queryFieldNode = queryFieldNodes.next();
            Assertions.assertTrue(queryFieldNode instanceof ObjectNode);
            ObjectNode queryFieldObjectNode = (ObjectNode) queryFieldNode;
            ArrayNode namesNode = (ArrayNode) queryFieldObjectNode.get("names");
            for (JsonNode nameNode : namesNode) {
                mappedFields.put(nameNode.textValue(), queryFieldObjectNode.get("type").textValue());
            }
        }
        Assertions.assertEquals("int", mappedFields.get("postnr"));
        Assertions.assertEquals("string", mappedFields.get("bynavn"));
        Assertions.assertEquals("string", mappedFields.get("registrationFromAfter"));
        Assertions.assertEquals("string", mappedFields.get("registrationFromBefore"));
        Assertions.assertEquals("string", mappedFields.get("registrationToAfter"));
        Assertions.assertEquals("string", mappedFields.get("registrationToBefore"));
        Assertions.assertEquals("string", mappedFields.get("effectFromAfter"));
        Assertions.assertEquals("string", mappedFields.get("effectFromBefore"));
        Assertions.assertEquals("string", mappedFields.get("effectToAfter"));
        Assertions.assertEquals("string", mappedFields.get("effectToBefore"));
        Assertions.assertEquals("int", mappedFields.get("pageSize"));
        Assertions.assertEquals("int", mappedFields.get("page"));
    }


    @Test
    public void testJsonIndex() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        HttpEntity<String> httpEntity = new HttpEntity<String>("", headers);
        ResponseEntity<String> resp = this.restTemplate.exchange("/", HttpMethod.GET, httpEntity, String.class);
        JsonNode json = objectMapper.readTree(resp.getBody());
        Assertions.assertNotNull(json);
        JsonNode servicesNode = json.get("services");
        Assertions.assertTrue(servicesNode instanceof ArrayNode);
        ArrayNode servicesArray = (ArrayNode) servicesNode;
        HashMap<String, ObjectNode> serviceMap = new HashMap<>();
        for (JsonNode serviceNode : servicesArray) {
            Assertions.assertTrue(serviceNode instanceof ObjectNode);
            ObjectNode serviceObject = (ObjectNode) serviceNode;
            serviceMap.put(serviceObject.get("metadata_url").textValue(), serviceObject);
            String type = serviceObject.get("type").textValue();
            Assertions.assertNotNull(type);
            Assertions.assertEquals("rest", type);
            Assertions.assertNotNull(serviceObject.get("service_name").textValue());
            Assertions.assertEquals("https://redmine.magenta-aps.dk/projects/dafodoc/wiki/API", serviceObject.get("declaration_url").textValue());
            Assertions.assertNotNull(serviceObject.get("fetch_url").textValue());
            Assertions.assertNotNull(serviceObject.get("search_url").textValue());
            JsonNode queryFieldsNode = serviceObject.get("search_queryfields");
            Assertions.assertNotNull(queryFieldsNode);
            Assertions.assertTrue(queryFieldsNode instanceof ArrayNode);
            ArrayNode queryFieldsArray = (ArrayNode) queryFieldsNode;
            HashMap<String, String> queryFieldMap = new HashMap<>();
            for (JsonNode queryField : queryFieldsArray) {
                Assertions.assertNotNull(queryField);
                Assertions.assertTrue(queryField instanceof ObjectNode);
                ObjectNode queryFieldObject = (ObjectNode) queryField;
                Assertions.assertNotNull(queryFieldObject.get("names"));
                Assertions.assertNotNull(queryFieldObject.get("type"));
                ArrayNode nameNodes = (ArrayNode) queryFieldObject.get("names");
                for (JsonNode name : nameNodes) {
                    queryFieldMap.put(name.textValue(), queryFieldObject.get("type").textValue());
                }

            }
            Assertions.assertEquals("string", queryFieldMap.get("registrationFromAfter"));
            Assertions.assertEquals("string", queryFieldMap.get("registrationFromBefore"));
            Assertions.assertEquals("string", queryFieldMap.get("registrationToAfter"));
            Assertions.assertEquals("string", queryFieldMap.get("registrationToBefore"));
            Assertions.assertEquals("string", queryFieldMap.get("effectFromAfter"));
            Assertions.assertEquals("string", queryFieldMap.get("effectFromBefore"));
            Assertions.assertEquals("string", queryFieldMap.get("effectToAfter"));
            Assertions.assertEquals("string", queryFieldMap.get("effectToBefore"));
            Assertions.assertEquals("int", queryFieldMap.get("pageSize"));
            Assertions.assertEquals("int", queryFieldMap.get("page"));
        }
    }

    @Test
    public void testHtmlIndex() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "text/html");
        HttpEntity<String> httpEntity = new HttpEntity<String>("", headers);
        ResponseEntity<String> resp = this.restTemplate.exchange("/", HttpMethod.GET, httpEntity, String.class);
        Assertions.assertNotNull(resp.getBody());
    }

}

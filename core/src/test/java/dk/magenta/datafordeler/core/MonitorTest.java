package dk.magenta.datafordeler.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;

import java.text.ParseException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.TimeZone;

@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MonitorTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static Logger log = LogManager.getLogger(MonitorTest.class.getCanonicalName());

    @Test
    public void testDatabaseMonitoring() {
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> response = this.restTemplate.exchange("/monitor/database", HttpMethod.GET, httpEntity, String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testPullMonitoring() {
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> response = this.restTemplate.exchange("/monitor/pull", HttpMethod.GET, httpEntity, String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    //@Test
    public void testError() {
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> response = this.restTemplate.exchange("/monitor/errors", HttpMethod.GET, httpEntity, String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(), response.getBody());

        log.info("testing");
        response = this.restTemplate.exchange("/monitor/errors", HttpMethod.GET, httpEntity, String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        log.error(new NullPointerException().toString());
        response = this.restTemplate.exchange("/monitor/errors", HttpMethod.GET, httpEntity, String.class);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testGetTimeBefore() throws ParseException {
        CronExpression expression = new CronExpression("0 0 0 * * ?");
        expression.setTimeZone(TimeZone.getTimeZone("UTC"));
        Assertions.assertTrue(
                MonitorService.getTimeBefore(
                        expression,
                        OffsetDateTime.of(2018, 1, 30, 12, 0, 0, 0, ZoneOffset.UTC).toInstant()
                ).equals(
                        OffsetDateTime.of(2018, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC).toInstant()
                )
        );

        Assertions.assertTrue(
                MonitorService.getTimeBefore(
                        expression,
                        OffsetDateTime.of(2018, 1, 30, 0, 0, 1, 0, ZoneOffset.UTC).toInstant()
                ).equals(
                        OffsetDateTime.of(2018, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC).toInstant()
                )
        );



        expression = new CronExpression("* * * * * ?");
        expression.setTimeZone(TimeZone.getTimeZone("UTC"));
        Assertions.assertTrue(
                MonitorService.getTimeBefore(
                        expression,
                        OffsetDateTime.of(2018, 1, 30, 12, 0, 0, 0, ZoneOffset.UTC).toInstant()
                ).equals(
                        OffsetDateTime.of(2018, 1, 30, 11, 59, 59, 0, ZoneOffset.UTC).toInstant()
                )
        );


    }
}

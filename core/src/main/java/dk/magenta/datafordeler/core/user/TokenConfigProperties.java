package dk.magenta.datafordeler.core.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TokenConfigProperties {

    @Value("dafo.tokenvalidation.time-skew-in-seconds")
    private int timeSkewInSeconds = 5;

    @Value("dafo.tokenvalidation.max-assertion-time-in-seconds")
    private long maxAssertionTimeInSeconds = 60 * 60;

    @Value("dafo.tokenvalidation.issuer-metadata-path")
    private String issuerMetadataPath = null;

    @Value("dafo.tokenvalidation.audience-uri")
    private String audienceURI = "https://data.gl/";

    public int getTimeSkewInSeconds() {
        return timeSkewInSeconds;
    }

    public long getMaxAssertionTimeInSeconds() {
        return maxAssertionTimeInSeconds;
    }

    public String getIssuerMetadataPath() {
        return issuerMetadataPath;
    }

    public String getAudienceURI() {
        return audienceURI;
    }

}

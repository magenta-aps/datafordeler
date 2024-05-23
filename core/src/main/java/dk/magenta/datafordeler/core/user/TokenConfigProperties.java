package dk.magenta.datafordeler.core.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TokenConfigProperties {

    @Value("${dafo.tokenvalidation.time-skew-in-seconds:5}")
    private int timeSkewInSeconds = 5;

    @Value("${dafo.tokenvalidation.max-assertion-time-in-seconds:3600}")
    private long maxAssertionTimeInSeconds = 60 * 60;

    @Value("${dafo.tokenvalidation.issuer-metadata-path:#{null}}")
    private String issuerMetadataPath = null;

    @Value("${dafo.tokenvalidation.audience-URI}")
    private String audienceURI = "https://data.gl/";

    public int getTimeSkewInSeconds() {
        return timeSkewInSeconds;
    }

    public void setTimeSkewInSeconds(int timeSkewInSeconds) {
        this.timeSkewInSeconds = timeSkewInSeconds;
    }

    public long getMaxAssertionTimeInSeconds() {
        return maxAssertionTimeInSeconds;
    }

    public void setMaxAssertionTimeInSeconds(long maxAssertionTimeInSeconds) {
        this.maxAssertionTimeInSeconds = maxAssertionTimeInSeconds;
    }

    public String getIssuerMetadataPath() {
        return issuerMetadataPath;
    }

    public void setIssuerMetadataPath(String issuerMetadataPath) {
        this.issuerMetadataPath = issuerMetadataPath;
    }

    public String getAudienceURI() {
        return audienceURI;
    }

    public void setAudienceURI(String audienceURI) {
        this.audienceURI = audienceURI;
    }
}

package dk.magenta.datafordeler.core.user;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;


@ConfigurationProperties(prefix="dafo.tokenvalidation")
@ConfigurationPropertiesScan
public class TokenConfigProperties {

    private int timeSkewInSeconds = 5;

    private long maxAssertionTimeInSeconds = 60 * 60;

    private String issuerMetadataPath = null;

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

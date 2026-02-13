package dk.magenta.datafordeler.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;

@Component
public class JobReporter {

    protected Logger log = LogManager.getLogger(this.getClass().getCanonicalName());

    @Value("${dafo.job_pushgateway_data}")
    private String pushgatewayData;

    @Value("${dafo.job_pushgateway_url}")
    private String pushgatewayUrl;

    public boolean reportJobSuccess(String jobName) {
        if (this.pushgatewayUrl != null && !this.pushgatewayUrl.isBlank() && this.pushgatewayData != null && !this.pushgatewayData.isBlank()) {
            String data = String.format(this.pushgatewayData, Instant.now().getEpochSecond());
            String url = String.format(this.pushgatewayUrl, jobName);
            try {
                new URI(url).toURL().openConnection().getOutputStream().write(data.getBytes());
            } catch (IOException | URISyntaxException e) {
                log.error("Failed to report job success", e);
            }
            return true;
        }
        return false;
    }

}

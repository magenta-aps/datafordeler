package dk.magenta.datafordeler.core;

import jakarta.annotation.PostConstruct;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;
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
        if (this.pushgatewayUrl != null && this.pushgatewayData != null) {
            String data = String.format(this.pushgatewayData, Instant.now().getEpochSecond());
            String url = String.format(this.pushgatewayUrl, jobName);
            try {
                try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                    HttpPost post = new HttpPost(new URI(url));
                    post.setEntity(new StringEntity(data));
                    httpClient.execute(
                            post,
                            (HttpClientResponseHandler<Object>) response -> null
                    );
                }
            } catch (IOException | URISyntaxException e) {
                log.error("Failed to report job success", e);
            }
            return true;
        }
        return false;
    }

    @PostConstruct
    public void sendStartupMessage() {
        log.info("JobReporter started");
        this.reportJobSuccess("startup");
    }

}

package dk.magenta.dafosts.clientcertificates.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties used for setting up the Ajp connector.
 */
@ConfigurationProperties("dafo.ajp")
public class AjpProperties {
    private boolean enabled = true;
    private int ajpPort = 9090;
    private boolean secure = true;
    private boolean allowTrace = false;
    private String scheme = "https";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getAjpPort() {
        return ajpPort;
    }

    public void setAjpPort(int ajpPort) {
        this.ajpPort = ajpPort;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public boolean isAllowTrace() {
        return allowTrace;
    }

    public void setAllowTrace(boolean allowTrace) {
        this.allowTrace = allowTrace;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }
}

package dk.magenta.datafordeler.core.fapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.user.DafoUserDetails;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlElement;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Envelope {

    public Envelope() {
        try {
            this.terms = new URL("https://doc.test.data.gl/terms");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @JsonProperty
    @XmlElement
    private String path;

    public void setPath(String path) {
        this.path = path;
    }

    @JsonProperty
    @XmlElement
    private URL terms;

    public void setTerms(URL terms) {
        this.terms = terms;
    }

    @JsonProperty
    @XmlElement
    private OffsetDateTime requestTimestamp;

    public void setRequestTimestamp(OffsetDateTime requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    @JsonProperty
    @XmlElement
    private OffsetDateTime responseTimestamp;

    public void setResponseTimestamp(OffsetDateTime responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
    }

    @JsonProperty
    @XmlElement
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private OffsetDateTime newestResultTimestamp;

    public void setNewestResultTimestamp(OffsetDateTime newestResultTimestamp) {
        this.newestResultTimestamp = newestResultTimestamp;
    }

    @JsonProperty
    @XmlElement
    private String username;

    public void setUsername(String username) {
        this.username = username;
    }

    @JsonProperty
    @XmlElement
    private int page;

    public void setPage(int page) {
        this.page = page;
    }

    @JsonProperty
    @XmlElement
    private int pageSize;

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    @JsonProperty
    @XmlElement
    private List<?> results = Collections.emptyList();

    public void setResults(Collection<?> results) {
        this.results = new LinkedList<>(results);
    }

    public void setResult(Object result) {
        this.results = Collections.singletonList(result);
    }

    public void addQueryData(BaseQuery query) {
        this.setPage(query.getPage());
        this.setPageSize(query.getPageSize());
    }

    public void addUserData(DafoUserDetails user) {
        this.setUsername(user.toString());
        this.setRequestTimestamp(user.getCreationTime());
    }

    public void addRequestData(HttpServletRequest request) {
        this.setPath(request.getServletPath());
    }

    public void close() {
        if (this.requestTimestamp != null) {
            this.setResponseTimestamp(OffsetDateTime.now());
        }
    }

    public String toLogString(String queryString) {
        return String.format(
                "Path: %s, query: %s, results: %s, request timestamp: %s, "
                        + "response timestamp: %s, page: %s, pagesize: %s",
                path,
                queryString == null ? "<empty>" : queryString,
                results.size(),
                requestTimestamp == null ?
                        "<null>" :
                        requestTimestamp.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                responseTimestamp == null ?
                        "<null>" :
                        responseTimestamp.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                page,
                pageSize
        );
    }

    public String toLogString() {
        return toLogString(null);
    }

}

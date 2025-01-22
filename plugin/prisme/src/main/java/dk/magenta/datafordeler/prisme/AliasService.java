package dk.magenta.datafordeler.prisme;


import com.fasterxml.jackson.core.JsonProcessingException;
import dk.magenta.datafordeler.core.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;

@RestController
@RequestMapping("/prisme")
public class AliasService {

    @Autowired
    private CvrServiceDirect cvrService;

    @Autowired
    private CprRecordService cprService;

    @RequestMapping(method = RequestMethod.GET, path = "/cvr/{cvrNummer}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getCvrSingle(@PathVariable("cvrNummer") String cvrNummer, HttpServletRequest request) throws JsonProcessingException, DataFordelerException {
        return this.cvrService.getSingle(cvrNummer, request);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/cvr", produces = {MediaType.APPLICATION_JSON_VALUE})
    public StreamingResponseBody getCvrBulk(HttpServletRequest request) throws HttpNotFoundException, InvalidTokenException, IOException, InvalidClientInputException, AccessDeniedException, AccessRequiredException, InvalidCertificateException {
        return this.cvrService.getBulk(request);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/cpr/{cprNummer}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getCprSingle(@PathVariable("cprNummer") String cprNummer, HttpServletRequest request)
            throws AccessDeniedException, AccessRequiredException, InvalidTokenException, InvalidClientInputException, JsonProcessingException, HttpNotFoundException, InvalidCertificateException {
        return this.cprService.getSingle(cprNummer, request);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/", produces = {MediaType.APPLICATION_JSON_VALUE})
    public StreamingResponseBody getCprBulkDeprecate(HttpServletRequest request)
            throws AccessDeniedException, AccessRequiredException, InvalidTokenException, InvalidClientInputException, InvalidParameterException, IOException, HttpNotFoundException, InvalidCertificateException {
        return this.cprService.getBulk(request);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/cpr", produces = {MediaType.APPLICATION_JSON_VALUE})
    public StreamingResponseBody getCprBulk(HttpServletRequest request)
            throws AccessDeniedException, AccessRequiredException, InvalidTokenException, InvalidClientInputException, InvalidParameterException, IOException, HttpNotFoundException, InvalidCertificateException {
        return this.cprService.getBulk(request);
    }
}

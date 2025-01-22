package dk.magenta.datafordeler.core.testutil;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/test")
public class CallbackController {

    @Autowired
    ObjectMapper objectMapper;

    private static Logger log = LogManager.getLogger(CallbackController.class.getCanonicalName());

    private class Expector {
        public String response;
        public Collection<Callback> callbacks;
        public Expector(String response, Collection<Callback> callbacks) {
            this.response = response;
            this.callbacks = callbacks;
        }
        public void run(String key, Map<String, String[]> params) {
            for (Callback callback : this.callbacks) {
                callback.run(key, params);
            }
        }
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public class HttpNotFoundException extends RuntimeException {
    }

    private HashMap<String, Expector> expectors = new HashMap<>();

    public void addCallbackResponse(String key, String response, Collection<Callback> callbacks) {
        this.log.debug("Added listener on "+key);
        this.expectors.put(key, new Expector(response, callbacks));
    }

    public void addCallbackResponse(String key, String response, Callback callback) {
        this.log.debug("Added listener on "+key);
        this.expectors.put(key, new Expector(response, Collections.singletonList(callback)));
    }

    public void addCallbackResponse(String key, String response) {
        this.log.debug("Added listener on "+key);
        this.expectors.put(key, new Expector(response, Collections.emptyList()));
    }

    public void removeCallback(String key) {
        this.expectors.remove(key);
    }

    @RequestMapping(value = "**")
    public @ResponseBody String handleGet(HttpServletRequest request) {
        String path = request.getServletPath();
        Expector expector = this.expectors.get(path);
        if (expector != null) {
            expector.run(path, request.getParameterMap());
            return expector.response;
        }
        throw new HttpNotFoundException();
    }

}

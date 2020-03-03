package dk.magenta.dafosts.saml.users;

import dk.magenta.dafosts.saml.stereotypes.DafoSAMLUser;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.security.Principal;


/**
 * Tells spring how to resolve an argument to a controller methods that has the "@DafoSAMLUser" annotation and
 * the type "DafoSAMLUserDetails".
 */
@Component
public class DafoSAMLUserArgumentResolver implements HandlerMethodArgumentResolver {

    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.getParameterAnnotation(DafoSAMLUser.class) != null
                && methodParameter.getParameterType().equals(DafoSAMLUserDetails.class);
    }

    public Object resolveArgument(MethodParameter methodParameter,
                                  ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        if (this.supportsParameter(methodParameter)) {
            Principal principal = (Principal) webRequest.getUserPrincipal();
            return (DafoSAMLUserDetails) ((Authentication) principal).getDetails();
        } else {
            return WebArgumentResolver.UNRESOLVED;
        }
    }
}
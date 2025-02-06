package dk.magenta.datafordeler.core.fapi;

import dk.magenta.datafordeler.core.Engine;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@WebFilter(value = "/*", asyncSupported = true)
@Component
public class ServerNameHeaderFilter implements Filter {

    @Autowired
    private Engine engine;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setHeader("X-Dafo-Server", engine.getServerName());
        chain.doFilter(request, response);
    }

}

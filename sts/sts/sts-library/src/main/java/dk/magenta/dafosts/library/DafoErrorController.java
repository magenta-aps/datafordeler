package dk.magenta.dafosts.library;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class DafoErrorController implements ErrorController {
    @Autowired
    ErrorAttributes errorAttributes;

    @Override
    public String getErrorPath() {
        return "/error";
    }

    @RequestMapping("/error")
    public String error(Model model, HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute("httpStatus", HttpStatus.valueOf(response.getStatus()));
        model.addAllAttributes(errorAttributes.getErrorAttributes(
                new ServletRequestAttributes(request, response),
                false
        ));
        return "error";
    }
}

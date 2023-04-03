package dk.magenta.datafordeler.cvr.service;

import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.cvr.entitymanager.CompanyEntityManager;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cvr/company/close")
public class CompanyCloseService {

    @Autowired
    CompanyEntityManager companyEntityManager;

    @RequestMapping("/")
    public void close(HttpServletRequest request) throws DataFordelerException {
        this.companyEntityManager.closeAllEligibleRegistrations();
    }

}

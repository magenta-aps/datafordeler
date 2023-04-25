package dk.magenta.datafordeler.cvr.service;

import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.cvr.entitymanager.CompanyEntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/cvr/company/close")
public class CompanyCloseService {

    @Autowired
    CompanyEntityManager companyEntityManager;

    @RequestMapping(
            path = {"/{cvr}"}
    )
    public void close(@PathVariable("cvr") String cvr, HttpServletRequest request) throws DataFordelerException {
        this.companyEntityManager.closeAllEligibleRegistrations(cvr);
    }

}

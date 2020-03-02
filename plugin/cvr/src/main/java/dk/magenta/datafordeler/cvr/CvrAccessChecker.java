package dk.magenta.datafordeler.cvr;

import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.user.DafoUserDetails;

public class CvrAccessChecker {

  public static void checkAccess(DafoUserDetails dafoUserDetails) throws AccessDeniedException {
    dafoUserDetails.checkHasSystemRole(CvrRolesDefinition.READ_CVR_ROLE);
  }

}

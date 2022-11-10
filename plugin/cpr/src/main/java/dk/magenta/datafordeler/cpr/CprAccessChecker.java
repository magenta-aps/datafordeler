package dk.magenta.datafordeler.cpr;

import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.AccessRequiredException;
import dk.magenta.datafordeler.core.user.DafoUserDetails;

/**
 * Checks access for all entity services related to PERSONNUMMER.
 */
public abstract class CprAccessChecker {

    /**
     * Checks that the user has the READ_CPR_ROLE system role.
     *
     * @param dafoUserDetails the user to check access for
     * @throws AccessDeniedException
     * @throws AccessRequiredException
     */
    public static void checkAccess(DafoUserDetails dafoUserDetails)
            throws AccessDeniedException, AccessRequiredException {
        // Users must have the READ_CPR_ROLE to access PERSONNUMMER data
        dafoUserDetails.checkHasSystemRole(CprRolesDefinition.READ_CPR_ROLE);
    }
}

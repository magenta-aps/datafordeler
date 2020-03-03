package dk.magenta.dafosts.library.users;

import java.util.Collection;

/**
 * Created by jubk on 03-05-2017.
 */
public interface DafoUserData {

    String getUsername();

    int getAccessAccountId();

    String getOnBehalfOf();

    Collection<String> getUserProfiles();

    String getNameQualifier();
}

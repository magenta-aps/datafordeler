package dk.magenta.dafosts.library.users;

/**
 * Created by jubk on 22-05-2017.
 */
public interface DafoCertificateUserDetails extends DafoUserData {
    public int getIdentificationMode();
    public void setOnBehalfOf(String onBehalfOf);
}

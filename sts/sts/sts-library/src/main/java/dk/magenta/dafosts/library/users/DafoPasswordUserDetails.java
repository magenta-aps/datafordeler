package dk.magenta.dafosts.library.users;

import com.nimbusds.jose.util.Base64;
import dk.magenta.dafosts.library.DatabaseQueryManager;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

/**
 * Stores data about a DAFO password user.
 */
public class DafoPasswordUserDetails implements DafoUserData {
    int userId;
    int status;
    String givenName;
    String lastName;
    String emailAddress;
    String organisation;
    String encrypted_password;
    String password_salt;

    DatabaseQueryManager databaseQueryManager;

    public DafoPasswordUserDetails(
            int userId, int status, String givenName, String lastName, String emailAddress,
            String organisation, String encrypted_password, String password_salt,
            DatabaseQueryManager databaseQueryManager) {
        this.userId = userId;
        this.status = status;
        this.givenName = givenName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.organisation = organisation;
        this.encrypted_password = encrypted_password;
        this.password_salt = password_salt;
        this.databaseQueryManager = databaseQueryManager;
    }

    public int getUserId() {
        return userId;
    }

    public int getStatus() {
        return status;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getOrganisation() {
        return organisation;
    }

    public String getEncrypted_password() {
        return encrypted_password;
    }

    public String getPassword_salt() {
        return password_salt;
    }

    /**
     * Is the account for the user active?
     * @return True of the account is active, false otherwise
     */
    public boolean isActive() {
        return status == DatabaseQueryManager.ACCESS_ACCOUNT_STATUS_ACTIVE;
    }

    public boolean checkPassword(String password) {
        // Check password and return invalid user ID if it does not match
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String saltedPassword = password + password_salt;

            md.update(saltedPassword.getBytes("UTF-8"));
            String testEncryptedPassword = Base64.encode(md.digest()).toString();

            return testEncryptedPassword.equals(encrypted_password);
        }
        catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
        catch(UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int getAccessAccountId() {
        return userId;
    }

    @Override
    public String getUsername() {
        return emailAddress;
    }

    @Override
    public String getOnBehalfOf() {
        return null;
    }

    @Override
    public Collection<String> getUserProfiles() {
        return databaseQueryManager.getUserProfiles(userId);
    }

    @Override
    public String getNameQualifier() {
        return "<none>";
    }
}

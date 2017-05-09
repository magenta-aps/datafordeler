package dk.magenta.dafosts.library;

import java.util.ArrayList;
import java.util.Collection;

import dk.magenta.dafosts.library.users.DafoPasswordUserDetails;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.util.List;

public class DatabaseQueryManager {

    private JdbcTemplate jdbcTemplate;

    public static int INVALID_USER_ID = -1;
    public static int ACCESS_ACCOUNT_STATUS_ACTIVE = 1;

    public DatabaseQueryManager(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<String> getLocalIdpUsers() {
        ArrayList<String> result = new ArrayList<>();

        SqlRowSet rows = jdbcTemplate.queryForRowSet(
                "SELECT [dafousers_passworduser].[email] FROM [dafousers_passworduser]"
        );

        while(rows.next()) {
            result.add(rows.getString(1));
            System.out.println(rows.getString(1));
        }

        return result;
    }

    public int getUserIdByCertificateData(String fingerprint) {
        int result = INVALID_USER_ID;

        SqlRowSet rows = jdbcTemplate.queryForRowSet(
                String.join("\n",
                        "SELECT",
                        "   [dafousers_accessaccount].[id]",
                        "FROM",
                        "   [dafousers_certificateuser]",
                        "   INNER JOIN [dafousers_accessaccount] ON (",
                        "       [dafousers_certificateuser].[accessaccount_ptr_id] = [dafousers_accessaccount].[id]",
	                    "   )",
                        "   INNER JOIN [dafousers_certificateuser_certificates] ON (",
                        "       [dafousers_certificateuser].[accessaccount_ptr_id] = " +
                                "[dafousers_certificateuser_certificates].[certificateuser_id]",
	                    "   )",
                        "   INNER JOIN [dafousers_certificate] ON (",
                        "       [dafousers_certificateuser_certificates].[certificate_id] = " +
                                "[dafousers_certificate].[id]",
	                    "   )",
                        "WHERE (",
                        "   [dafousers_accessaccount].[status] = ?",
                        "   AND",
                        "   [dafousers_certificate].[fingerprint] = ?",
                        ")"
                ),
                new Object[] {ACCESS_ACCOUNT_STATUS_ACTIVE, fingerprint}
        );
        if(rows.next()) {
            result = rows.getInt(1);
        }

        return result;
    }

    public DafoPasswordUserDetails getDafoPasswordUserByUsername(String username) {

        SqlRowSet rows = jdbcTemplate.queryForRowSet(
                String.join("\n",
                        "SELECT",
                        "   [dafousers_accessaccount].[id],",
                        "   [dafousers_accessaccount].[status],",
                        "   [dafousers_passworduser].[givenname],",
                        "   [dafousers_passworduser].[lastname],",
                        "   [dafousers_passworduser].[email],",
                        "   [dafousers_passworduser].[organisation],",
                        "   [dafousers_passworduser].[encrypted_password],",
                        "   [dafousers_passworduser].[password_salt]",
                        "FROM",
                        "   [dafousers_passworduser] INNER JOIN",
                        "   [dafousers_accessaccount] ON (",
                        "       [dafousers_passworduser].[accessaccount_ptr_id] =",
                        "           [dafousers_accessaccount].[id]",
                        "   )",
                        "WHERE",
                        "   [dafousers_passworduser].[email] = ?"
                ),
                new Object[] {username}
        );
        if(rows.next()) {
            return new DafoPasswordUserDetails(
                    rows.getInt(1), // UserId
                    rows.getInt(2), // Status
                    rows.getString( 3), // givenName
                    rows.getString(4), // lastName
                    rows.getString(5), // email
                    rows.getString(6), // organisation
                    rows.getString(7), // encrypted password
                    rows.getString(8), // salt
                    this
            );
        }

        return null;
    }

    public Collection<String> getUserProfiles(int accessAccountId) {
        ArrayList<String> result = new ArrayList<>();

        SqlRowSet rows = jdbcTemplate.queryForRowSet(
                String.join("\n",
                        "SELECT",
                        "   [dafousers_userprofile].[name]",
                        "FROM",
                        "   [dafousers_userprofile]",
                        "   INNER JOIN [dafousers_accessaccount_user_profiles] ON (",
                        "       [dafousers_userprofile].[id] = ",
                        "           [dafousers_accessaccount_user_profiles].[userprofile_id]",
                        "   )",
                        "WHERE",
                        "   [dafousers_accessaccount_user_profiles].[accessaccount_id] = ?"
                ),
                new Object[] {accessAccountId}
        );
        while(rows.next()) {
            result.add(rows.getString(1));
        }
        return result;
    }
}

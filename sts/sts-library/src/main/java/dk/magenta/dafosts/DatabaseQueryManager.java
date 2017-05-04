package dk.magenta.dafosts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.sql.ResultSet;
import java.util.List;

public class DatabaseQueryManager {

    private JdbcTemplate jdbcTemplate;

    public static int INVALID_CERTIFICATE_USER_ID = -1;
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

    public int getCertificateUserIdByCertificateData(String fingerprint) {
        int result = INVALID_CERTIFICATE_USER_ID;

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

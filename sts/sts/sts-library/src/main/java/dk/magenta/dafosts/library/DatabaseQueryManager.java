package dk.magenta.dafosts.library;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

import dk.magenta.dafosts.library.users.DafoPasswordUserDetails;
import dk.magenta.dafosts.library.users.DafoUserData;
import org.opensaml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import javax.servlet.http.HttpServletRequest;

public class DatabaseQueryManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseQueryManager.class);

    private JdbcTemplate jdbcTemplate;

    public static int INVALID_USER_ID = -1;
    public static int ACCESS_ACCOUNT_STATUS_ACTIVE = 1;

    public static int IDENTIFICATION_MODE_INVALID = -1;
    public static int IDENTIFICATION_MODE_SINGLE_USER = 1;
    public static int IDENTIFICATION_MODE_ON_BEHALF_OF = 2;

    public static int INVALID_TOKEN_ID = -1;

    // If updating this, also update django/dafousers/model_constants.py in the DAFO-admin project.
    public static final String IDP_UPDATE_TIMESTAMP_NAME = "LAST_IDP_UPDATE";

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
        }

        return result;
    }

    public String getUserIdentificationByAccountId(int accountId) {
        String result = null;
        SqlRowSet rows = jdbcTemplate.queryForRowSet(
                String.join("\n",
                    "SELECT [dafousers_useridentification].[user_id] ",
                        "FROM [dafousers_useridentification] INNER JOIN [dafousers_accessaccount] ON (",
                        "    [dafousers_useridentification].[id] = [dafousers_accessaccount].[identified_user_id]",
                        ")",
                        "WHERE [dafousers_accessaccount].[id] = ?"
                ),
                new Object[] {accountId}
        );
        if(rows.next()) {
            result = rows.getString(1);
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
                        "   [dafousers_certificate].[fingerprint] = ?",
                        ")"
                ),
                new Object[] {fingerprint}
        );
        if(rows.next()) {
            result = rows.getInt(1);
        }

        return result;
    }

    public boolean isAccessAccountActive(int accessAccountId) {
        boolean result = false;

        SqlRowSet rows = jdbcTemplate.queryForRowSet(
                String.join("\n",
                        "SELECT",
                        "   [dafousers_accessaccount].[id]",
                        "FROM",
                        "   [dafousers_accessaccount] ",
                        "WHERE (",
                        "   [dafousers_accessaccount].[id] = ? ",
                        "   AND ",
                        "   [dafousers_accessaccount].[status] = ?",
                        ")"
                ),
                new Object[] {accessAccountId, ACCESS_ACCOUNT_STATUS_ACTIVE}
        );
        if(rows.next()) {
            if(rows.getInt(1) == accessAccountId) {
                result = true;
            }
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


    public int getCertUserIdentificationMode(int accessAccountId) {
        int result = IDENTIFICATION_MODE_INVALID;
        SqlRowSet rows = jdbcTemplate.queryForRowSet(
                String.join("\n",
                        "SELECT ",
                        "[dafousers_certificateuser].[identification_mode] ",
                        "FROM [dafousers_certificateuser] ",
                        "WHERE [dafousers_certificateuser].[accessaccount_ptr_id] = ?"
                ),
                new Object[] {accessAccountId}
        );
        if(rows.next()) {
            result = rows.getByte(1);
        }

        return result;
    }

    public int getAccountIdByEntityId(String entityID) {
        SqlRowSet rows = jdbcTemplate.queryForRowSet(
                "SELECT [dafousers_accessaccount].[id] " +
                        "FROM [dafousers_accessaccount] " +
                        "INNER JOIN [dafousers_identityprovideraccount] ON (" +
                        "   [dafousers_accessaccount].[id] = " +
                        "       [dafousers_identityprovideraccount].[accessaccount_ptr_id]" +
                        ") " +
                        "WHERE [dafousers_identityprovideraccount].[idp_entity_id] = ?",
                new Object[] {entityID}
        );
        if(rows.next()) {
            return rows.getInt(1);
        }

        return INVALID_USER_ID;
    }


    public Map<String, LocalDateTime> getIdPUpdateMap() {
        Map<String, LocalDateTime> results = new HashMap<>();
        SqlRowSet rows = jdbcTemplate.queryForRowSet(
                    "SELECT " +
                            "   [dafousers_identityprovideraccount].[updated], " +
                            "   [dafousers_identityprovideraccount].[idp_entity_id] " +
                            "FROM " +
                            "   [dafousers_identityprovideraccount]" +
                            "   INNER JOIN" +
                            "   [dafousers_accessaccount] ON (" +
                            "       [dafousers_identityprovideraccount].[accessaccount_ptr_id] =" +
                            "        [dafousers_accessaccount].[id]" +
                            "   )" +
                            "WHERE " +
                            "   [dafousers_accessaccount].[status] = ?",
                new Object[] {ACCESS_ACCOUNT_STATUS_ACTIVE}
        );
        while(rows.next()) {
            results.put(rows.getString(2), rows.getTimestamp(1).toLocalDateTime());
        }

        return results;
    }

    public DafoIdPData getIdPDataByName(String identityId) {
        DafoIdPData result = null;
        SqlRowSet rows = jdbcTemplate.queryForRowSet(
                "SELECT " +
                        "   [dafousers_accessaccount].[id], " +
                        "   [dafousers_accessaccount].[status], " +
                        "   [dafousers_identityprovideraccount].[updated], " +
                        "   [dafousers_identityprovideraccount].[name], " +
                        "   [dafousers_identityprovideraccount].[idp_entity_id], " +
                        "   [dafousers_identityprovideraccount].[idp_type], " +
                        "   [dafousers_identityprovideraccount].[metadata_xml], " +
                        "   [dafousers_identityprovideraccount].[userprofile_attribute], " +
                        "   [dafousers_identityprovideraccount].[userprofile_attribute_format], " +
                        "   [dafousers_identityprovideraccount].[userprofile_adjustment_filter_type], " +
                        "   [dafousers_identityprovideraccount].[userprofile_adjustment_filter_value] " +
                        "FROM " +
                        "   [dafousers_identityprovideraccount]" +
                        "   INNER JOIN" +
                        "   [dafousers_accessaccount] ON (" +
                        "       [dafousers_identityprovideraccount].[accessaccount_ptr_id] =" +
                        "        [dafousers_accessaccount].[id]" +
                        "   )" +
                        "WHERE" +
                        "   [dafousers_identityprovideraccount].[idp_entity_id] = ?",
                new Object[] {identityId}
        );
        if(rows.next()) {
            result = new DafoIdPData(
                    rows.getInt(1),
                    rows.getInt(2),
                    rows.getTimestamp(3).toLocalDateTime(),
                    rows.getString(4),
                    rows.getString(5),
                    rows.getInt(6),
                    rows.getString(7),
                    rows.getString(8),
                    rows.getInt(9),
                    rows.getInt(10),
                    rows.getString(11)
            );
        }

        return result;
    }

    public LocalDateTime getLastIdPUpdate() {
        LocalDateTime result = null;
        SqlRowSet rows = jdbcTemplate.queryForRowSet(
        "SELECT " +
                "[dafousers_updatetimestamps].[updated] " +
                "FROM" +
                "   [dafousers_updatetimestamps]" +
                "WHERE" +
                "   [dafousers_updatetimestamps].[name] = ?",
                new Object[] { IDP_UPDATE_TIMESTAMP_NAME }
        );
        if(rows.next()) {
            Timestamp tmp = rows.getTimestamp(1);
            if(tmp != null) {
                result = tmp.toLocalDateTime();
            }
        }
        if(result == null) {
            // Provide a reasonable static default far in the past
            result = LocalDateTime.of(2000, 1,1,1,1,1);
        }
        return result;
    }

    public int registerPendingToken(DafoUserData user, HttpServletRequest request) {
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(
                    connection -> {
                        @SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
                        final String query = "" +
                                "INSERT INTO [dafousers_issuedtoken] (" +
                                "   [access_account_id], " +
                                "   [issued_time], " +
                                "   [token_nameid], " +
                                "   [token_on_behalf_of], " +
                                "   [request_service_url], " +
                                "   [client_ip], " +
                                "   [client_user_agent]" +
                                ")" +
                                "VALUES (?, ?, ?, ?, ?, ?, ?)";

                        String remoteAddr = request.getHeader("X-Forwarded-For");
                        if(remoteAddr == null) {
                            remoteAddr = request.getRemoteAddr();
                        }

                        PreparedStatement preparedStatement = connection.prepareStatement(
                                query, Statement.RETURN_GENERATED_KEYS
                        );
                        preparedStatement.setInt(
                                1,
                                user.getAccessAccountId() == INVALID_USER_ID ? null : user.getAccessAccountId()
                        );
                        preparedStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                        preparedStatement.setString(
                                3,
                                String.format("[%s]@[%s]", user.getUsername(), user.getNameQualifier())
                        );
                        preparedStatement.setString(4, user.getOnBehalfOf());
                        preparedStatement.setString(5, request.getRequestURL().toString());
                        preparedStatement.setString(6, remoteAddr);
                        preparedStatement.setString(7, request.getHeader("User-Agent"));

                        return preparedStatement;
                    },
                    generatedKeyHolder
            );
        }
        catch(Exception e) {
            logger.warn("Failed to insert pending token: " + e.getMessage());
            return INVALID_TOKEN_ID;
        }

        return generatedKeyHolder.getKey().intValue();
    }

    public void updateRegisteredToken(Assertion assertion, String textValue) {
        @SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
        final String query =
                "UPDATE [dafousers_issuedtoken] " +
                "SET " +
                "   [issued_time] = ?, " +
                "   [token_data] = ? " +
                "WHERE " +
                "   [id] = ?";

        int tokenId = LogRequestWrapper.getTokenIdFromToken(assertion);
        if(tokenId == INVALID_TOKEN_ID) {
            return;
        }

        jdbcTemplate.update(
                query,
                new java.sql.Timestamp(assertion.getIssueInstant().getMillis()),
                textValue,
                tokenId
        );
    }
}

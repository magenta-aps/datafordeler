package dk.magenta.datafordeler.core.user;

import dk.magenta.datafordeler.core.arearestriction.AreaRestriction;
import dk.magenta.datafordeler.core.arearestriction.AreaRestrictionType;
import dk.magenta.datafordeler.core.role.SystemRole;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handles queries for accessing data in the DAFO user database.
 */
@Component
public class UserQueryManagerImpl extends UserQueryManager {

    private final JdbcTemplate jdbcTemplate;

    public UserQueryManagerImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int getUserProfileIdByName(String name) {
        SqlRowSet rows = jdbcTemplate.queryForRowSet(
                "SELECT "
                        + " [dafousers_userprofile].[id]"
                        + "FROM"
                        + " [dafousers_userprofile]"
                        + "WHERE"
                        + " [dafousers_userprofile].[name] = ?",
                name);
        if (rows.next()) {
            return rows.getInt(1);
        } else {
            return INVALID_USERPROFILE_ID;
        }
    }

    @Override
    public List<String> getSystemRoleNamesByUserProfileId(int databaseId) {
        List<String> result = new ArrayList<>();
        SqlRowSet rows = jdbcTemplate.queryForRowSet(
                "SELECT"
                        + " [dafousers_systemrole].[role_name]"
                        + "FROM"
                        + " [dafousers_systemrole]"
                        + " INNER JOIN [dafousers_userprofile_system_roles] ON ("
                        + "   [dafousers_systemrole].[id] ="
                        + "      [dafousers_userprofile_system_roles].[systemrole_id]"
                        + ")"
                        + " INNER JOIN [dafousers_userprofile] ON ("
                        + "   [dafousers_userprofile_system_roles].[userprofile_id] ="
                        + "        [dafousers_userprofile].[id]"
                        + ")"
                        + " WHERE [dafousers_userprofile].[id] = ?",
                databaseId);
        while (rows.next()) {
            result.add(rows.getString(1));
        }
        return result;
    }

    @Override
    public List<AreaRestriction> getAreaRestrictionsByUserProfileId(int databaseId) {

        List<AreaRestriction> result = new ArrayList<>();
        SqlRowSet rows = jdbcTemplate.queryForRowSet(
                "SELECT"
                        + " [dafousers_arearestriction].[name], "
                        + " [areatype].[name], "
                        + " [areatype].[service_name] "
                        + "FROM"
                        + " [dafousers_arearestriction]"
                        + " INNER JOIN"
                        + " [dafousers_userprofile_area_restrictions] ON ("
                        + "   [dafousers_arearestriction].[id] = "
                        + "     [dafousers_userprofile_area_restrictions].[arearestriction_id]"
                        + " )"
                        + " INNER JOIN [dafousers_userprofile] ON ("
                        + "   [dafousers_userprofile_area_restrictions].[userprofile_id] ="
                        + "    [dafousers_userprofile].[id]"
                        + " )"
                        + " INNER JOIN [dafousers_arearestrictiontype] areatype ON ("
                        + "   [dafousers_arearestriction].[area_restriction_type_id] = [areatype].[id]"
                        + " )"
                        + "WHERE"
                        + "  [dafousers_userprofile].[id] = ?",
                databaseId);
        while (rows.next()) {
            AreaRestriction area = AreaRestriction.lookup(
                    rows.getString(3) + ":" +
                            rows.getString(2) + ":" +
                            rows.getString(1)
            );
            if (area != null) {
                result.add(area);
            } else {
                // TODO: Log warning about unkown areatype being mentioned in token
            }
        }
        return result;
    }

    @Override
    public Set<String> getAllStoredSystemRoleNames() {
        HashSet result = new HashSet<>();
        SqlRowSet rows = jdbcTemplate.queryForRowSet(
                "SELECT"
                        + " [dafousers_systemrole].[role_name]"
                        + "FROM"
                        + " [dafousers_systemrole]"
        );
        while (rows.next()) {
            result.add(rows.getString(1));
        }
        return result;
    }

    @Override
    public void insertSystemRole(SystemRole systemRole) {
        String parentName = null;
        if (systemRole.getParent() != null) {
            parentName = systemRole.getParent().getRoleName();
        }
        jdbcTemplate.update(
                "INSERT INTO [dafousers_systemrole]" +
                        "([role_name], [role_type], [target_name], [parent_id]) " +
                        "VALUES (?, ?, ?, " +
                        "(SELECT TOP 1 [id] FROM [dafousers_systemrole] WHERE [role_name] = ?)" +
                        ")",
                systemRole.getRoleName(),
                systemRole.getType().getNumericValue(),
                systemRole.getTargetName(),
                parentName
        );
    }

    @Override
    public Set<String> getAllAreaRestrictionTypeLookupNames() {
        HashSet result = new HashSet<>();
        SqlRowSet rows = jdbcTemplate.queryForRowSet(
                "SELECT"
                        + " [dafousers_arearestrictiontype].[name],"
                        + " [dafousers_arearestrictiontype].[service_name]"
                        + "FROM"
                        + " [dafousers_arearestrictiontype]"
        );
        while (rows.next()) {
            result.add(rows.getString(2) + ":" + rows.getString(1));
        }
        return result;
    }

    @Override
    public Set<String> getAllAreaRestrictionLookupNames() {
        HashSet result = new HashSet<>();
        SqlRowSet rows = jdbcTemplate.queryForRowSet(
                "SELECT"
                        + " [area].[name],"
                        + " [areatype].[name],"
                        + " [areatype].[service_name]"
                        + "FROM"
                        + " [dafousers_arearestriction] area"
                        + " INNER JOIN [dafousers_arearestrictiontype] areatype ON ("
                        + "   [area].[area_restriction_type_id] = [areatype].[id]"
                        + " )"
        );
        while (rows.next()) {
            result.add(
                    rows.getString(3) + ":" +
                            rows.getString(2) + ":" +
                            rows.getString(1)
            );
        }
        return result;
    }

    @Override
    public void insertAreaRestrictionType(AreaRestrictionType areaRestrictionType) {
        jdbcTemplate.update(
                "INSERT INTO [dafousers_arearestrictiontype] " +
                        "([name], [description], [service_name]) " +
                        "VALUES (?, ?, ?)",
                areaRestrictionType.getName(),
                areaRestrictionType.getDescription(),
                areaRestrictionType.getServiceName()
        );
    }

    @Override
    public void insertAreaRestriction(AreaRestriction areaRestriction) {
        String typeName = null;
        String serviceTypeName = null;
        if (areaRestriction.getType() != null) {
            typeName = areaRestriction.getType().getName();
            serviceTypeName = areaRestriction.getType().getServiceName();
        }
        jdbcTemplate.update(
                "INSERT INTO [dafousers_arearestriction] "
                        + "([name], [description], [sumiffiik], [area_restriction_type_id]) "
                        + "VALUES "
                        + " (?, ?, ?, "
                        + "   (SELECT TOP 1"
                        + "     [id] "
                        + "   FROM"
                        + "     [dafousers_arearestrictiontype] "
                        + "   WHERE"
                        + "     [name] = ? AND "
                        + "     [service_name] = ?"
                        + "   )"
                        + " )",
                areaRestriction.getName(),
                areaRestriction.getDescription(),
                areaRestriction.getSumifiik(),
                typeName,
                serviceTypeName
        );
    }

    @Override
    public void checkConnection() {
        SqlRowSet rows = jdbcTemplate.queryForRowSet("SELECT 1");
        if (rows.next()) {
            rows.getInt(1);
        }
    }
}

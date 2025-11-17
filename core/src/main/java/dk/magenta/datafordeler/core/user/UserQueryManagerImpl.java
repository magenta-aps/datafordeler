package dk.magenta.datafordeler.core.user;

import dk.magenta.datafordeler.core.arearestriction.AreaRestriction;
import dk.magenta.datafordeler.core.arearestriction.AreaRestrictionType;
import dk.magenta.datafordeler.core.database.UserSessionManager;
import dk.magenta.datafordeler.core.role.SystemRole;
import jakarta.persistence.NoResultException;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private UserSessionManager userSessionManager;

    @Override
    public int getUserProfileIdByName(String name) {
        try (Session session = this.userSessionManager.getSessionFactory().openSession()) {
            NativeQuery<Integer> query = session.createNativeQuery(
                "SELECT userprofile.id " +
                        "FROM dafousers_userprofile as userprofile " +
                        "WHERE userprofile.name = ?1", Integer.class
                );
            query.setParameter(1, name);
            try {
                return (Integer) query.getSingleResult();
            } catch (NoResultException e) {
                return INVALID_USERPROFILE_ID;
            }
        }
    }

    @Override
    public List<String> getSystemRoleNamesByUserProfileId(int databaseId) {
        List<String> result = new ArrayList<>();
        try (Session session = this.userSessionManager.getSessionFactory().openSession()) {
            NativeQuery<String> query = session.createNativeQuery(
                    "SELECT systemrole.role_name " +
                            "FROM dafousers_systemrole as systemrole " +

                            "INNER JOIN dafousers_userprofile_system_roles as userprofile_system_roles " +
                            "ON systemrole.id = userprofile_system_roles.systemrole_id " +

                            "INNER JOIN dafousers_userprofile as userprofile " +
                            "ON userprofile_system_roles.userprofile_id = userprofile.id " +

                            "WHERE userprofile.id = ?1",
                    String.class
            );
            query.setParameter(1, databaseId);
            query.getResultList().forEach(result::add);
        }
        return result;
    }

    @Override
    public List<AreaRestriction> getAreaRestrictionsByUserProfileId(int databaseId) {
        List<AreaRestriction> result = new ArrayList<>();
        try (Session session = this.userSessionManager.getSessionFactory().openSession()) {
            NativeQuery<Object[]> query = session.createNativeQuery(
                    "SELECT arearestriction.name, arearestrictiontype.name, arearestrictiontype.service_name " +
                            "FROM dafousers_arearestriction as arearestriction " +

                            "INNER JOIN dafousers_userprofile_area_restrictions as userprofile_area_restrictions " +
                            "ON arearestriction.id = userprofile_area_restrictions.arearestriction_id " +

                            "INNER JOIN dafousers_userprofile as userprofile " +
                            "ON userprofile_area_restrictions.userprofile_id = userprofile.id " +

                            "INNER JOIN dafousers_arearestrictiontype as arearestrictiontype " +
                            "ON arearestriction.area_restriction_type_id = arearestrictiontype.id " +

                            "WHERE userprofile.id = ?1"
            );
            query.setParameter(1, databaseId);
            List<Object[]> rows = query.getResultList();
            for (Object[] row : rows) {
                String areaRestrictionName = (String) row[0];
                String areaTypeName = (String) row[1];
                String serviceName = (String) row[2];
                AreaRestriction area = AreaRestriction.lookup(
                        serviceName + ":" + areaTypeName + ":" + areaRestrictionName
                );
                if (area != null) {
                    result.add(area);
                } else {
                    // TODO: Log warning about unknown areatype being mentioned in token
                }
            }
        }
        return result;
    }

    @Override
    public Set<String> getAllStoredSystemRoleNames() {
        try (Session session = this.userSessionManager.getSessionFactory().openSession()) {
            NativeQuery<String> query = session.createNativeQuery(
                    "SELECT systemrole.role_name " +
                            "FROM dafousers_systemrole as systemrole",
                    String.class
            );
            return new HashSet<>(query.getResultList());
        }
    }

    @Override
    public void insertSystemRole(SystemRole systemRole) {
        try (Session session = this.userSessionManager.getSessionFactory().openSession()) {
            NativeQuery query = session.createNativeQuery(
                    "INSERT INTO dafousers_systemrole " +
                            "(role_name, role_type, target_name, parent_id) " +
                            "VALUES (" +
                            "?1, ?2, ?3, " +
                            "(SELECT TOP 1 id FROM dafousers_systemrole WHERE role_name = ?4)" +
                            ")"
            );
            query.setParameter(1, systemRole.getRoleName());
            query.setParameter(2, systemRole.getType().getNumericValue());
            query.setParameter(3, systemRole.getTargetName());
            query.setParameter(4, systemRole.getParent() != null ? systemRole.getParent().getRoleName() : null);
            query.executeUpdate();
        }
    }

    @Override
    public Set<String> getAllAreaRestrictionTypeLookupNames() {
        HashSet<String> result = new HashSet<>();
        try (Session session = this.userSessionManager.getSessionFactory().openSession()) {
            NativeQuery<Object[]> query = session.createNativeQuery(
                    "SELECT arearestrictiontype.name, arearestrictiontype.service_name " +
                            "FROM dafousers_arearestrictiontype as arearestrictiontype"
            );
            List<Object[]> rows = query.getResultList();
            for (Object[] row : rows) {
                result.add(row[1] + ":" + row[0]);
            }
        }
        return result;
    }

    @Override
    public Set<String> getAllAreaRestrictionLookupNames() {
        HashSet<String> result = new HashSet<>();
        try (Session session = this.userSessionManager.getSessionFactory().openSession()) {
            NativeQuery<Object[]> query = session.createNativeQuery(
                    "SELECT arearestriction.name, areatype.name, areatype.service_name " +
                            "FROM dafousers_arearestriction arearestriction " +

                            "INNER JOIN dafousers_arearestrictiontype as areatype " +
                            "ON arearestriction.area_restriction_type_id = areatype.id"
            );
            List<Object[]> rows = query.getResultList();
            for (Object[] row : rows) {
                result.add(row[2] + ":" + row[1] + ":" + row[0]);
            }
        }
        return result;
    }

    @Override
    public void insertAreaRestrictionType(AreaRestrictionType areaRestrictionType) {
        try (Session session = this.userSessionManager.getSessionFactory().openSession()) {
            NativeQuery query = session.createNativeQuery(
                    "INSERT INTO dafousers_arearestrictiontype " +
                    "(name, description, service_name) " +
                    "VALUES (?1, ?2, ?3)");
            query.setParameter(1, areaRestrictionType.getName());
            query.setParameter(2, areaRestrictionType.getDescription());
            query.setParameter(3, areaRestrictionType.getServiceName());
            query.executeUpdate();
        }
    }

    @Override
    public void insertAreaRestriction(AreaRestriction areaRestriction) {
        String typeName = null;
        String serviceTypeName = null;
        if (areaRestriction.getType() != null) {
            typeName = areaRestriction.getType().getName();
            serviceTypeName = areaRestriction.getType().getServiceName();
        }
        try (Session session = this.userSessionManager.getSessionFactory().openSession()) {
            NativeQuery query = session.createNativeQuery(
                    "INSERT INTO dafousers_arearestriction "
                            + "(name, description, sumiffiik, area_restriction_type_id) "
                            + "VALUES "
                            + " (?1, ?2, ?3, "
                            + "   (SELECT TOP 1 id "
                            + "   FROM dafousers_arearestrictiontype "
                            + "   WHERE name = ?4 AND service_name = ?5"
                            + "   )"
                            + " )"
            );
            query.setParameter(1, areaRestriction.getName());
            query.setParameter(2, areaRestriction.getDescription());
            query.setParameter(3, areaRestriction.getSumifiik());
            query.setParameter(4, typeName);
            query.setParameter(5, serviceTypeName);
            query.executeUpdate();
        }
    }

    @Override
    public void checkConnection() {
        try (Session session = this.userSessionManager.getSessionFactory().openSession()) {
            NativeQuery<Integer> query = session.createNativeQuery("SELECT 1", Integer.class);
            query.getSingleResult();
        }
    }
}

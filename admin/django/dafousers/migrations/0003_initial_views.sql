-- This file contains views that allows WSO2 to use the database as a read-only
-- use storage.

IF EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[dbo].[UM_USER]') AND TYPE IN (N'V'))
DROP VIEW UM_USER;
GO

CREATE VIEW UM_USER AS
SELECT
    [dafousers_accessaccount].[id] UM_ID,
    [dafousers_passworduser].[email] UM_USER_NAME,
    [dafousers_passworduser].[encrypted_password] UM_USER_PASSWORD,
    [dafousers_passworduser].[password_salt] UM_SALT_VALUE,
	0 UM_REQUIRE_CHANGE,
    [dafousers_passworduser].[updated] UM_CHANGED_TIME,
	-1234 UM_TENANT_ID
FROM
    [dafousers_passworduser] INNER JOIN [dafousers_accessaccount] ON (
        [dafousers_passworduser].[accessaccount_ptr_id] = [dafousers_accessaccount].[id]
    );
GO

IF EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[dbo].[UM_ROLE]') AND TYPE IN (N'V'))
DROP VIEW UM_ROLE;
GO

CREATE VIEW UM_ROLE AS
SELECT
	[dafousers_systemrole].[id] UM_ID,
	[dafousers_systemrole].[role_name] UM_ROLE_NAME,
	-1234 UM_TENANT_ID,
	0 UM_SHARED_ROLE
FROM
	[dafousers_systemrole];

GO

IF EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[dbo].[UM_USER_ROLE]') AND TYPE IN (N'V'))
DROP VIEW UM_USER_ROLE;
GO

CREATE VIEW UM_USER_ROLE AS
SELECT
	0 UM_ID,
	[dafousers_systemrole].[id] UM_ROLE_ID,
	[dafousers_passworduser].[accessaccount_ptr_id] UM_USER_ID,
	-1234 UM_TENANT_ID
FROM
	[dafousers_systemrole]
	INNER JOIN [dafousers_userprofile_system_roles] ON (
		[dafousers_systemrole].[id] = [dafousers_userprofile_system_roles].[systemrole_id]
	)
	INNER JOIN [dafousers_userprofile] ON (
		[dafousers_userprofile_system_roles].[userprofile_id] = [dafousers_userprofile].[id]
	)
	INNER JOIN [dafousers_accessaccount_user_profiles] ON (
		[dafousers_userprofile].[id] = [dafousers_accessaccount_user_profiles].[userprofile_id]
	)
	INNER JOIN [dafousers_accessaccount] ON (
		[dafousers_accessaccount_user_profiles].[accessaccount_id] = [dafousers_accessaccount].[id]
	)
	INNER JOIN [dafousers_passworduser] ON (
		[dafousers_accessaccount].[id] = [dafousers_passworduser].[accessaccount_ptr_id]
	);

GO

IF EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[dbo].[UM_SHARED_USER_ROLE]') AND TYPE IN (N'V'))
DROP VIEW UM_SHARED_USER_ROLE;
GO

CREATE VIEW UM_SHARED_USER_ROLE AS
SELECT
    1 UM_ROLE_ID,
    2 UM_USER_ID,
    -1234 UM_USER_TENANT_ID,
    -1234 UM_ROLE_TENANT_ID
WHERE
	1=0;
GO

IF EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[dbo].[UM_TENANT]') AND TYPE IN (N'V'))
DROP VIEW UM_TENANT;
GO

CREATE VIEW UM_TENANT AS
SELECT
    -1234 UM_ID,
    'DATAFORDELER' UM_DOMAIN_NAME,
    'system@data.nanoq.gl' UM_EMAIL,
    1 UM_ACTIVE,
	'2017-04-03 19:30:34.3220000' UM_CREATED_DATE,
	NULL UM_USER_CONFIG
WHERE
	1=1;
GO


IF EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[dbo].[UM_USER_ATTRIBUTE]') AND TYPE IN (N'V'))
DROP VIEW UM_USER_ATTRIBUTE;
GO

CREATE VIEW UM_USER_ATTRIBUTE AS
SELECT
	[dafousers_passworduser].[accessaccount_ptr_id] * 100 + 1 UM_ID,
	'givenName' UM_ATTR_NAME,
	[dafousers_passworduser].[givenname] UM_ATTR_VALUE,
	'default' UM_PROFILE_ID,
	[dafousers_passworduser].[accessaccount_ptr_id] UM_USER_ID,
	-1234 UM_TENANT_ID
FROM
	[dafousers_passworduser]
	INNER JOIN [dafousers_accessaccount] ON (
		[dafousers_passworduser].[accessaccount_ptr_id] = [dafousers_accessaccount].[id]
	)
UNION
SELECT
	[dafousers_passworduser].[accessaccount_ptr_id] * 100 + 2 UM_ID,
	'sn' UM_ATTR_NAME,
	[dafousers_passworduser].[lastname] UM_ATTR_VALUE,
	'default' UM_PROFILE_ID,
	[dafousers_passworduser].[accessaccount_ptr_id] UM_USER_ID,
	-1234 UM_TENANT_ID
FROM
	[dafousers_passworduser]
	INNER JOIN [dafousers_accessaccount] ON (
		[dafousers_passworduser].[accessaccount_ptr_id] = [dafousers_accessaccount].[id]
	)
UNION
SELECT
	[dafousers_passworduser].[accessaccount_ptr_id] * 100 + 3 UM_ID,
	'mail' UM_ATTR_NAME,
	[dafousers_passworduser].[email] UM_ATTR_VALUE,
	'default' UM_PROFILE_ID,
	[dafousers_passworduser].[accessaccount_ptr_id] UM_USER_ID,
	-1234 UM_TENANT_ID
FROM
	[dafousers_passworduser]
	INNER JOIN [dafousers_accessaccount] ON (
		[dafousers_passworduser].[accessaccount_ptr_id] = [dafousers_accessaccount].[id]
	)
UNION
SELECT
	[dafousers_passworduser].[accessaccount_ptr_id] * 100 + 4 UM_ID,
	'organizationName' UM_ATTR_NAME,
	[dafousers_passworduser].[organisation] UM_ATTR_VALUE,
	'default' UM_PROFILE_ID,
	[dafousers_passworduser].[accessaccount_ptr_id] UM_USER_ID,
	-1234 UM_TENANT_ID
FROM
	[dafousers_passworduser]
	INNER JOIN [dafousers_accessaccount] ON (
		[dafousers_passworduser].[accessaccount_ptr_id] = [dafousers_accessaccount].[id]
	)
UNION
SELECT
	[src_users].[accessaccount_ptr_id] * 100 + 5 UM_ID,
	'groups' UM_ATTR_NAME,
	SUBSTRING(
		(
			SELECT
				',' + [dafousers_systemrole].[role_name] AS [text()]
			FROM
				[dafousers_systemrole] INNER JOIN [dafousers_userprofile_system_roles] ON (
					[dafousers_systemrole].[id] = [dafousers_userprofile_system_roles].[systemrole_id]
				)
				INNER JOIN [dafousers_userprofile] ON (
					[dafousers_userprofile_system_roles].[userprofile_id] = [dafousers_userprofile].[id]
				)
				INNER JOIN [dafousers_accessaccount_user_profiles] ON (
					[dafousers_userprofile].[id] = [dafousers_accessaccount_user_profiles].[userprofile_id]
				)
				INNER JOIN [dafousers_accessaccount] ON (
					[dafousers_accessaccount_user_profiles].[accessaccount_id] = [dafousers_accessaccount].[id]
				)
				INNER JOIN [dafousers_passworduser] ON (
					[dafousers_accessaccount].[id] = [dafousers_passworduser].[accessaccount_ptr_id]
				)
			WHERE
				[dafousers_passworduser].[accessaccount_ptr_id] = [src_users].[accessaccount_ptr_id]
			ORDER BY [dafousers_systemrole].[role_name]
			FOR XML PATH('')
		),
        2, 4000
    ) UM_ATTR_VALUE,
	'default' UM_PROFILE_ID,
	[src_users].[accessaccount_ptr_id] UM_USER_ID,
	-1234 UM_TENANT_ID
FROM
	[dafousers_passworduser] [src_users];

GO

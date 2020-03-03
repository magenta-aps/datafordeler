# -*- coding: utf-8 -*-
from __future__ import unicode_literals
import pytz
import datetime

from dafousers import model_constants
from django.db import migrations




def add_defaults(apps, schema_editor, modelname, items):
    # We get the model from the versioned app registry;
    # if we directly import it, it'll be the wrong version
    model = apps.get_model(modelname)
    dbh = model.objects.using(schema_editor.connection.alias)
    for x in items:
        if not dbh.filter(**x).exists():
            obj = dbh.create(**x)
            obj.save()

def remove_defaults(apps, schema_editor, modelname, items):
    # forwards_func() creates two Country instances,
    # so reverse_func() should delete them.
    model = apps.get_model(modelname)
    dbh = model.objects.using(schema_editor.connection.alias)
    for x in items:
        dbh.filter(**x).delete()


def add_m2m(apps, schema_editor, mname1, attr, mname2, data):
    model1 = apps.get_model(mname1).objects.using(
        schema_editor.connection.alias
    )
    model2 = apps.get_model(mname2).objects.using(
        schema_editor.connection.alias
    )
    for src, dest in data:
        getattr(model1.get(**src), attr).add(model2.get(**dest))

users = [
    {
        "username": "admin",
        "password": "".join([
            u'pbkdf2_sha256$30000$dq8xF1RP9GHP$/',
            u'JoNLrL5PdcXK+Eflso2UlWO7W0LesHf/QCBR/6WXRE='
        ]),
        "is_active": True,
        "is_staff": True,
        "is_superuser": True,
    },
    {
        "username": "dafoadmin",
        "password": "".join([
            u'pbkdf2_sha256$30000$TrP4nKsj7OoX$sI',
            u'oH53kF23A7o80XJGlV91iyd/qo80qhYrFi+NduY4Q='
        ]),
        "is_active": True,
        "is_staff": True,
        "is_superuser": True,
    }
]


def add_users(apps, schema_editor):
    add_defaults(apps, schema_editor, "auth.User", users)


def remove_users(apps, schema_editor):
    remove_defaults(apps, schema_editor, "auth.User", users)


system_roles = [
    {
        "role_name": "DAFO Administrator",
        "role_type": model_constants.SystemRole.TYPE_CUSTOM,
        "target_name": "DAFO Admin",
    },
    {
        "role_name": "DAFO Serviceudbyder",
        "role_type": model_constants.SystemRole.TYPE_CUSTOM,
        "target_name": "DAFO Admin",
    }
]


def add_roles(apps, schema_editor):
    add_defaults(apps, schema_editor, "dafousers.SystemRole", system_roles)

def remove_roles(apps, schema_editor):
    remove_defaults(apps, schema_editor, "dafousers.SystemRole", system_roles)


userprofiles = [
    {
        "name": "DAFO Administrator",
        "created_by": "system@data.nanoq.gl",
    },
    {
        "name": "DAFO Serviceudbyder",
        "created_by": "system@data.nanoq.gl",
    }
]


def add_userprofiles(apps, schema_editor):
    add_defaults(apps, schema_editor, "dafousers.UserProfile", userprofiles)
    add_m2m(
        apps, schema_editor,
        "dafousers.UserProfile", "system_roles",
        "dafousers.SystemRole",
        [
            (
                {"name": "DAFO Administrator"},
                {"role_name": "DAFO Administrator"}
            ),
            (
                {"name": "DAFO Serviceudbyder"},
                {"role_name": "DAFO Serviceudbyder"}
            ),
        ]

    )
    

def remove_userprofiles(apps, schema_editor):
    remove_defaults(apps, schema_editor, "dafousers.UserProfile", userprofiles)

user_ids = [
    {"user_id": "jacob@data.nanoq.gl"},
    {"user_id": "amalie@serviceudbyder.gl"}
]


def add_useridentifications(apps, schema_editor):
    add_defaults(apps, schema_editor, "dafousers.UserIdentification", user_ids)


def remove_useridentifications(apps, schema_editor):
    remove_defaults(
        apps, schema_editor, "dafousers.UserIdentification", user_ids
    )


create_time = datetime.datetime(
    2017, 4, 1, 12, 0, 0, 0, tzinfo=pytz.UTC
)


def get_password_users(apps, schema_editor):
    model = apps.get_model("dafousers.UserIdentification")
    dbh = model.objects.using(schema_editor.connection.alias)
    return [
        {
            "givenname": "Jakob",
            "lastname": "Administrator",
            "email": "jakob@data.nanoq.gl",
            "person_identification": None,
            "identified_user": dbh.get(user_id="jacob@data.nanoq.gl"),
            "encrypted_password":
                "oqkkxNKw7nVixXoHj6jtNn4dlSmJ+L9Lk7/GBJWA3TE=",
            "password_salt": "eAxwVKDSNpyXsoi/WIJKTA==",
            "status": model_constants.AccessAccount.STATUS_ACTIVE,
            "changed_by": "system@data.nanoq.gl",
            "updated": create_time,
        },
        {
            "givenname": "Amalie",
            "lastname": "Serviceudbyder",
            "email": "amalie@serviceudbyder.gl",
            "person_identification": None,
            "identified_user": dbh.get(user_id="amalie@serviceudbyder.gl"),
            "encrypted_password":
                "9QTHekiEX59S7GqF5Za0X7ezZ1E4rXq2/1cvIRbTqfE=",
            "password_salt": "eAxwVKDSNpyXsoi/WIJKTA==",
            "status": model_constants.AccessAccount.STATUS_ACTIVE,
            "changed_by": "system@data.nanoq.gl",
            "updated": create_time,
        },
    ]


def add_password_users(apps, schema_editor):
    password_users = get_password_users(apps, schema_editor)
    add_defaults(apps, schema_editor, "dafousers.PasswordUser", password_users)
    add_m2m(
        apps, schema_editor,
        "dafousers.PasswordUser", "user_profiles",
        "dafousers.UserProfile",
        (
            (
                {"email": "jakob@data.nanoq.gl"},
                {"name": "DAFO Administrator"},

            ),
            (
                {"email": "amalie@serviceudbyder.gl"},
                {"name": "DAFO Serviceudbyder"},
            )
        )
    )


def remove_password_users(apps, schema_editor):
    password_users = get_password_users(apps, schema_editor)
    remove_defaults(
        apps, schema_editor, "dafousers.PasswordUser", password_users
    )


class Migration(migrations.Migration):

    dependencies = [("dafousers", "0001_initial_tables")]
    # dependencies = [(b"dafousers", "0004_auto_20170404_1601")]

    operations = [
        migrations.RunPython(add_roles, remove_roles),
        migrations.RunPython(add_users, remove_users),
        migrations.RunPython(add_userprofiles, remove_userprofiles),
        migrations.RunPython(
            add_useridentifications, remove_useridentifications
        ),
        migrations.RunPython(add_password_users, remove_password_users),
    ]

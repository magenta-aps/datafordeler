# -*- coding: utf-8 -*-
from __future__ import unicode_literals

import sys

from django.conf import settings
from django.db import models

fixed = {}
schema = settings.DATABASES.get('default', {}).get('SQL_SERVER_SCHEMA')


def fix_sql_server_schemas():
    if not schema:
        return
    for x in settings.INSTALLED_APPS:
        module_str = x + ".models"
        if fixed.get(module_str):
            continue
        try:
            __import__(module_str)
        except ImportError:
            fixed[module_str] = True
            continue
        module = sys.modules[x + ".models"]
        fix_schemas_in_module(module)
        fixed[module_str] = True


def fix_schemas_in_module(module):
    if not schema:
        return
    for x in module.__dict__.values():
        if isinstance(x, type) and issubclass(x, models.Model):
            if not x._meta.db_table.startswith("%s]." % schema):
                x._meta.db_table = schema + "].[" + x._meta.db_table

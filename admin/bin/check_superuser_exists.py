# The contents of this file are subject to the Mozilla Public License
# Version 2.0 (the "License"); you may not use this file except in
# compliance with the License. You may obtain a copy of the License at
#    http://www.mozilla.org/MPL/
#
# Software distributed under the License is distributed on an "AS IS" basis,
# WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
# for the specific language governing rights and limitations under the
# License.
#
# Copyright 2015 Magenta Aps
#
import os
import django
django.setup()
from django.contrib.auth.models import User

if __name__ == '__main__':
    try:
        users = User.objects.filter(is_superuser=True)
        if len(users) > 0:
            print "Superusers already exists: %s" % (
                ", ".join([str(u) for u in users])
            )
            exit(0)
        else:
            raise Exception("No superusers found")
    except Exception as e:
        print e
        exit(1)

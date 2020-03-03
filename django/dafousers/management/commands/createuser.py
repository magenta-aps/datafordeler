from django.core.management import base
from django.core.exceptions import ValidationError
from django.core import validators
import getpass

from dafousers.models import PasswordUser, UserProfile


class Command(base.BaseCommand):

    # Must be the name of the admin profile created in initial migration
    userprofile_name = "DAFO Administrator"

    @staticmethod
    def input(prompt=None):
        import sys
        if sys.version_info[0] < 3:
            return raw_input(prompt)
        else:
            return input(prompt)

    def add_arguments(self, parser):
        parser.add_argument(
            '--username',
            help=u"Username of the created user"
        )
        parser.add_argument(
            '--firstname',
            help=u"Firstname of the created user"
        )
        parser.add_argument(
            '--lastname',
            help=u"Lastname of the created user"
        )
        parser.add_argument(
            '--email',
            help=u"Email address of the created user"
        )

    @staticmethod
    def username_valid(username):
        try:
            validators.validate_email(username)
            return True
        except ValidationError:
            return False

    @staticmethod
    def username_exists(username):
        return PasswordUser.objects.filter(email=username).count() > 0

    @staticmethod
    def validate_username(username):
        if not Command.username_valid(username):
            print "Invalid username"
            return False
        elif Command.username_exists(username):
            print "Username already exists"
            return False
        return True

    def handle(
            self, username=None,
            firstname=None, lastname=None,
            **kwargs
    ):

        # Make sure to get a userprofile object to attach to the user
        try:
            userprofile = UserProfile.objects.get(name=self.userprofile_name)
        except UserProfile.DoesNotExist:
            print "Couldn't find userprofile '%s' to assign" % \
                  self.userprofile_name
            return

            # Validate username from parameter
        if username is not None:
            username = username.strip()
            if not self.validate_username(username):
                username = None

        # Ask for username until a valid one is entered
        while username is None or len(username) == 0:
            username = self.input("Username (email address): ").strip()
            if not self.validate_username(username):
                username = None

        password = None
        repeat_password = None

        # Ask for password and repeat password until both are set and match
        while password is None:
            while password is None or len(password) == 0:
                password = getpass.getpass("Password: ")

            while repeat_password is None or len(repeat_password) == 0:
                repeat_password = getpass.getpass("Repeat password: ")

            if repeat_password != password:
                print "Password mismatch"
                password = None
                repeat_password = None

        # Ask for optional extra data
        if firstname is None:
            firstname = self.input("Firstname (optional):")

        if lastname is None:
            lastname = self.input("Lastname (optional):")

        # Create the user object
        user = PasswordUser()
        user.email = username
        user.givenname = firstname
        user.lastname = lastname
        (salt, encrypted_password) = user.generate_encrypted_password_and_salt(
            password
        )
        user.password_salt = salt
        user.encrypted_password = encrypted_password
        user.changed_by = "Console command 'createuser'"
        user.save()
        print "User '%s' created" % username

        user.user_profiles.add(userprofile)
        user.save()
        print "UserProfile '%s' added to User '%s'" % \
              (userprofile.name, username)

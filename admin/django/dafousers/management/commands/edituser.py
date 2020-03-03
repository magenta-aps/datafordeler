from django.core.management import base
import getpass

from dafousers.models import PasswordUser


class Command(base.BaseCommand):

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
            help=u"Username of the edited user"
        )

    @staticmethod
    def get_user(username):
        try:
            return PasswordUser.objects.get(email=username)
        except PasswordUser.DoesNotExist:
            print("User with name '%s' does not exist" % username)
            return None

    def handle(
            self, username=None,
            firstname=None, lastname=None,
            **kwargs
    ):
        user = None

        # Validate username from parameter
        if username is not None and len(username) > 0:
            username = username.strip()
            user = self.get_user(username)

        # Ask for username until a valid one is entered
        while user is None:
            username = self.input("Username (email address): ").strip()
            user = self.get_user(username)

        password = None
        repeat_password = None

        # Ask for password and repeat password until both are set and match
        while password is None:
            while password is None or len(password) == 0:
                password = getpass.getpass("Password: ")

            while repeat_password is None or len(repeat_password) == 0:
                repeat_password = getpass.getpass("Repeat password: ")

            if repeat_password != password:
                print("Password mismatch")
                password = None
                repeat_password = None

        # Update the user object
        (salt, encrypted_password) = user.generate_encrypted_password_and_salt(
            password
        )
        user.password_salt = salt
        user.encrypted_password = encrypted_password
        user.changed_by = "Console command 'edituser'"
        user.save()
        print("User '%s' changed password" % username)

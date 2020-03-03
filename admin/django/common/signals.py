# -*- coding: utf-8 -*-
import logging

from django.contrib.auth.signals import user_logged_in, user_logged_out
from django.dispatch import receiver


@receiver(user_logged_in)
def on_login(sender, signal, request, user, **kwargs):
    logging.getLogger('django.server').info(
        "User %s logged in", user
    )


@receiver(user_logged_out)
def on_logout(sender, signal, request, user, **kwargs):
    logging.getLogger('django.server').info(
        "User %s logged out", user
    )

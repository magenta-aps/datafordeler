from __future__ import unicode_literals

from django.apps import AppConfig


class DafousersConfig(AppConfig):
    name = 'dafousers'

    signals_loaded = False

    def ready(self):
        if not self.signals_loaded:
            import dafousers.signals  # noqa
            self.signals_loaded = True

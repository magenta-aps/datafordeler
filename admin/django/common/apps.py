from __future__ import unicode_literals

from django.apps import AppConfig


class CommonConfig(AppConfig):
    name = 'common'

    signals_loaded = False

    def ready(self):
        if not self.signals_loaded:
            import common.signals  # noqa
            self.signals_loaded = True

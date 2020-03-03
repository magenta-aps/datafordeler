# from django import forms
import dafousers.models
from django.contrib import admin
from django.db import models as django_models

EXCLUDE_MODELS = [
    dafousers.models.AccessAccount,
]
CUSTOM_ADMIN_CLASSES = {
    # Empty for now
}

ALWAYS_READONLY_FIELDS = set([
    # 'changed_by',
    'updated'
])


class DefaultModelAdmin(admin.ModelAdmin):

    def get_readonly_fields(self, request, obj=None):

        readonly_list = [
            x.name for x in self.model._meta.get_fields()
            if x.name in ALWAYS_READONLY_FIELDS
        ]

        if hasattr(self.model, "get_readonly_fields"):
            readonly_list = readonly_list + self.model.get_readonly_fields()

        if obj is None:
            pass
        else:
            pass

        return readonly_list

    def get_model_perms(self, *args, **kwargs):
        perms = super(DefaultModelAdmin, self).get_model_perms(*args, **kwargs)

        try:
            request = args[0]
        except ValueError:
            request = None

        if(request and request.user.username == "dafoadmin"):
            if (hasattr(self.model, "hide_in_dafoadmin") and
                    getattr(self.model, "hide_in_dafoadmin")):
                return {}

        return perms


def register_models(models, namespace=None):
    models = models.__dict__.items()

    for name, value in models:
        # Skip stuff that is not classes
        if not isinstance(value, type):
            continue

        # Skip stuff that is not models
        if not issubclass(value, django_models.Model):
            continue

        if value._meta.abstract:
            continue

        # Skip stuff that is not native to the booking.models module
        if namespace is not None and not value.__module__ == namespace:
            continue

        if value in EXCLUDE_MODELS:
            continue

        cls = CUSTOM_ADMIN_CLASSES.get(value, None)

        kwargs = {
            # "formfield_overrides": {
            #     django_models.ManyToManyField: {
            #         'widget': forms.CheckboxSelectMultiple
            #     },
            # }
        }

        if hasattr(value, "admin_register_kwargs"):
            kwargs.update(value.admin_register_kwargs())

        if cls is not None:
            admin.site.register(value, cls, **kwargs)
        else:
            admin.site.register(value, DefaultModelAdmin, **kwargs)


register_models(dafousers.models, 'dafousers.models')

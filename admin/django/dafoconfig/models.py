# -*- coding: utf-8 -*-

# This is an auto-generated Django model module.
# You'll have to do the following manually to clean this up:
#   * Rearrange models' order
#   * Make sure each model has one field with primary_key=True
#   * Make sure each ForeignKey has `on_delete` set to the desired behavior.
#   * Remove `managed = False` lines if you wish to allow Django to create, modify, and delete the table
# Feel free to rename the models, but don't rename db_table values or field names.
from __future__ import unicode_literals

import json

import fancy_cronfield.fields
from django.db import models
from django.db.models import options
from django.urls import reverse
from django.utils.translation import ugettext_lazy as _

if 'database' not in options.DEFAULT_NAMES:
    options.DEFAULT_NAMES = options.DEFAULT_NAMES + ('database',)

DEFAULT_CRON_SCHEDULE = '0 0 1 1 *'


class CronField(fancy_cronfield.fields.CronField):
    def __init__(self, *args, **kwargs):
        kwargs.setdefault(
            'default', DEFAULT_CRON_SCHEDULE,
        )
        super(CronField, self).__init__(*args, **kwargs)

    def formfield(self, **kwargs):
        kwargs.setdefault(
            'widget',
            fancy_cronfield.widgets.CronWidget(
                options=dict(
                    allow_multiple_all=False,
                )
            )
        )
        return super(CronField, self).formfield(**kwargs)

    def value_from_object(self, obj):
        """convert from quartz to "fancy" format, and ensure a value

        The JavaScript doesn't handle missing values...
        """

        s = super(CronField, self).value_from_object(obj)
        if not s:
            return DEFAULT_CRON_SCHEDULE

        # quartz uses '?', but fancycron does not
        parts = s.replace('?', '*').split(' ')

        # drop seconds...
        if len(parts) == 6:
            parts.pop(0)

        return ' '.join(parts)


class DafoConfig(object):

    def get_field_dict(self):
        own_fields = set([f.name for f in self.__class__._meta.get_fields()])
        return {
            field.name: getattr(self, field.name, None)
            for field in self.__class__._meta.get_fields()
            if field.name in own_fields
        }


class CprConfig(DafoConfig, models.Model):

    charset_choices = [
        (0, "US-ASCII"),
        (1, "ISO-8859-1"),
        (2, "UTF-8"),
        (3, "UTF-16BE"),
        (4, "UTF-16LE"),
        (5, "UTF-16")
    ]

    type_choices = [
        (0, u"Deaktiveret"),
        (1, u"Lokal fil"),
        (2, u"FTP-server")
    ]

    id = models.CharField(primary_key=True, max_length=255)
    personregistertype = models.IntegerField(blank=True, null=True, verbose_name=u"Kildetype", choices=type_choices)
    personregisterdatacharset = models.IntegerField(blank=True, null=True, verbose_name=u"Forventet inputdata-tegnkodning", choices=charset_choices)
    personregisterftpaddress = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"FTP adresse")
    personregisterftpdownloadfolder = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"FTP download-mappe")
    personregisterftpuploadfolder = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"FTP upload-mappe")
    personregisterftpusername = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"FTP brugernavn")
    personregisterftppassword = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"FTP password")
    personregisterlocalfile = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"Lokal fil")
    personregisterpullcronschedule = CronField(
        max_length=255, null=True,
        default=DEFAULT_CRON_SCHEDULE,
        verbose_name=u"CRON-tidsangivelse for automatisk hentning")
    roadregistertype = models.IntegerField(blank=True, null=True, verbose_name=u"Kildetype", choices=type_choices)
    roadregisterdatacharset = models.IntegerField(blank=True, null=True, verbose_name=u"Forventet inputdata-tegnkodning", choices=charset_choices)
    roadregisterftpaddress = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"FTP adresse")
    roadregisterftpdownloadfolder = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"FTP download-mappe")
    roadregisterftpuploadfolder = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"FTP upload-mappe")
    roadregisterftpusername = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"FTP brugernavn")
    roadregisterftppassword = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"FTP password")
    roadregisterlocalfile = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"Lokal fil")
    roadregisterpullcronschedule = CronField(
        max_length=255, null=True,
        default=DEFAULT_CRON_SCHEDULE,
        verbose_name=u"CRON-tidsangivelse for automatisk hentning")

    class Meta:
        managed = False
        database = 'configuration'
        db_table = 'cpr_config'

        verbose_name = 'Konfiguration af CPR-register'
        verbose_name_plural = 'Konfigurationer af CPR-registre'


class CvrConfig(DafoConfig, models.Model):

    type_choices = [
        (0, u"Deaktiveret"),
        (2, u"HTTP-server")
    ]

    id = models.CharField(primary_key=True, max_length=255)
    pullcronschedule = CronField(
        max_length=255, null=True,
        verbose_name=u"Cron-udtryk for automatisk synkronisering"
    )

    companyregistertype = models.IntegerField(blank=True, null=True, verbose_name=u"Kildetype", choices=type_choices)
    companyregisterstartaddress = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"Scan/scroll startadresse")
    companyregisterscrolladdress = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"Scan/scroll dataadresse")
    companyregisterusername = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"Brugernavn")
    companyregisterpassword = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"Password")
    companyregisterquery = models.CharField(max_length=1024, blank=True, null=True, verbose_name=u"Forespørgsel")
    companyregisterdirectlookupcertificate = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"Certifikat til direkte lookup")
    companyregisterdirectlookuppassword = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"Password til certifikat")
    companyregisterdirectlookupaddress = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"Adresse til direkte lookup")

    companyunitregistertype = models.IntegerField(blank=True, null=True, verbose_name=u"Kildetype", choices=type_choices)
    companyunitregisterstartaddress = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"Scan/scroll startadresse")
    companyunitregisterscrolladdress = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"Scan/scroll dataadresse")
    companyunitregisterusername = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"Brugernavn")
    companyunitregisterpassword = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"Password")
    companyunitregisterquery = models.CharField(max_length=1024, blank=True, null=True, verbose_name=u"Forespørgsel")

    participantregistertype = models.IntegerField(blank=True, null=True, verbose_name=u"Kildetype", choices=type_choices)
    participantregisterstartaddress = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"Scan/scroll startadresse")
    participantregisterscrolladdress = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"Scan/scroll dataadresse")
    participantregisterusername = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"Brugernavn")
    participantregisterpassword = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"Password")
    participantregisterquery = models.CharField(max_length=1024, blank=True, null=True, verbose_name=u"Forespørgsel")
    participantregisterdirectlookupcertificate = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"Certifikat til direkte lookup")
    participantregisterdirectlookuppassword = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"Password til certifikat")
    participantregisterdirectlookupaddress = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"Adresse til direkte lookup")


    class Meta:
        managed = False
        database = 'configuration'
        db_table = 'cvr_config'

        verbose_name = 'Konfiguration af CVR-register'
        verbose_name_plural = 'Konfigurationer af CVR-registre'


class GeoConfig(DafoConfig, models.Model):

    type_choices = [
        (0, u"Deaktiveret"),
        (1, u"Lokal fil"),
        (2, u"HTTP-server")
    ]

    charset_choices = [
        (0, u"UTF-8"),
        (1, u"ISO_8859_1")
    ]

    id = models.CharField(primary_key=True, max_length=255)
    pullcronschedule = CronField(
        max_length=255, null=True,
        verbose_name=u"Cron-udtryk for automatisk synkronisering"
    )

    tokenService = models.CharField(max_length=1024, blank=True, null=True, verbose_name=u"Tokenservice")
    username = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"Brugernavn")
    password = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"Password")

    charsetName = models.IntegerField(blank=True, null=True, verbose_name=u"Karaktersæt", choices=charset_choices)

    municipalityregistertype = models.IntegerField(blank=True, null=True, verbose_name=u"Kildetype", choices=type_choices)
    municipalityregisterurl = models.CharField(max_length=1024, blank=True, null=True, verbose_name=u"Kildeadresse")

    postcoderegistertype = models.IntegerField(blank=True, null=True, verbose_name=u"Kildetype", choices=type_choices)
    postcoderegisterurl = models.CharField(max_length=1024, blank=True, null=True, verbose_name=u"Kildeadresse")

    localityregistertype = models.IntegerField(blank=True, null=True, verbose_name=u"Kildetype", choices=type_choices)
    localityregisterurl = models.CharField(max_length=1024, blank=True, null=True, verbose_name=u"Kildeadresse")

    roadregistertype = models.IntegerField(blank=True, null=True, verbose_name=u"Kildetype", choices=type_choices)
    roadregisterurl = models.CharField(max_length=1024, blank=True, null=True, verbose_name=u"Kildeadresse")

    buildingregistertype = models.IntegerField(blank=True, null=True, verbose_name=u"Kildetype", choices=type_choices)
    buildingregisterurl = models.CharField(max_length=1024, blank=True, null=True, verbose_name=u"Kildeadresse")

    accessaddressregistertype = models.IntegerField(blank=True, null=True, verbose_name=u"Kildetype", choices=type_choices)
    accessaddressregisterurl = models.CharField(max_length=1024, blank=True, null=True, verbose_name=u"Kildeadresse")

    unitaddressregistertype = models.IntegerField(blank=True, null=True, verbose_name=u"Kildetype", choices=type_choices)
    unitaddressregisterurl = models.CharField(max_length=1024, blank=True, null=True, verbose_name=u"Kildeadresse")

    class Meta:
        managed = False
        database = 'configuration'
        db_table = 'geo_config'

        verbose_name = 'Konfiguration af GEO-register'
        verbose_name_plural = 'Konfigurationer af GEO-registre'


class GladdregConfig(DafoConfig, models.Model):
    id = models.CharField(primary_key=True, max_length=255)
    pullcronschedule = CronField(null=True, verbose_name=u"Cron-udtryk for automatisk synkronisering")
    registeraddress = models.CharField(max_length=255, blank=True, null=True, verbose_name=u"Registeradresse")

    class Meta:
        managed = False
        database = 'configuration'
        db_table = 'gladdreg_config'

        verbose_name = 'Konfiguration af Adresseopslagsregister'
        verbose_name_plural = 'Konfigurationer af Adresseopslagsregistre'


class DumpConfig(DafoConfig, models.Model):
    class Meta:
        managed = False
        database = 'configuration'
        db_table = 'dump_config'

        verbose_name = 'Dataudtræksopsætning'
        verbose_name_plural = 'Dataudtræksopsætninger'

        ordering = (
            'name',
        )
        unique_together = (
            ('requestPath', 'format', 'charset', 'destinationURI'),
        )

    charset_choices = [
        ("us-ascii", "US-ASCII",),
        ("iso-8859-1", "ISO-8859-1",),
        ("utf-8", "UTF-8",),
        ("utf-16", "UTF-16",),
    ]

    format_choices = [
        ("application/json", "JSON"),
        ("application/xml", "XML"),
        ("text/csv", "CSV"),
        ("text/tsv", "TSV"),
    ]

    id = models.BigAutoField(primary_key=True)

    name = models.CharField(
        unique=True, max_length=20,
        verbose_name=_('Navn'),
    )

    notes = models.TextField(
        max_length=1000, blank=True,
        verbose_name=_('Noter'),
    )

    format = models.CharField(
        max_length=20,
        choices=format_choices,
        default=format_choices[0],
        verbose_name=_("Format"),
    )

    charset = models.CharField(
        max_length=20,
        choices=charset_choices,
        default='utf-8',
        verbose_name=_("Kodning"),
    )

    destinationURI = models.TextField(
        db_column='destination_uri',
        max_length=255, null=True, blank=True,
        verbose_name=_("Destination"),
    )

    requestPath = models.TextField(
        db_column='request_path',
        max_length=255,
    )

    schedule = CronField(
        max_length=100,
        default='0 2 * * *',
    )

    def get_absolute_url(self):
        return reverse('dafoconfig:dump-edit', kwargs={'pk': self.pk})

    def __str__(self):
        return self.name


class Command(models.Model):

    STATUS_QUEUED = 0
    STATUS_PROCESSING = 1
    STATUS_SUCCESS = 2
    STATUS_FAILED = 3
    STATUS_CANCEL = 4
    STATUS_CANCELLED = 5

    status_choices = [
        (STATUS_QUEUED, u"I kø"),
        (STATUS_PROCESSING, u"Kører"),
        (STATUS_SUCCESS, u"Færdig"),
        (STATUS_FAILED, u"Fejlet"),
        (STATUS_CANCEL, u"Afbryder"),
        (STATUS_CANCELLED, u"Afbrudt")
    ]

    id = models.BigAutoField(primary_key=True)
    commandbody = models.CharField(max_length=255, blank=True, null=True)
    commandname = models.CharField(max_length=255)
    errormessage = models.CharField(max_length=2048, blank=True, null=True)
    handled = models.DateTimeField(blank=True, null=True)
    issuer = models.CharField(max_length=255)
    received = models.DateTimeField(blank=True, null=True)
    status = models.IntegerField(blank=True, null=True, choices=status_choices)

    @property
    def commandbody_json(self):
        return json.loads(self.commandbody)

    class Meta:
        managed = False
        database = 'configuration'
        db_table = 'command'

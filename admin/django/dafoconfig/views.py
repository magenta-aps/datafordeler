# -*- coding: utf-8 -*-

import json
import logging
import re
from datetime import datetime

from common.views import LoginRequiredMixin
from django.forms import model_to_dict
from django.shortcuts import redirect
from django.urls import reverse, reverse_lazy
from django.views.generic import TemplateView
from django.views.generic.edit import CreateView
from django.views.generic.edit import UpdateView, DeleteView
from django.views.generic.list import ListView

from .forms import CvrConfigurationForm, CprConfigurationForm
from .forms import DumpConfigurationForm
from .forms import GladdrregConfigurationForm, GeoConfigurationForm
from .models import CvrConfig, CprConfig, GeoConfig, GladdregConfig, Command
from .models import DumpConfig

logger = logging.getLogger('django.server')


class PluginConfigurationView(LoginRequiredMixin, UpdateView):

    plugin_name = None
    sectioned = False

    def get_object(self, queryset=None):
        return self.model.objects.first()

    def get_success_url(self):
        return reverse('dafoconfig:plugin-list')

    def get_context_data(self, **kwargs):
        context = {
            'plugin_name': self.plugin_name,
            'sections': self.get_sections()
        }

        context.update(kwargs)
        return super(PluginConfigurationView, self).get_context_data(**context)

    def get_sections(self):
        if self.sectioned:
            other = 'other'
            form = self.get_form()
            regex = re.compile('([a-zA-Z]+register)')
            sections = {}
            for (name, field) in form.fields.items():
                m = regex.match(name)
                key = other
                if m:
                    key = m.group(1)
                if not key in sections:
                    obj = {'items': []}
                    if key != other:
                        obj['name'] = key
                    sections[key] = obj
                sections[key]['items'].append(form[name])
            return ([sections[other]] if other in sections else []) + \
                   [values for key, values in sections.items() if key != other]
        else:
            return [{'items': self.get_form()}]

    def form_valid(self, form):
        response = super(PluginConfigurationView, self).form_valid(form)
        logging.getLogger('django.server').info(
            '\n'.join([
                "%s was updated by %s",
                "Contents:",
                "%s"
            ]),
            self.object.__class__.__name__,
            self.request.user.username,
            '\n'.join([
                "    %s: %s" %
                (field, value if 'password' not in field else '******')
                for field, value in self.object.get_field_dict().items()
            ])
        )
        return response


class CvrPluginConfigurationView(PluginConfigurationView):

    model = CvrConfig
    form_class = CvrConfigurationForm
    template_name = 'form.html'
    plugin_name = 'CVR'
    sectioned = True


class CprPluginConfigurationView(PluginConfigurationView):

    model = CprConfig
    form_class = CprConfigurationForm
    template_name = 'form.html'
    plugin_name = 'CPR'
    sectioned = True


class GeoPluginConfigurationView(PluginConfigurationView):

    model = GeoConfig
    form_class = GeoConfigurationForm
    template_name = 'form.html'
    plugin_name = 'GEO'
    sectioned = True



class GladdregPluginConfigurationView(PluginConfigurationView):

    model = GladdregConfig
    form_class = GladdrregConfigurationForm
    template_name = 'form.html'
    plugin_name = 'Gladdrreg'


class PluginListTable(LoginRequiredMixin, TemplateView):

    template_name = 'table.html'

    def get_context_data(self, **kwargs):

        plugin_list = [
            {
                'name': 'CVR',
                'configlink': reverse('dafoconfig:plugin-cvr-edit'),
                'synclink': reverse('dafoconfig:plugin-pull', args=['cvr'])
            },
            {
                'name': 'CPR',
                'configlink': reverse('dafoconfig:plugin-cpr-edit'),
                'synclink': reverse('dafoconfig:plugin-pull', args=['cpr'])
            },
            {
                'name': 'GEO',
                'configlink': reverse('dafoconfig:plugin-geo-edit'),
                'synclink': reverse('dafoconfig:plugin-pull', args=['geo'])
            },
            {
                'name': 'Gladdrreg',
                'configlink': reverse('dafoconfig:plugin-gladdrreg-edit'),
                'synclink': reverse(
                    'dafoconfig:plugin-pull', args=['gladdrreg']
                )
            },
        ]

        order = self.request.GET.get('order', 'name')
        if order:
            do_reverse = False
            if order[0] == '-':
                order = order[1:]
                do_reverse = True
            if order == "name":
                plugin_list = sorted(
                    plugin_list, key=lambda item: item['name'],
                    reverse=do_reverse
                )

        context = {
            'list': plugin_list,
            'order': order
        }
        context.update(kwargs)
        return super(PluginListTable, self).get_context_data(**context)


class PluginListView(PluginListTable):

    template_name = 'list.html'


class PluginPullView(LoginRequiredMixin, TemplateView):

    template_name = 'pull.html'

    status_conversion = {
        Command.STATUS_CANCELLED: u'Afbrudt',
        Command.STATUS_PROCESSING: u'Kørende',
        Command.STATUS_QUEUED: u'Lagt i kø',
        Command.STATUS_FAILED: u'Fejlet',
        Command.STATUS_CANCEL: u'Afbryder',
        Command.STATUS_SUCCESS: u'Færdig'
    }

    available_command_names = [
        'pull'
    ]

    def get_context_data(self, **kwargs):
        context = self.get_status()
        context.update(**kwargs)
        return context

    def get_plugin(self):
        return self.kwargs['plugin']

    def get_status(self):
        status = {}

        running_pulls = self.get_pull_summary('all', 'running')
        status['running'] = [
            {
                'plugin': command.commandbody_json['plugin'],
                'id': command.id,
                'received': command.received
            }
            for command in running_pulls
        ]

        latest_pulls = self.get_pull_summary(self.get_plugin(), 'latest')
        command = latest_pulls[0] if len(latest_pulls) > 0 else None
        if command is not None:
            status['latest'] = {
                'plugin': command.commandbody_json['plugin'],
                'id': command.id,
                'received': command.received,
                'handled': command.handled,
                'status': self.status_conversion.get(
                    command.status
                ),
                'errorMessage': command.errormessage
            }

        return status

    def post(self, request, *args, **kwargs):
        if request.POST.get('sync_start') is not None:
            self.create_command('pull', {'plugin': self.get_plugin()})

        elif request.POST.get('sync_stop') is not None:
            id = request.POST.get('sync_stop')
            self.cancel_command(id)

        return redirect(
            reverse('dafoconfig:plugin-pull', args=[self.get_plugin()])
        )

    def create_command(self, command_name, command_data):
        if command_name in self.available_command_names:
            command = Command(
                commandname=command_name.lower(),
                commandbody=json.dumps(command_data),
                issuer="admin interface",
                received=datetime.now(),
                status=Command.STATUS_QUEUED
            )
            command.save()

    def cancel_command(self, command_id):
        try:
            command = Command.objects.get(id=command_id)
            command.status = Command.STATUS_CANCEL
            command.save()
        except Command.DoesNotExist:
            pass

    def get_pull_summary(self, plugin_name, state):
        commands = []
        valid_states = ['latest', 'running']
        if state not in valid_states:
            return []
        qs = Command.objects.filter(commandname='pull')
        if state == 'running':
            qs = qs.filter(
                status__in=[Command.STATUS_QUEUED, Command.STATUS_PROCESSING]
            )
        plugins = [plugin_name] \
            if plugin_name != 'all' \
            else ['cpr', 'cvr', 'gladdrreg']
        for plugin in plugins:
            pqs = qs.filter(
                commandbody__icontains="\"plugin\": \"%s\"" % plugin
            ).order_by('-handled')
            command = pqs.first()
            if command is not None:
                commands.append(command)
        return commands


class DumpCreate(LoginRequiredMixin, CreateView):
    template_name = 'dump/add.html'
    form_class = DumpConfigurationForm
    model = DumpConfig

    def get_success_url(self):
        action = self.request.POST.get('action')
        if action == '_save':
            return reverse('dafoconfig:dump-list')
        elif action == '_addanother':
            return reverse('dafoconfig:dump-add')

    def form_valid(self, form):
        response = super(DumpCreate, self).form_valid(form)
        logging.getLogger('django.server').info(
            '\n'.join([
                "%s (id=%d) was created by %s",
                'Contents:',
                "%s"
            ]),
            self.object.__class__.__name__,
            self.object.id,
            self.request.user.username,
            '\n'.join([
                "    %s: %s" %
                (field, value)
                for field, value in self.object.get_field_dict().items()
            ])
        )
        return response

class DumpEdit(LoginRequiredMixin, UpdateView):
    model = DumpConfig
    form_class = DumpConfigurationForm
    template_name = 'dump/edit.html'

    def form_valid(self, form):
        response = super(DumpEdit, self).form_valid(form)
        logging.getLogger('django.server').info(
            '\n'.join([
                "%s (id=%d) was updated by %s",
                'Contents:',
                "%s"
            ]),
            self.object.__class__.__name__,
            self.object.id,
            self.request.user.username,
            '\n'.join([
                "    %s: %s" %
                (field, value)
                for field, value in self.object.get_field_dict().items()
            ])
        )
        return response


class DumpList(LoginRequiredMixin, ListView):
    template_name = 'dump/list.html'
    model = DumpConfig


class DumpDelete(LoginRequiredMixin, DeleteView):
    template_name = 'dump/delete.html'
    model = DumpConfig

    success_url = reverse_lazy('dafoconfig:dump-list')

    def get_context_data(self, **kwargs):
        context = super(DumpDelete, self).get_context_data(**kwargs)
        context.update(fields=model_to_dict(self.object))
        return context

    def delete(self, request, *args, **kwargs):
        id = self.get_object().id
        response = super(DumpDelete, self).delete(request, *args, **kwargs)
        logging.getLogger('django.server').info(
            "%s (id=%d) was deleted by %s",
            self.model.__name__,
            id,
            self.request.user.username
        )
        return response

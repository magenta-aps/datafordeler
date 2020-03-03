import django.contrib.auth.views
from django.conf import settings
from django.conf.urls import url
from django.conf.urls.static import static
from django.views.decorators.csrf import csrf_exempt
from django.views.i18n import JavaScriptCatalog
import common.views as dafo_views

app_name = 'common'

urlpatterns = [

    # GLOBAL
    url(r'^frontpage/$',
        csrf_exempt(dafo_views.FrontpageView.as_view()),
        name="frontpage"),
    url(r'^$',
        dafo_views.IndexView.as_view(),
        name="index"),
    url(r'^login/',
        csrf_exempt(dafo_views.LoginView.as_view()),
        name='login'),
    url(r'^logout/',
        django.contrib.auth.views.LogoutView.as_view(),
        {'template_name': 'logged_out.html'},
        name='logout'),

    # jsi18n views
    url(r'^jsi18n/$', JavaScriptCatalog.as_view(), name='javascript-catalog'),

    # Restructured text documentation
    url(r'^doc/(?P<docfile>.*)[.]rst',
        dafo_views.RstDocView.as_view(),
        name="doc"),
] + static(settings.DOC_STATIC_URL, document_root=settings.DOC_STATIC_DIR)

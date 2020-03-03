import dafousers.views as dafo_views
from django.conf.urls import url

app_name = 'dafousers'
urlpatterns = [

    # USER
    url(r'^user/(?P<pk>[0-9]+)/$',
        dafo_views.PasswordUserEdit.as_view(),
        name='passworduser-edit'),
    url(r'^user/(?P<pk>[0-9]+)/history/$',
        dafo_views.PasswordUserHistory.as_view(),
        name='passworduser-history'),
    url(r'^user/add/$',
        dafo_views.PasswordUserCreate.as_view(),
        name='passworduser-add'),
    url(r'^user/list/$',
        dafo_views.PasswordUserList.as_view(),
        name='passworduser-list'),

    url(r'^ajax/update_passworduser_queryset/$',
        dafo_views.PasswordUserListTable.as_view(),
        name='update_passworduser_queryset'),
    url(r'^ajax/update_passworduser/$',
        dafo_views.PasswordUserAjaxUpdate.as_view(),
        name='update_passworduser'),

    # SYSTEM
    url(r'^system/(?P<pk>[0-9]+)/$',
        dafo_views.CertificateUserEdit.as_view(),
        name='certificateuser-edit'),
    url(r'^system/(?P<pk>[0-9]+)/history/$',
        dafo_views.CertificateUserHistory.as_view(),
        name='certificateuser-history'),
    url(r'^system/add/$',
        dafo_views.CertificateUserCreate.as_view(),
        name='certificateuser-add'),
    url(r'^system/list/$',
        dafo_views.CertificateUserList.as_view(),
        name='certificateuser-list'),

    url(r'^ajax/update_certificateuser_queryset/$',
        dafo_views.CertificateUserListTable.as_view(),
        name='update_certificateuser_queryset'),
    url(r'^ajax/update_certificateuser/$',
        dafo_views.CertificateUserAjaxUpdate.as_view(),
        name='update_certificateuser'),

    url(r'certificate/(?P<pk>[0-9]+)/download/$',
        dafo_views.CertificateDownload.as_view(),
        name='certificate_download'),

    # ORGANISATION
    url(r'^organisation/(?P<pk>[0-9]+)/$',
        dafo_views.IdentityProviderAccountEdit.as_view(),
        name='identityprovideraccount-edit'),
    url(r'^organisation/(?P<pk>[0-9]+)/history/$',
        dafo_views.IdentityProviderAccountHistory.as_view(),
        name='identityprovideraccount-history'),
    url(r'^organisation/add/$',
        dafo_views.IdentityProviderAccountCreate.as_view(),
        name='identityprovideraccount-add'),
    url(r'^organisation/list/$',
        dafo_views.IdentityProviderAccountList.as_view(),
        name='identityprovideraccount-list'),

    url(r'^ajax/update_identityprovideraccount_queryset/$',
        dafo_views.IdentityProviderAccountListTable.as_view(),
        name='update_identityprovideraccount_queryset'),
    url(r'^ajax/update_identityprovideraccount/$',
        dafo_views.IdentityProviderAccountAjaxUpdate.as_view(),
        name='update_identityprovideraccount'),

    # USER PROFILE
    url(r'^user-profile/(?P<pk>[0-9]+)/$',
        dafo_views.UserProfileEdit.as_view(),
        name='userprofile-edit'),
    url(r'^user-profile/(?P<pk>[0-9]+)/history/$',
        dafo_views.UserProfileHistory.as_view(),
        name='userprofile-history'),
    url(r'^user-profile/add/$',
        dafo_views.UserProfileCreate.as_view(),
        name='userprofile-add'),
    url(r'^user-profile/list/$',
        dafo_views.UserProfileList.as_view(),
        name='userprofile-list'),

    url(r'^ajax/update_userprofile_queryset/$',
        dafo_views.UserProfileListTable.as_view(),
        name='update_userprofile_queryset'),
    url(r'^ajax/update_userprofile/$',
        dafo_views.UserProfileAjaxUpdate.as_view(),
        name='update_userprofile'),
    # AJAX Calls
    url(r'^ajax/search_org_user_system/$',
        dafo_views.search_org_user_system,
        name='search_org_user_system'),
    url(r'^ajax/search_user_profile/$',
        dafo_views.search_user_profile,
        name='search_user_profile'),

    # MONITORING HANDLES
    url(r'^monitor/dafousers/database/?$',
        dafo_views.DatabaseCheckView.as_view(),
        name='monitoring_users_database'),

]
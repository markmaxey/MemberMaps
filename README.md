MemberMaps
==========

This project will create Google Maps and spreadsheets containing geodedic information &amp; statistics given a membership list for churches, community organizations, etc.


https://developers.google.com/maps-engine/documentation/start
* Distance Matrix API
* Geocoding API
https://console.developers.google.com/project
* Consent Screen
* Public API access
* OAuth
https://developers.google.com/maps-engine/documentation/oauth/serviceaccount#creating_a_service_account
https://develepers.google.com/accounts/docs/OAuth2ServiceAccount#creatinganaccount

Generate JSON Key
no need to add OAuth client id as a user (it is automatically done for you)

Quotas can be explored via project -> APIs -> [SERVICE] -> Quota tab

Go to

https://mapsengine.google.com/admin

and on the right hand side there is a gear icon with a pull down selection.  Choose "Manage Users".  From there, add the client_email.  This is the Google APIs service account found at

https://cloud.google.com/console

under the permissions tab.

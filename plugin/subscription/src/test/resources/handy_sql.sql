--Subscription


--Liste over magentas dataabonnementer
SELECT * FROM 
	dafotestdb007.[dbo].[subscription_subscriber] subscriber
	JOIN dafotestdb007.[dbo].[subscription_dataevent] subscription ON (
		subscriber.id=subscription.subscriber_id
	)
	WHERE subscriberId = 'PITU_GOV_DIA_magenta_services'

--Liste over magenta CPR-lister
SELECT * FROM 
	dafotestdb007.[dbo].[subscription_subscriber] subscriber
	JOIN dafotestdb007.[dbo].[subscription_cpr_list] cprList ON (
		subscriber.id=cprList.subscriber_id
	)
	WHERE subscriberId = 'PITU_GOV_DIA_magenta_services'


--Liste over magenta CPR-numre i abonnement under Magenta
SELECT * FROM 
	dafotestdb007.[dbo].[subscription_subscriber] subscriber
	JOIN dafotestdb007.[dbo].[subscription_cpr_list] cprList ON (
		subscriber.id=cprList.subscriber_id
	)
	JOIN dafotestdb007.[dbo].[subscription_cpr_number_subscribed] cprNo ON (
		cprList.id=cprNo.cprlistId
	)
	WHERE subscriberId = 'PITU_GOV_DIA_magenta_services'

CVR
============

This project fetches information from virk.dk. It reads information of companies and persists it in the database of
datafordeleren.

Data for this module is fetched daily. It feches all companies that is adressed with a munipialicity-code of a
munipialicity in greenland. Every time data has been fetched a timestamp is persisted in the database of datafordeler,
and when performing fetching next day, all companies that has been changed since last fetch is updated.




# PredictionAssistant-Data
Download and Persist PredictIt API data.

The easiest way to use this application is to go to: https://www.dropbox.com/s/akmhna96xzmup8p/Prediction%20Assistant%20Data.zip?dl=0

Download and extract to wherever you want. Make sure the Jar and application.properties file stay together, that's it! You will need a MySQL instance running, but we will cover that below.

This application uses Spring Boot and JavaFX to download market data from the PredictIt API (https://www.predictit.org/api/marketdata/all/) and save it to a MySQL database.

PredictIt's API only gives a current snapshot of all market data, we cannot easily collect historical data for any given market and so we cannot easily analyze it. This program is part of a collection of applications that seeks to make it simple and easy for anyone to collect and gain access to historical data for any PredictIt market.

This program persists the downloaded data to a MySQL database whose source is defined by the user in the "application.properties" file. For example:


spring.datasource.url=jdbc:mysql://localhost:3306/prediction_assistant_pisnapshots

spring.datasource.username=axlor_dev

spring.datasource.password=axlordevpass


The datasource.url contains the database location, port, and pre-generated schema name. No tables need be created before running. The application will generate them. 

The datasource.username must be a user that is authorized to create tables and add data to the database etc. This application only creates tables once (when it needs to), otherwise it will only be adding data to the database, it does not remove old or outdated data in any way. However, it is important to note that tables could be dropped by the application if the program is ran with an application.properties file containing the line:


spring.jpa.hibernate.ddl-auto=create


By default this value is set to 'update', which assumes a schema defined in the datasource.url exists. Most users should never need to mess with this and can keep the default setting that exists internally in the default config file.

Another configurable setting is the wait time between data download attempts. Some users may want every bit of data the PredictIt API puts out... PredictIt's API mentions they update once every minute, so they may wish to set wait time to 60 seconds. Others may only be interested in hourly data, attempting to get data once every hour, thirty minutes, or whatever. To configure the wait time between attempted data downloads, use:


my.waitTime=<timeInMilliseconds>


By default this value is set at 40000, meaning the application waits 40 seconds between data download attempts. Note that this application does not store redundant data to the database, so having a wait time as short as 10 or 15 seconds is acceptable. The application will download data but won't persist it if it duplicates another snapshot's hash already stored in the database.

When running the executable jar, make sure you have an application.properties file in the same directory with the required spring.datasource key-value pairs defined.

If you do not wish to use a MySQL database and want to use some other SQL based relational database you will need to modify the source code to use a different database driver, but that is outside the scope of this readme.

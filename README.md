# What's this?
Application to collect data on your reading habits. Which books you read. When you read. How much you read. 
So you could analyze this data for yourself.

# How it works?
TODO: technical description

* readings are sent to server app at https://parse.com/
* CloudCode functions  used to send e-mails when you switch away from book,etc

# Which E-Book Reading programs are supported?
* Only Mantano Reader currently supported (it's great app! Why you need anything else?)

# App only collects data? How to use them?
I plan to add client side graphs,reporting,etc
Server side part currently send e-mails with reading details when you done reading. 


# Building without API Keys
Code will build correctly and will even work but everything network will not work. And it's a lot of things
It's done in this way because I'm new to Gradle and I need this code to at least compile  without any extra non-public data

# How to configure correct API Keys, code signing,etc 
* if don't do this, your build WILL fail
* cd $HOME
* mkdir .androidSigning/
* touch versionCodesBookTrackerProperties
* edit versionCodesBookTrackerProperties to put VERSION_CODE=0 (or any other number)
* touch booktrackerAPIKeys
* edit booktrackerAPIKeys to add:
  * PARSE_APP_ID=your_parse_app_id (https://parse.com/ , Application used Parse as network backend
  * PARSE_CLIENT_KEY=your_parse_client_key
  * PARSE_MASTER_KEY=master_key_for your_parse_application (used for crash reporting)
  * HOCKEYAPP_APP_ID=your_HockeApp_app_id (used to check for updates if they are distributed via HockeyApp)
  * HOCKEYAPP_TOKEN=your_HockeyApp_token (for this app, used to automatically upload updated version here)
  you can change paths in gradle.properties

# Will sources for  Parse CloudCode part be available?
Maybe later
# What's this?
Application to collect data on your reading habits. Which books you read. When you read. How much you read. 
So you could analyze this data for yourself.

# How it works?
TODO: technical description

* readings are sent to server app at https://parse.com/
* CloudCode functions  used to send e-mails when you switch away from book,etc

# How your code here licensed?
* For now only GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
* I also reserve right to re-license code under any other license for any purpose I want. 
* Included ParseUI-Android library is (of course) licensed under it's own license. Same applies to Hockeyapp's binaries,etc


# Which E-Book Reading programs are supported?
* Only Mantano Reader currently supported (it's great app! Why you need anything else?)

# App only collects data? How to use them? 
I plan to add client side graphs,reporting,etc
Server side part currently send e-mails with reading details when you done reading. 

# Is there Privacy Policy?
If you build your own version, privacy policy is up to you of course.
If you use my build it's simple: I will only sell/transfer data if asked by relevant authority.
When this app will be on Play Store / Amazon App Store I will write something more formal.

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

# How Android Permissions are used and why
* RECEIVE_BOOT_COMPLETED  - to start at device boot
* INTERNET - to send readings report to server 
* ACCESS_NETWORK_STATE - for analytics & error reporting
* READ_PHONE_STATE - to get hardware details for statistical purposes
* GET_TASKS - to knew currently active app. This is needed to detected situation when user switches away from reading app
* WRITE_EXTERNAL_STORAGE - 
* BIND_ACCESSIBILITY_SERVICE - to perform core functionality. user WILL be asked to enable this anyway. Application couldn't do it's job without it. TODO: add note somewhere how exactly we use it (more details)

# Thanks to
* [Freepik from Flaticon]( http://www.flaticon.com/free-icon/marionette-puppet-silhouette_33882) for application icon
At time I got it license was:
License: Creative commons
You are free to use this icon for commercial purposes, to share or to modify it. In exchange, it's necessary to credit the author for the original creation.
* [Parse Team (currently at Facebook)](https://parse.com/about) for Parse Platform (and ParseUI-Android)
* [Mantano SAS](www.mantano.com/mantano-reading-platform/) for Mantano Premium. This is best E-Book reader I have so far, even accounting for occasional glitches with Cloud Service and crashes with badly formatted books. Current version of Reading Tracker only support Mantano as data source. 

# What's this?
Application to collect data on your reading habits. Which books you read. When you read. How much you read.
So you could analyze this data for yourself.

# How it works? (Non-technical)
In the bigging there was a phrase "The Amazon Kindle reports which pages of which books the user looks at, and when"...but I wanted this information too, for e-books I read using e-book readers I read.

# How it works? (Technical)
Android Accessibility APIs are used to 'read' parts of supported e-book reader's views.
Data are to [Parse](parse.com) cloud service for later processing and analysis.
After that e-mails are send to me using [Mandrill](mandrillapp.com) and record is added to my [Evernote](evernote.com) notebook using [Zapier](zapier.com).
In future various interesting graphs could be made once I collect enough statistics

# Requirements for using
* Android 4.3+ (last version of code with support for 4.2 is at https://github.com/intari/readingtracker/tree/Last_API_17_Android_4_2 , it's much easier to work at API Level 18)

# Which e-book readers are supported for integration
Currently Mantano Reader Premium is supported (it's great. Please buy it. If you want to used it on multiple devices you should buy their cloud subscription too).
Mantano Essentials and Mantano Lite should also work.
Others are not supported yet.

Please note authors of ebook readers don't endorse this app.

# How to use application (assuming you use Google Play version):
* Install one of supported ebook readers
* Install this app
* enable access to accessibility services for app
* open & read books from reader's Library view
* you could confirm everything was detecred correctly by opening app again and checking it saw details
* when you close book you will get e-mail from my bot intari@viorsan.com

# What about analytics, Evernote integration?
Analytics will be possible in future versions.
Ability to setup integration with Zapier web-hooks (support for Evernote was done this way) is not yet available in application's interface.



# Which Android Permissions are used and why
  Permissions
  * RECEIVE_BOOT_COMPLETED  - to be able to catch moments when you start read
  * INTERNET - communication with our server
  * ACCESS_NETWORK_STATE - usage analytics & error reporting.
  * READ_PHONE_STATE - statistics
  * GET_TASKS - to knew when you switch from supported reading app to something other
  * WRITE_EXTERNAL_STORAGE - automatic updates of non-PlayStore versions using HockeyApp
  * BIND_ACCESSIBILITY_SERVICE - main app functionality. you will be asked to provide access to 'Accessibility Services' on startup. You could refuse but you will not be able to get your statistics from this device
  * WAKE_LOCK / VIBRATE / GET_ACCOUNTS - Parse-based push notifications

# Licenses
My own code is licensed under GPLv3, exception is given to link all libraries I link to (Parse,etc). I reserve right to change this at any time
Other things:
* Parse Platform jars under their own EULA
* Flurry (to be removed) jars under their own EULA
* Countly Analytics Client SDKs are under MIT license http://resources.count.ly/v1.0/docs/licencing-faq
* Logo and Icon are based on images from [Flaticon](flaticon.com) by [Freepik](freepik.com) licensed under CC BY 3.0 so my logo and icon also under CC BY 3.0

# Is it in Google Play Store / other Android Markets?
* [Google Play Store](https://play.google.com/store/apps/details?id=com.viorsan.readingtracker)

# Privacy Policy (for PlayStore version)
Data collected by application will only be used to provide services to app user.
Collected data won't be sold, used for spam or other bad things.
If applicable goverment authority asks for your data I could provide data collected by app from you.
Application uses several 3rd-party services (Parse, Mandrill, Countly) to provide some functionality.

# Where are sources for CloudCode part?
Not in this repository yet

# Requirements for building
* Android SDK 21
* Configure correct API Keys, if don't do this, your build WILL fail
* cd $HOME
* mkdir .androidSigning/
* touch booktrackerAPIKeys
* edit booktrackerAPIKeys to add:
  * PARSE_APP_ID=your_parse_app_id (https://parse.com/ , Application used Parse as network backend
  * PARSE_CLIENT_KEY=your_parse_client_key
  * PARSE_MASTER_KEY=master_key_for your_parse_application (used for crash reporting)
  * HOCKEYAPP_APP_ID=your_HockeApp_app_id (used to check for updates if they are distributed via HockeyApp)
  * HOCKEYAPP_TOKEN=your_HockeyApp_token (for this app, used to automatically upload updated version here)
  * PARSE_APP_ID_FOR_TEST_HARNESS=not currently used but something must be put here
  * PARSE_CLIENT_KEY_FOR_TEST_HARNESS=not currently used but something must be put here
  * PARSE_USERNAME_FOR_TEST_HARNESS=not currently used but something must be put here
  * PARSE_PASSWORD_FOR_TEST_HARNESS=not currently used but something must be put here
  * COUNTLY_SERVER = count.ly analytics server URL
  * COUNTLY_APPKEY = count.ly analytics App Key
  * FLURRY_APIKEY = Flurry's API Key (currently disabled but something must be here)
  * VERSION_CODE_AUTH/VERSION_CODE_URL - see build.gradle, those are used to get monotonically increasing build numbers. It's likely you will want change those parts of build.gradle
    you can change paths in gradle.properties
* or look at build.gradle to determine how to use env vars

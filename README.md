# What's this?
Application to collect data on your reading habits. Which books you read. When you read. How much you read.
So you could analyze this data for yourself.

# How it works? (Non-technical)
In the beginning there was a phrase "The Amazon Kindle reports which pages of which books the user looks at, and when"...but I wanted this information too, for e-books I read using e-book readers I read.

# How it works? (Technical)
Android Accessibility APIs are used to 'read' parts of supported e-book reader's views.
Data are to [Parse](parse.com) cloud service for later processing and analysis.
After that e-mails are send to me using [Mandrill](mandrillapp.com) and record is added to my [Evernote](evernote.com) notebook using [Zapier](zapier.com).
In future various interesting graphs could be made once I collect enough statistics

# Plans for future
* Make it possible to view your stats data locally


# Application requirements
* Android 4.3+ (last version of code with support for 4.2 is at https://github.com/intari/readingtracker/tree/Last_API_17_Android_4_2 , it's much easier to work at API Level 18)
* Suppported e-book reader (Currently this mean some edition of Mantano Reader)
* Android tablet or phone

# Android 5.0.1 Lollipop support
* As of version 1.6.8 application should work correctly with Lollipop

# Android 6.0 Marshmallow support
* Current release version doesn't work with Android 6.0 at all. it just doesn't work. Alpha version with Android 6.0 support is available. (visit https://play.google.com/apps/testing/com.viorsan.readingtracker to opt-in)


# Which e-book readers are supported?
Currently all editions of Mantano Reader are supported (it's great. Please buy it. If you want to used it on multiple devices you should buy their cloud subscription too).
Others are not supported yet.
Please note authors of ebook readers don't endorse this app.

# YotaPhone2 support
* Currently YotapPhone2 supported as regular device (no support for Mantano's ability to use e-ink screen). This will be fixed if/when I get access to YotaPhone2. You are welcome to sponsor it -:)


# How to use application (assuming you use Google Play version):
* Install one of supported ebook readers
* Install this app
* enable access to accessibility services for app
* open & read books from reader's Library view
* you could confirm everything was detecred correctly by opening app again and checking if it  saw details
* when you close book you will get e-mail from my bot intari@viorsan.com.
Bot's message will be in Russian if bot thinks you are speak Russian or in English.

# What about analytics, Evernote integration?
Analytics will be possible in future versions.
Ability to setup integration with [Zapier](zapier.com) web-hooks (support for [Evernote](evernote.com) was done this way) is not yet available in application's interface


# Which Android Permissions are used and why
  Permissions for currently released version.
  * RECEIVE_BOOT_COMPLETED  - to be able to catch moments when you start read
  * INTERNET - communication with our server
  * ACCESS_NETWORK_STATE - usage analytics and error reporting.
  * READ_PHONE_STATE - statistics (Version for Android 6.0 doesn't need this)
  * GET_TASKS - to knew when you switch from supported reading app to something other (Version for Android 6.0 doesn't need this)
  * WRITE_EXTERNAL_STORAGE - automatic updates of non-PlayStore versions using HockeyApp
  * BIND_ACCESSIBILITY_SERVICE - main app functionality. you will be asked to provide access to 'Accessibility Services' on startup. You could refuse but you will not be able to get your statistics from this device
  * WAKE_LOCK / VIBRATE / GET_ACCOUNTS - Parse-based push notifications (Version for Android 6.0 doesn't need this)
  

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
I reserve right to  provide data collected by app from you if applicable goverment authority asks for your data.

Application uses several 3rd-party services (Parse, Mandrill, Countly) to provide some functionality.

# Where are sources for CloudCode part?
Not in this repository yet

# Requirements for building
see end of  [README_old] (https://github.com/intari/readingtracker/blob/master/README_old.md)

# Update 2023+:
- Mantano reader is ancient and no longer works on modern versions of android anyway.
- License modification: If you develop reading app for andorid (or android and other platforms) - you can use all my code from this repo for any purpose you want (even if you reader is closed-source), it would be nice if you send me link to your app.
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
* Make it possible to actually view your stats data using on-device interface and not only in e-mails


# Application requirements
* Android 4.3+ (last version of code with support for 4.2 is at https://github.com/intari/readingtracker/tree/Last_API_17_Android_4_2 , it's much easier to work at API Level 18)
* Suppported e-book reader (Currently this mean some edition of Mantano Reader)
* Android tablet or phone

# Android 5.0.1 Lollipop support
* As of version 1.6.8 application should work correctly with Lollipop

# Android 6.0 Marshmallow support
* Version 1.8.1 works with Android 6.0

* Android 7.0 support
* Not even tested yet

# Which e-book readers are supported?
Currently all editions of Mantano Reader are supported (it's great. Please buy it. If you want to used it on multiple devices you should buy their cloud subscription too).
Others are not supported yet.
Please note authors of ebook readers don't endorse this app.

# How to use application (assuming you use Google Play version):
* Install one of supported ebook readers
* Install this app
* enable access to accessibility services for app
* open & read books from reader's Library view
* you could confirm everything was detecred correctly by opening app again and checking if it  saw details
* when you close book you will get e-mail from my bot intari@viorsan.com.
Bot's message will be in Russian if bot thinks you are speak Russian or in English in other cases.

# What about analytics, Evernote integration, E-Mail support?
Analytics will be possible in future versions.
Ability to setup integration with [Zapier](zapier.com) web-hooks (support for [Evernote](evernote.com) was done this way) is not yet available in application's interface but available by requesting author.

# Which Android Permissions are used and why
  Permissions for currently released version.
  * RECEIVE_BOOT_COMPLETED  - to be able to catch moments when you start read
  * INTERNET - communication with our server
  * ACCESS_NETWORK_STATE - usage analytics and error reporting.
  * WRITE_EXTERNAL_STORAGE - automatic updates of non-PlayStore versions using HockeyApp
  * BIND_ACCESSIBILITY_SERVICE - main app functionality. you will be asked to provide access to 'Accessibility Services' on startup. You could refuse but you will not be able to get your statistics from this device
  

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

Application uses several 3rd-party services (Parse, Mailgun, Mixpanel,etc) to provide some functionality. 
Main database is now hosted in EU

# Where are sources for Reading Tracker Server?
* Not in this repository. Basic logic works without it. 

# Do I have to create account/login?
* No, you don't have to. Notification with data will be shown anyway if that's you need.

# Requirements for building
see end of  [README_old] (https://github.com/intari/readingtracker/blob/master/README_old.md)

# Badges
[![GetBadges Game](https://intari-readingtracker.getbadges.io/shield/company/intari-readingtracker/user/2151)](https://intari-readingtracker.getbadges.io/?ref=shield-player)

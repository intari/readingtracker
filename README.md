# English
## What's this?
Application to collect data on your reading habits. Which books you read. When you read. How much you read. 
So you could analyze this data for yourself.


## How it works? (Non-technical)
* In the begging there was a phrase "The Amazon Kindle reports which pages of which books the user looks at, and when"...but I want this information too, for e-books I read
* TODO:continue

## How it works? (Technical)
using Android Accessibility APIs

TODO: technical description

* readings are sent to server app at https://parse.com/
* CloudCode functions  used to send e-mails when you switch away from book,etc


## Is code ever works?
* According to Travis CI current build status is ![current build status according to Travis CI](https://travis-ci.org/intari/readingtracker.svg?branch=master)

## How it is licensed?
* For now only GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
* I also reserve right to re-license my code under any other license for any purpose I want. 
* Included ParseUI-Android library is (of course) licensed under it's own license. Same applies to Hockeyapp's binaries and other 3rd-party libraries

## Which E-Book Reading programs are supported?
* Only Mantano Reader currently supported (it's great app! Why you need anything else?)

## App only collects data? How to use them?
I plan to add client side graphs,reporting,etc at some point in future
Server side part currently send e-mails with reading details when you done reading. 


## Is there Privacy Policy?
If you build your own version, privacy policy is up to you of course.
If you use my build it's simple: I will only sell/transfer data if asked by relevant authority.
When this app will be on Play Store / Amazon App Store I will write something more formal.

## Is app in Google Play Store?
Not yet. But will be

## Which 3rd-party services used?
* [HockeyApp](hockeyapp.net) - updates for test builds
* [Parse](parse.com) - data store, server-side logic, analytics, crash handling
* [Mandrill](mandrillapp.com) - sending reading progress notifcations


## Building without API Keys
Code will not build correctly without them.

## How to configure correct API Keys, code signing,etc
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
  * PARSE_APP_ID_FOR_TEST_HARNESS=not currently used but something must be put here
  * PARSE_CLIENT_KEY_FOR_TEST_HARNESS=not currently used but something must be put here
  * PARSE_USERNAME_FOR_TEST_HARNESS=not currently used but something must be put here
  * PARSE_PASSWORD_FOR_TEST_HARNESS=not currently used but something must be put here
  you can change paths in gradle.properties

* or look at build.gradle to determine how to use env vars



## Will sources for  Parse CloudCode part be available?
Maybe later

## Which Android Permissions are used and why
* RECEIVE_BOOT_COMPLETED  - to start at device boot
* INTERNET - to send readings report to server 
* ACCESS_NETWORK_STATE - for analytics & error reporting
* READ_PHONE_STATE - to get hardware details for statistical purposes
* GET_TASKS - to knew currently active app. This is needed to detected situation when user switches away from reading app
* WRITE_EXTERNAL_STORAGE - 
* BIND_ACCESSIBILITY_SERVICE - to perform core functionality. user WILL be asked to enable this anyway. Application couldn't do it's job without it. TODO: add note somewhere how exactly we use it (more details)

## Thanks to
* [Freepik from Flaticon]( http://www.flaticon.com/free-icon/marionette-puppet-silhouette_33882) for application icon
At time I got it license was:
License: Creative commons
You are free to use this icon for commercial purposes, to share or to modify it. In exchange, it's necessary to credit the author for the original creation.
* [Parse Team (currently at Facebook)](https://parse.com/about) for Parse Platform (and ParseUI-Android)
* [Mantano SAS](www.mantano.com/mantano-reading-platform/) for Mantano Premium. This is best E-Book reader I have so far, even accounting for occasional glitches with Cloud Service and crashes with badly formatted books. Current version of Reading Tracker only support Mantano as data source.



#Russian

## Что это?
Приложение для сборка данных о том как вы читаете. Какие книги. Когда. Сколько.
Для того чтобы вы могли сами проанализировать эти данные


## Как оно работает?
* В начале была фраза "Amazon Kindle докладывает, в какие страницы каких книг заглядывает пользователь и когда он это делает"...но мне захотелось тоже иметь эту информацию. Для тех книг что я читаю.
* TODO:даль

## Как оно работает (Технически подробности)
* С использованием API Специальных Возможностей (Accessibility API) Android'а отслеживается структура элементов управления и читается часть текста с них.
За счет этого отслеживается находимся ли мы в Библиотеке или в конкретной книге. И где именно в книге мы находимся.

* Затем все отсылается в мое приложение на Parse Platform
* на сервере при приеме данных запускаются функции CloudCode которые выполняют дополнительную обработку (отсылку e-mail когда пользователь переключился с книги например)

### И не тормозит?
Нет. Разумеется когда читалка не активна, никакого отслеживания не выполняется.

## Требования
* Android 4.2
* поддерживаемая читалка
* разрешение приложению доступа в 'Специальных возможностях'

## Оно вообще - работает?

* Проверено на:
 * HP Slate 21 (не "Pro") c Android 4.2
 * Sony Xperia Tablet Z2 с родной оболочкой с Android 4.4
 * Sony Xperia Z Ultra с CyanogenMod
 * Nexus 5/7/10...как с CyanogenMod так и без

* для Android 4.2 используется чуть другой метод работы из-за отличающегося поведения API Специальных Возможностей. Вообще если бы не тот HP Slate 21 то минимальной версией был бы 4.3 и все работало бы чуть чуть надежднее

* Собираемость текущей версии и прохождение автотестов (знаю что тестируется далеко не все...пока что): ![current build status according to Travis CI](https://travis-ci.org/intari/readingtracker.svg?branch=master)

## Под какой лицензий код?
GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
Я оставляю за собой право изменить лицензию на любую другую когда это будет необходимым.
ParseUI-Android разумеется под своей собственной лицензий. Как и бинарные библиотеки Parse Platform / HockeyApp и остальные сторонние

## Какие читалки поддерживаются?
* Пока только Mantano Reader. А зачем вам что-то еще?

## Приложение только собирает данные. А как их использовать?
Красивые картинки со статистикой будут позднее.
Пока серверная часть только посылает e-mail'ы и дописывает в заметку Evernote

## А Privacy Policy есть?
Если вы сами соберете сборку, то это уже к вам вопрос
Когда приложение будет в Google Play Store я подумаю что написать

## В Google Play Store приложение есть?
Пока нет (как и в остальных местах). Но рано или поздно будет.

## Какие сторонние сервисы используются?
* [HockeyApp](hockeyapp.net) - для обновлений тестовых версий
* [Parse](parse.com) - хранилище данных, серверная логика, аналитика, сборка данных о крешах
* [Mandrill](mandrillapp.com) - рассылка нотификацией о процессе чтения


## Сборка без API-ключей
Приложение просто не соберется. Так намеренно сделано. Получите свои и укажите.
Ну или подправьте build.gradle

## Как настроить API-ключи, подпись,etc
* cd $HOME
* mkdir .androidSigning/
* touch versionCodesBookTrackerProperties
* в versionCodesBookTrackerProperties напишите VERSION_CODE=0 (или другое)
* touch booktrackerAPIKeys
* в booktrackerAPIKeys добавьте:
  * PARSE_APP_ID=your_parse_app_id (https://parse.com/ , Application used Parse as network backend
  * PARSE_CLIENT_KEY=your_parse_client_key
  * PARSE_MASTER_KEY=master_key_for your_parse_application (used for crash reporting)
  * HOCKEYAPP_APP_ID=идентификатор приложения для HockeApp (используется для проверки апдейтов)
  * HOCKEYAPP_TOKEN=токен_HockeyApp_для данного приложения (используется для загрузки новой версии)
  * PARSE_APP_ID_FOR_TEST_HARNESS=пока не используется но указать что-то надо
  * PARSE_CLIENT_KEY_FOR_TEST_HARNESS=пока не используется но указать что-то надо
  * PARSE_USERNAME_FOR_TEST_HARNESS=пока не используется но указать что-то надо
  * PARSE_PASSWORD_FOR_TEST_HARNESS=пока не используется но указать что-то надо
  пути можно помять в  gradle.properties

* или посмотрите в build.gradle как задать это же с использованием переменных окружения


## Будут ли выложеные исходники той части что на JavaScript для CloudCode?
Может быть

## Какие используется Permission'ы Андроида и зачем
* RECEIVE_BOOT_COMPLETED  - to start at device boot
* INTERNET - to send readings report to server
* ACCESS_NETWORK_STATE - for analytics & error reporting
* READ_PHONE_STATE - to get hardware details for statistical purposes
* GET_TASKS - to knew currently active app. This is needed to detected situation when user switches away from reading app
* WRITE_EXTERNAL_STORAGE -
* BIND_ACCESSIBILITY_SERVICE - to perform core functionality. user WILL be asked to enable this anyway. Application couldn't do it's job without it. TODO: add note somewhere how exactly we use it (more details)

# Благодарности
* [Freepik from Flaticon]( http://www.flaticon.com/free-icon/marionette-puppet-silhouette_33882) текущая иконка
Когда я ее нашел - лицензия была:
License: Creative commons
You are free to use this icon for commercial purposes, to share or to modify it. In exchange, it's necessary to credit the author for the original creation.
* [Parse Team (currently at Facebook)](https://parse.com/about) -  Parse Platform (and ParseUI-Android)
* [Mantano SAS](www.mantano.com/mantano-reading-platform/) for Mantano Premium. This is best E-Book reader I have so far, even accounting for occasional glitches with Cloud Service and crashes with badly formatted books. Current version of Reading Tracker only support Mantano as data source.




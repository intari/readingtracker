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
* Checked on:
 * HP Slate 21 (not "Pro") with Android 4.2
 * Sony Xperia Tablet Z2 (stock firmware) with с Android 4.4
 * Sony Xperia Z Ultra with CyanogenMod
 * Nexus 5/7/10...with CyanogenMod or with stock

* I used slightly different method for Android 4.2 because Accessibility API works in slightly different way here (Target Android version would be 4.3 if not for my HP Slate 21)

* According to Travis CI current build status is ![current build status according to Travis CI](https://travis-ci.org/intari/readingtracker.svg?branch=master) or look yourself how it gets build at https://travis-ci.org/intari/readingtracker

## How it is licensed?
* For now only GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
* I also reserve right to re-license my code under any other license for any purpose I want. 
* Included ParseUI-Android library is (of course) licensed under it's own license. Same applies to Hockeyapp's binaries and other 3rd-party libraries

## Which E-Book Reading programs are supported?
* Only Mantano Reader Premium currently supported (it's great app! Why you need anything else?)

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
  * TODO:describe others
  you can change paths in gradle.properties
* or look at build.gradle to determine how to use env vars

## grantAnimationPermission / Espresso Tests are failing...
if you Espresso tests and your device is not configured to disable animations manually you should uncomment lines around 'grantAnimationPermission' in build.gradle.
See [This Gist](https://gist.github.com/intari/2a3f0b79ba95aa31f95a) for example.
See [Disabling Animations](https://code.google.com/p/android-test-kit/wiki/DisablingAnimations) to knew why this is needed


## It does not build on Windows
currently getVersionCodeFromNetwork() from build.gradle doesn't work on Windows
Simple solution is just put 'return 0' instead of it's content.

## Will sources for  Parse CloudCode part be available?
Maybe later

## Which Android Permissions are used and why
Permissions
* RECEIVE_BOOT_COMPLETED  - to be able to catch moments when you start read
* INTERNET - communication with our server
* ACCESS_NETWORK_STATE - usage analytics & error reportings
* READ_PHONE_STATE - statistics
* GET_TASKS - to knew when you switch from supported reading app to something other
* WRITE_EXTERNAL_STORAGE -
* BIND_ACCESSIBILITY_SERVICE - main app functionality. you will be asked to provide access to 'Accessibility Services' on startup. You could refuse but you will not be able to get your statistics from this device


## Thanks to
* Freepik from FlatIcon. Icon(s) are based on
<div>Icons made by <a href="http://www.freepik.com" title="Freepik">Freepik</a> from <a href="http://www.flaticon.com" title="Flaticon">www.flaticon.com</a>         is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0">CC BY 3.0</a></div>
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
* TODO:дальше

## Как оно работает (Технически подробности)
* С использованием API Специальных Возможностей (Accessibility API) Android'а отслеживается структура элементов управления и читается часть текста с них.
За счет этого отслеживается находимся ли мы в Библиотеке или в конкретной книге. И где именно в книге мы находимся.

* Затем все отсылается в мое приложение на Parse Platform
* на сервере при приеме данных запускаются функции CloudCode которые выполняют дополнительную обработку (отсылку e-mail когда пользователь переключился с книги например)

### И не тормозит?
Нет. Разумеется когда читалка не активна, никакого отслеживания не выполняется.

## Требования
* Android 4.3+
** ранее поддерживался и 4.2, исходники последнией версии с поддержкой 4.2 - https://github.com/intari/readingtracker/tree/Last_API_17_Android_4_2
* поддерживаемая читалка
* разрешение приложению доступа в 'Специальных возможностях'

## Оно вообще - работает?

* Проверено на:
 * HP Slate 21 (не "Pro") c Android 4.4
 * Sony Xperia Tablet Z2 с родной оболочкой с Android 4.4
 * Sony Xperia Z Ultra с CyanogenMod
 * Nexus 5/7/10...как с CyanogenMod так и без
* Собираемость текущей версии и прохождение автотестов (знаю что тестируется далеко не все...пока что): ![current build status according to Travis CI](https://travis-ci.org/intari/readingtracker.svg?branch=master), вы можете посмотреть как оно собирается на https://travis-ci.org/intari/readingtracker

## Работало же 4.2 - зачем убрано?
* для Android 4.2 используется чуть другой метод работы из-за отличающегося поведения API Специальных Возможностей. Вообще если бы не тот HP Slate 21 где Kitktat вышел 29 декабря 2014 то минимальной версией был бы 4.3. Поддержку других читалок тоже будет обеспечить проще.


## Под какой лицензий код?
GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
Я оставляю за собой право изменить лицензию на любую другую когда это будет необходимым.
ParseUI-Android разумеется под своей собственной лицензий. Как и бинарные библиотеки Parse Platform / HockeyApp и остальные сторонние

## Какие читалки поддерживаются?
* Пока только Mantano Reader (Lite & Essentials редакции тоже должны работать но тестирования было очень мало) . А зачем вам что-то еще?

## Приложение только собирает данные. А как их использовать?
Красивые картинки со статистикой будут позднее.
Пока серверная часть только посылает e-mail'ы и дописывает в заметку Evernote

## А Privacy Policy есть?
Если вы сами соберете сборку, то это уже к вам вопрос
Для Google Play Store версии скоро будет

## В Google Play Store приложение есть?
* Разумеется - [Google Play Store](https://play.google.com/store/apps/details?id=com.viorsan.readingtracker)

## Какие сторонние сервисы используются?
* [HockeyApp](hockeyapp.net) - для обновлений тестовых версий
* [Parse](parse.com) - хранилище данных, серверная логика, аналитика, сборка данных о крешах
* [Mandrill](mandrillapp.com) - рассылка нотификацией о процессе чтения
* [Flurry](flurry.com) - аналитика (видимо буду убирать)
* [Countly](countly.com) - аналитика (пока используется бесплатная версия Countly Cloud)


## Сборка без API-ключей
Приложение просто не соберется. Так намеренно сделано. Получите свои и укажите.
Ну или подправьте build.gradle

## Как настроить API-ключи, подпись,etc
* cd $HOME
* mkdir .androidSigning/
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
  * COUNTLY_SERVER = count.ly analytics server URL
  * COUNTLY_APPKEY = count.ly analytics App Key
  * TODO:остально (про систему работы с versionCode)
  пути можно помять в  gradle.properties

* или посмотрите в build.gradle как задать это же с использованием переменных окружения


## Будут ли выложеные исходники той части что на JavaScript для CloudCode?
Может быть

## Какие используется Permission'ы Андроида и зачем
* RECEIVE_BOOT_COMPLETED  - запуск мониторинга с момента загрузки
* INTERNET - общение с серверной частью
* ACCESS_NETWORK_STATE - анатилитка и отсылка ошибок
* READ_PHONE_STATE - статистика
* GET_TASKS - отслеживание ситуации когда вы переключаетесь с поддерживаемой читалки на что-то е-е
* WRITE_EXTERNAL_STORAGE -  хранить временные данны
* BIND_ACCESSIBILITY_SERVICE - для работы основного функционала приложения (приложения попросит при запуске разрешить доступ к 'Специальным возможностям', вы можете отказаться но сбор аналитики по книгам работать не будет)
* WAKE_LOCK / VIBRATE / GET_ACCOUNTS - для работы пуш-нотификаций

## Проблемы
* Q:Приложение спрашивает доступ к Специальным возможностям хотя уже выдано.
  A:Вы видимо недавно обновили приложение. Перезагрузите устройство либо отключите доступ и тут же включите

## О номерах страниц (TODO: вынести это отдельно)
В отчетах есть: перелистывания (pageSwitches), начальные/конечные/прочитанные/текущие номера страниц.

### Отрицательное количество страниц это как?
это когда чтение закончено на более ранеей позиции чем начато (может это справочник какой то)

### Что считается 'номером страницы' вообще?
то что считает таковым поддерживаемый ридер.
в случае Mantano Reader это так называеме ADE Pages (придуманные Adobe для Adobe Digital Editions)

* они специфичны для книги
* они могут соответствовать печатной книге а могут и не соответсвовать
* автор EPUB-файла может явно указать какие места каким страницам соответствуют - смотри https://blog.safaribooksonline.com/2009/11/26/adobe-page-map-versus-ncx-pagelist/
* если автор не указал - используется 'стандартное' разбиение (1024 символа на страницу насколько я помню)
* ADE Pages не зависят от разрешения экрана.
** Что кстати означает что если у нас Landscape-ориентация и включен двух-страничный режим то совершенно запросто одной 'странице'  экранной могут соответствовать две или боле ADE Pages
** На некоторых мелких экранах может быть наоборот
* то что делает Calibre для подсчета страниц - это приближение

все упомянания 'страниц' в приложении - это как раз поддерживаемые ридером страницы. отсюда и проблемы с тем что могут быть скачки 'через страницу'

### Перелистывания / pageSwitch
это просто - смена пользователем текущей страницы (нажатие на стрелочки)


# Благодарности
* Freepik from FlatIcon. Icon(s) are based on
<div>Icons made by <a href="http://www.freepik.com" title="Freepik">Freepik</a> from <a href="http://www.flaticon.com" title="Flaticon">www.flaticon.com</a>         is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0">CC BY 3.0</a></div>
At time I got it license was:
License: Creative commons
You are free to use this icon for commercial purposes, to share or to modify it. In exchange, it's necessary to credit the author for the original creation.
* [Parse Team (currently at Facebook)](https://parse.com/about) -  Parse Platform (and ParseUI-Android)
* [Mantano SAS](www.mantano.com/mantano-reading-platform/) for Mantano Premium. This is best E-Book reader I have so far, even accounting for occasional glitches with Cloud Service and crashes with badly formatted books. Current version of Reading Tracker only support Mantano as data source.




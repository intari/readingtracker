package com.viorsan.readingtracker;

import android.app.ActivityManager;
import android.content.*;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.parse.ParseObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 14.09.14.
 * incapsulated logic to monitor book readings
 *
 */
public class BookReadingsRecorder {
    static final String TAG = "ReadingTracker::B.R.R.";

    public static final String REPORT_TYPE_BOOK_READING_SESSION_COMPLETED = "BookReadingSesssionCompleted";
    public static final String READING_SESSION_TIME_MS = "readingSessionTimeMS";
    public static final String READING_SESSION_TIME = "readingSessionTime";
    //How we are report ourselves in our brodcasts
    public static final String OUR_DATA_SOURCE = "com.viorsan.readingtracker.BookReadingMonitor";
    //broadcast name
    public static final String BOOK_READING_STATUS_UPDATE = "com.viorsan.BookMonitoring.BookReadingUpdate";
    //broadcast params&field names
    public static final String BOOK_TITLE = "bookTitle";
    public static final String BOOK_AUTHOR = "bookAuthor";
    public static final String BOOK_TAGS = "bookTags";
    public static final String TOTAL_PAGES = "totalPages";
    public static final String CURRENT_PAGE = "currentPage";
    public static final String READING_APPLICATION = "readingApplication";
    public static final String DATA_SOURCE = "dataSource";
    //name to store last read book title and author

    public static final String BOOK_READING_PREFS="com.viorsan.LastReadBooks";
    public static final String TIME_PASSED = "TimePassed";
    public static final String TIME_PASSED_IN_SECONDS ="TimePassedInSeconds";
    public static final String REPORT_TYPE_BOOK_READING_PROGRESS_REPORT = "BookReadingProgressReportV2";//non-V2 used time in ms and not seconds
    public static final String DEVICE_TYPE = "deviceType";
    public static final double MIN_SECONDS_TO_READ_PAGE = 1.0;
    public static final double MAX_SECONDS_TO_READ_PAGE = 180.0;
    public static final double MS_IN_SECOND = 1000.0;
    public static final String START_PAGE = "startPage";
    public static final String PAGES_READ = "pagesReadSinceSessionStart";
    public static final String END_PAGE = "endPage";
    public static final String NUM_PAGE_SWITCHES = "numPageSwitchesSinceSessionStart";
    public static final String READING_SESSION = "readingSession";


    private static BookReadingsRecorder self=null;
    private BroadcastReceiver statusRequestReceiver;

    /**
     * BookReadingsRecorder is singleton. So this function returns it's instance, creating it if necessary
     * @param context - Context instance for use with updateStatusRequestReceiver (for using with LocalBroadcastManager)
     * @return instance of BookReadingsRecorder to work with
     */
    public static BookReadingsRecorder getBookReadingsRecorder(Context context) {
        if (self==null) {
            Log.i(TAG,"Class BookReadingsRecorder not created. doing so");

            self=new BookReadingsRecorder();
            self.updateDeviceInfo();
            //TODO:allow user to specify device name (i.e. jessica22)
            self.updateStatusRequestReceiver(context);

        }
        //Log.i(TAG,"Return singleton value");
        return self;
    }
    //TODO: think about moving exceptions to separate class so they can be reused
    public class InternalInconsistenceException extends Exception {
        public InternalInconsistenceException (String message) {
            super(message);
        }
    };
    public class InvalidArgumentsException extends Exception {
        public InvalidArgumentsException (String message) {
            super(message);
        }
    };

    //support vars


    String currentPageNumbers;
    String currentBookTitle;
    String currentBookAuthor;
    long currentTimestamp=0;
    boolean currentBookKnown=false;
    String currentBookTags;
    Pattern pagenumberParsePattern=Pattern.compile("(\\d+)\\s*/\\s*(\\d+)");
    String currentTotalPages;
    String currentPage;
    String prevBookTitle;
    String prevBookAuthor;
    long prevTimestamp;
    String prevBookTags;
    String prevTotalPages;
    String prevCurrentPage="";
    long totalTimeForCurrentBook;
    String deviceInfoString=null;


    boolean lastReadBookKnown=false;
    String lastBookTitle;
    String lastBookAuthor;
    String lastCurrentPage;
    String lastTotalPages;
    String lastBookTags;
    long  totalTimeForLastBook;

    long startedPage=0;//starting page, using to calculate amount of pages read
    long numPagePageSwitches=0;//total number of page switches

     /**
     * Get's 'deviceType' string for use in many reports
     * This is static method because MainActivity.updateInstallationObject() also needs it
     * @return 'deviceType' string
     */
    public static String getDeviceInfoString() {
        return ""+Build.MANUFACTURER+" "+Build.MODEL;//+" ("+Build.PRODUCT+")";
    }
    private void updateDeviceInfo() {
        DeviceInfoManager deviceInfoManager = new DeviceInfoManager();
        //yes, this will result in denormalized data. but I need it. and need bpm much less here
        deviceInfoString=BookReadingsRecorder.getDeviceInfoString();
        Log.i(TAG, "Device information string is " + deviceInfoString + "|");
    }

    /**
     * Stores last known information about book user read to SharedPreferences
     * @param context - context to use with SharedPreferences
     * @param timestamp - timestamp when this data were actual
     * @param title - book title
     * @param author - author or authors (can be , or & - separate)
     * @param tags - genre tags
     */
    private void writeLastBookInfo(Context context, long timestamp, String title,String author, String tags) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(BOOK_READING_PREFS, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(BOOK_TITLE,new String(title));
        editor.putString(BOOK_AUTHOR,new String(author));
        editor.putString(BOOK_TAGS,new String(tags));

        editor.commit();

    }

    private void copyCurrentToLast() {
        if (currentBookKnown) {
            lastReadBookKnown=true;
            lastBookAuthor=currentBookAuthor;
            lastBookTitle=currentBookTitle;
            lastCurrentPage=currentPage;
            lastTotalPages=currentTotalPages;
            lastBookTags=currentBookTags;
            totalTimeForLastBook=totalTimeForCurrentBook;
        }
    }

    /**
     * Reads last known information about book read some time ago. This is usually done on startup
     * Also sends necessary events to analytics services
     * @param context - - context to use with SharedPreferences
     * @param timestamp
     */
    private void readLastBookInfo(Context context,long timestamp) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(BOOK_READING_PREFS, Context.MODE_PRIVATE);
        currentBookTitle=sharedPreferences.getString(BOOK_TITLE,"Unknown");
        currentBookAuthor=sharedPreferences.getString(BOOK_AUTHOR,"Unknown");
        currentBookTags=sharedPreferences.getString(BOOK_TAGS,"");
        currentTimestamp= timestamp;

        prevCurrentPage="";

        currentBookKnown=true;
        copyCurrentToLast();

    }

    /**
     * Records new book being read. Stores initial data about book
     * @param context - context to use with writeLastBookInfo/recordPageSwitch
     * @param timestamp - timestamp when this data were actual
     * @param author - author or authors (can be , or & - separate)
     * @param title - - book title
     * @param tags - - genre tags
     * @param pageNumbers - initial unprocessed version of current pagenumber(s). See recordPageSwitch()
     * @throws InvalidArgumentsException
     */
    public void recordNewBook(Context context,long timestamp, String author,String title, String tags,String pageNumbers) throws InvalidArgumentsException {

        if (author==null) {
            throw new InvalidArgumentsException("Author is null");
        }
        if (title==null) {
            throw  new InvalidArgumentsException("Title is null");
        }

        if (pageNumbers==null) {
            throw new InvalidArgumentsException("pageNumbers==null");
        }

        if ((!author.equals(currentBookAuthor)) || (!title.equals(currentBookTitle))) {
            //record switch
            Log.i(TAG,"recordNewBook: author or title are different. recording switch away");
            recordSwitchAwayFromBook(context,timestamp);

        }
        //may be it's not NEW book being opened...
        currentBookAuthor=author;
        currentBookTitle=title;
        currentBookTags=tags;
        currentBookKnown=true;
        currentPageNumbers=pageNumbers;
        currentTimestamp=timestamp;
        prevCurrentPage="";

        writeLastBookInfo(context,timestamp, currentBookTitle, currentBookAuthor,currentBookTags);
        numPagePageSwitches=0;


        /*
        Map<String, String> dimensions = new HashMap<String, String>();
        dimensions.put(BOOK_TITLE,currentBookTitle);
        dimensions.put(BOOK_AUTHOR,currentBookAuthor);
        dimensions.put(BOOK_TAGS,currentBookTags);
        */

        //TODO:describe this in privacy policy, and really think if we need THIS data in 3rd-party analytical systems
        //MyAnalytics.trackEvent("readingSessionStarted", dimensions);
        MyAnalytics.trackTimedEventStart(READING_SESSION);
        
        recordPageSwitch(context,timestamp,pageNumbers);
        //at least on 5.0.1 it's possible that recordPageSwitch will not parse currentPage on initial book opening so...
        if (currentPage!=null) {
            startedPage=Long.valueOf(currentPage);
        }
    }

    /**
     * configured LocalBroadcastManager to listen for requests from UI to get status updates
     * @param outerContext - Context instance for use with LocalBroadcastManager
     */
    private void updateStatusRequestReceiver(Context outerContext) {
        statusRequestReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "Got request for status update (book)");

                sendStatusUpdateToUI(context,false);


            }
        };
        LocalBroadcastManager.getInstance(outerContext).registerReceiver(
                statusRequestReceiver, new IntentFilter(AccessibilityRecorderService.ACTIVITY_MONITORING_STATUS_UPDATE_REQUEST)
        );
    }

    /**
     * Sends requested by UI status update to UI if we can do that
     * @param context - context to use with LocalBroadcastManager
     * @param now - currently unused
     */
    private void sendStatusUpdateToUI(Context context, boolean now) {

        if (lastReadBookKnown) {

            //Log.i(TAG, "sendStatusUpdateToUI...sending updated information");

            Intent intent = new Intent(BOOK_READING_STATUS_UPDATE);
            intent.putExtra(BOOK_TITLE,lastBookTitle);
            intent.putExtra(BOOK_AUTHOR,lastBookAuthor);
            intent.putExtra(BOOK_TAGS,lastBookTags);
            intent.putExtra(READING_SESSION_TIME,totalTimeForLastBook/ MS_IN_SECOND);
            intent.putExtra(CURRENT_PAGE,lastCurrentPage);
            intent.putExtra(TOTAL_PAGES,lastTotalPages);
            long pagesRead=Long.valueOf(currentPage)-startedPage;
            intent.putExtra(PAGES_READ,pagesRead);//pages read, as in 'endPage-startPage'
            intent.putExtra(NUM_PAGE_SWITCHES,Long.valueOf(numPagePageSwitches));//number of times user switches page

            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        }
        else
        {
            //Log.i(TAG, "sendStatusUpdateToUI...last current book not known");

        }
    }

    /**
     * Records page switch
     * extracts real current page number from pageNumbers
     * sends analytics events
     * sends information to Parse Platform
     * sends status update to UI
     *
     * @param context - context to use with sendStatusUpdateToUI/ParsePlatformUtils
     * @param timestamp - timestamp when this data were actual
     * @param pageNumbers - initial unprocessed version of current pagenumber(s).
     * @throws InvalidArgumentsException
     */
    public void recordPageSwitch(Context context,long timestamp, String pageNumbers) throws InvalidArgumentsException  {

        if (pageNumbers==null) {
          throw new InvalidArgumentsException("pageNumbers==null");
        }
        Matcher matcher=pagenumberParsePattern.matcher(pageNumbers);


        if (matcher.matches()) {

            if (matcher.groupCount()!=2) {
                Log.i(TAG,"Patter matcher has "+matcher.groupCount()+" groups (should be 2).Pagenumbers are "+pageNumbers+"|. Possible change in mantano");
            }

            String currPage = matcher.group(1);
            String totalPages = matcher.group(2);
            if (!currentBookKnown) {
                Log.i(TAG, "Reading last known book...");
                readLastBookInfo(context,timestamp);
            }
            //currentTimestamp = SystemClock.elapsedRealtime();
            long timePassed = 0;
            timePassed=timestamp-currentTimestamp;



            double timePassedInSeconds=timePassed/ MS_IN_SECOND;
            //I like to see those stats too
            /*
            if (timePassedInSeconds < MIN_SECONDS_TO_READ_PAGE) {
                Log.i(TAG,"BookReadingsRecorder:RecordPageSwitch:read for less than "+MIN_SECONDS_TO_READ_PAGE+" seconds. Only "+timePassedInSeconds+" seconds. Not recording. early exit");
                return;

            }
            */


            //this should never happen anymore due to logic fixes in service
            if (pageNumbers.equals(prevCurrentPage)) {
                Log.i(TAG,"Page number is same "+pageNumbers+". early exit");
                //currentTimestamp=currentTimestamp-timePassed;//ignore this page at all
                return;
            }
            //only update timestamp if we decide to record this page (otherwise account this time to prev time
            //or if it took too long

            currentPage=currPage;
            currentTotalPages=totalPages;
            totalTimeForCurrentBook=totalTimeForCurrentBook+timePassed;
            currentTimestamp=timestamp;

            prevCurrentPage=pageNumbers;

            numPagePageSwitches++;
            if (timePassedInSeconds >MAX_SECONDS_TO_READ_PAGE) {
                Log.i(TAG,"BookReadingsRecorder:RecordPageSwitch:read for more  than "+MAX_SECONDS_TO_READ_PAGE+" seconds. Only "+timePassedInSeconds+" seconds. Assuming user was away. Not recording.early exit");
                //account for time used!
                //currentTimestamp=currentTimestamp-timePassed;//ignore this page at all
                Map<String, String> dimensions = new HashMap<String, String>();
                dimensions.put(BOOK_TITLE,currentBookTitle);
                dimensions.put(BOOK_AUTHOR,currentBookAuthor);
                dimensions.put(BOOK_TAGS,currentBookTags);
                dimensions.put(CURRENT_PAGE,currentPage);
                dimensions.put(TOTAL_PAGES,totalPages);
                dimensions.put(NUM_PAGE_SWITCHES,Long.valueOf(numPagePageSwitches).toString());
                dimensions.put(TIME_PASSED,Double.valueOf(timePassedInSeconds).toString());

                /*
                 * One page was read (or at least user switched pages. Possbile backwards!)
                 * And it took too much time so user likely was busy doing something else
                 * so only report to analytics system but NOT to Parse (because it could confuse stats)
                 */
                //TODO:report those events to Parse. to process...somehow
                //MyAnalytics.trackEvent("pageReadTooLong",dimensions);

                return;
            }

            Log.i(TAG, "Title:" + currentBookTitle + ". author " + currentBookAuthor + ". tags:" + currentBookTags + ". page " + currentPage + " of " + currentTotalPages + ". " + timePassed/MS_IN_SECOND + " aka "+timePassedInSeconds +" seconds passed ( "+totalTimeForCurrentBook / MS_IN_SECOND + " seconds total for this book in this session)");


            copyCurrentToLast();

            //send status update UI so if user wants to look - s/he can
            sendStatusUpdateToUI(context, true);

            /*
             * One page was read (or at least user switched pages. Possbile backwards!)
             * Report details to Parse
             */
            ParseObject report=new ParseObject(REPORT_TYPE_BOOK_READING_PROGRESS_REPORT);
            //Title of book currently read
            report.put(BOOK_TITLE,currentBookTitle);
            //Author of currenly read book
            report.put(BOOK_AUTHOR,currentBookAuthor);
            /*
             * comma-separated list of tags, like:
             * romance_sf, sf_space
             * sf_action
             * Development,iOS Development, Languages & Tools
             * sci, sci_space
             *
             */
            report.put(BOOK_TAGS,currentBookTags);
            // time for which this page was read, in seconds
            report.put(TIME_PASSED,timePassedInSeconds);






            /* Current page.
               For Mantano this means ADE page.
               see https://www.evernote.com/shard/s11/nl/1252762/ddd07bc4-afd1-4d5e-8331-899db04957f7/
               if you don't have access just see https://blog.safaribooksonline.com/2009/11/26/adobe-page-map-versus-ncx-pagelist/
               and https://www.goodreads.com/topic/show/701413-my-ebook-isbn-is-different-as-well-as-page-count
               Very short summary:
               - ADE Pages are book-specific
               - Number of pages usually correspond to printed book (if any) but not always equal
               - current/total pages are NOT depended on screen size
               - on some small screen devices several screen pages correspond to one ADE page. On this devices this function will be called several times with identical currentPage
               - on large screens in dual-page mode sometimes every 2nd page will be reported
               - Calibre's calculator is approximation
               For Kindle, if/then it will be supported it will be either location OR Kindle pages (depending on specific book)
             */
            report.put(CURRENT_PAGE,currentPage);
            //Total pages in this book
            report.put(TOTAL_PAGES,totalPages);
            //basic device information string
            report.put(DEVICE_TYPE,deviceInfoString);
            /*
             * number of page turns which leads to this page. i.e. how much time user switched times. no direct relation to current page,e
             * it's possible for user to go backwards or one on-screen page not be one 'page' in terms currentPages uses...see above
             */
            report.put(NUM_PAGE_SWITCHES,numPagePageSwitches);

            ParsePlatformUtils.saveReportToParse(report,context);

            Map<String, String> dimensions = new HashMap<String, String>();
            dimensions.put(BOOK_TITLE,currentBookTitle);
            dimensions.put(BOOK_AUTHOR,currentBookAuthor);
            dimensions.put(BOOK_TAGS,currentBookTags);
            dimensions.put(CURRENT_PAGE,currentPage);
            dimensions.put(TOTAL_PAGES,totalPages);
            dimensions.put(NUM_PAGE_SWITCHES,Long.valueOf(numPagePageSwitches).toString());
            dimensions.put(TIME_PASSED,Double.valueOf(timePassedInSeconds).toString());

            /*
             * One page was read (or at least user switched pages. Possbile backwards!)
             * but report details to (possibly) 3rd-party analytics system(s)
             * In future some of personal details will be masked
             */
            //MyAnalytics.trackEvent("pageRead",dimensions);




            // Book Switches are taken care of in other places
            // TODO:send update notifications if needed

            //is this important?
            /*
            //TODO: finally use our own reporting db
            prevBookAuthor=currentBookAuthor;
            prevBookTitle=currentBookTitle;
            prevBookTags=currentBookTags;
            prevTimestamp=currentTimestamp;
            prevCurrentPage=currPage;
            prevTotalPages=totalPages;
            */
        }
        else
        {
            Log.i(TAG,"No matches for "+pageNumbers+"|. possible not page number");
        }

    }

    //based off http://stackoverflow.com/questions/24625936/getrunningtasks-doesnt-work-in-android-l
    String[] getActivePackagesCompat(Context context,ActivityManager activityManager) {
        final List<ActivityManager.RunningTaskInfo> taskInfo = activityManager.getRunningTasks(1);
        final ComponentName componentName = taskInfo.get(0).topActivity;
        final String[] activePackages = new String[1];
        activePackages[0] = componentName.getPackageName();
        return activePackages;
    }
    String[] getActivePackages(Context context,ActivityManager activityManager) {
        final Set<String> activePackages = new HashSet<>();

        final List<ActivityManager.RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : processInfos) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                activePackages.addAll(Arrays.asList(processInfo.pkgList));
            }
        }
        return activePackages.toArray(new String[activePackages.size()]);
    }

    /**
     * Checks if one if one of supported reading apps are active.
     * Records switchto/from app events.
     * Sends analytics events
     * usually called every few seconds
     * @param context - used for accessing ACTIVITY_SERVICE and  startAnalyticsWithContext/stopAnalyticsWithContext
     */
    public void checkIfReadingAppActive(Context context) {
        ActivityManager activityManager = (ActivityManager)context.getSystemService(context.ACTIVITY_SERVICE);

        if (activityManager == null) {
            Log.i(TAG, "updateProcessList:null activity manager");
            return;

        }

        List<ActivityManager.RunningTaskInfo> appProcesses = activityManager.getRunningTasks(1);
        String topActivity = appProcesses.get(0).topActivity.getPackageName();
        /*
        String[] activePackages;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            activePackages = getActivePackages(context,activityManager);
        } else {
            activePackages = getActivePackagesCompat(context,activityManager);
        }
        boolean readerFound=false;
        for (String act:activePackages) {
            Log.d(TAG,"Package:"+act);
            if (act.equals(AccessibilityRecorderService.MANTANO_READER_PACKAGE_NAME) ||
                    act.equals(AccessibilityRecorderService.MANTANO_READER_ESSENTIALS_PACKAGE_NAME) ||
                    act.equals(AccessibilityRecorderService.MANTANO_READER_LITE_PACKAGE_NAME)
                    ) {
                readerFound=true;
                MyAnalytics.startAnalyticsWithContext(context);
                MyAnalytics.trackEvent("userIsInSupportedReadingApp");
            }

        }
        if (!readerFound) {
            Log.i(TAG, "current activity is not reading app. |");
            recordSwitchAwayFromBook(context, SystemClock.elapsedRealtime());
            MyAnalytics.stopAnalyticsWithContext(context);
        }
        */


        //For now only Mantano Reader is supported
        if (!(topActivity.equals(AccessibilityRecorderService.MANTANO_READER_PACKAGE_NAME)  ||
                topActivity.equals(AccessibilityRecorderService.MANTANO_READER_ESSENTIALS_PACKAGE_NAME)||
                topActivity.equals(AccessibilityRecorderService.MANTANO_READER_LITE_PACKAGE_NAME))
                ){
            Log.i(TAG, "current activity is not reading app. it's "+topActivity+"|");
            recordSwitchAwayFromBook(context, SystemClock.elapsedRealtime());
            MyAnalytics.stopAnalyticsWithContext(context);
        }
        else {
            MyAnalytics.startAnalyticsWithContext(context);
            MyAnalytics.trackEvent("userIsInSupportedReadingApp");
        }


    }

    /**
     * Planned to be used to record switch directly to currently reading book from other apps
     * currently not fully implemented
     * @param context
     * @param timestamp
     */
    public void recordSwitchBackToCurrentBook(Context context, long timestamp) {
        //TODO:this is switch back to reader app if it was arleady active
        //is this ever needed or other calls will help
        // sometimes it's needed
        /*
        if (!currentBookKnown) {
            Log.i(TAG, "Switching back to current book - reading last known book...");
            readLastBookInfo(context,timestamp);
        }
        */

    }

    /**
     * Records switch away from current book due device sleep, user switch to other app or just user switching to reader app's library
     * This mean that:
     * 'BookReadingSesssionCompleted' is being sent to Parse Platform
     * Analytics events are sent
     * @param context
     * @param timestamp - timestamp when this data were actual
     */
    public void recordSwitchAwayFromBook(Context context,long timestamp) {

        if (!currentBookKnown) {
            return;
        }
        if ((currentBookTitle != null) && (currentBookAuthor != null)) {
            Log.i(TAG, "Done reading " + currentBookTitle + " by " + currentBookAuthor + " with tags " + currentBookTags + ". Last page " + currentPage + " of " + currentTotalPages+". Session took "+totalTimeForCurrentBook/MS_IN_SECOND+" seconds");


            ParseObject report=new ParseObject(REPORT_TYPE_BOOK_READING_SESSION_COMPLETED);
            report.put(BOOK_TITLE,currentBookTitle);
            report.put(BOOK_AUTHOR,currentBookAuthor);
            report.put(BOOK_TAGS,currentBookTags);
            //report.put(READING_SESSION_TIME_MS,totalTimeForCurrentBook);
            report.put(READING_SESSION_TIME,totalTimeForCurrentBook/MS_IN_SECOND);
            report.put(DEVICE_TYPE,deviceInfoString);
            //Page on which reading session ended. same comments as for currentPage applies
            report.put(END_PAGE,Long.valueOf(currentPage));

            report.put(START_PAGE,startedPage);
            long pagesRead=Long.valueOf(currentPage)-startedPage;
            report.put(PAGES_READ,pagesRead);//pages read, as in 'endPage-startPage'
            report.put(NUM_PAGE_SWITCHES,Long.valueOf(numPagePageSwitches));//number of times user switches page



            if (totalTimeForCurrentBook > MIN_SECONDS_TO_READ_PAGE) {
                ParsePlatformUtils.saveReportToParse(report,context);
            }
            else
            {
                Log.i(TAG,"BookReadingsRecorder:RecordSwitchAwayFromBooks:reading only "+totalTimeForCurrentBook+" seconds     is not really READING");

            }
            //report analytics

            Map<String, String> dimensions = new HashMap<String, String>();
            if (currentBookTitle!=null) {
                dimensions.put(BOOK_TITLE,currentBookTitle);
            }
            if (currentBookAuthor!=null) {
                dimensions.put(BOOK_AUTHOR,currentBookAuthor);
            }
            if (currentBookTags!=null) {
                dimensions.put(BOOK_TAGS,currentBookTags);
            }
            dimensions.put(END_PAGE,currentPage);
            Double totalReadingSessionTime=totalTimeForCurrentBook/MS_IN_SECOND;
            dimensions.put(READING_SESSION_TIME,totalReadingSessionTime.toString());
            dimensions.put(START_PAGE,Long.valueOf(startedPage).toString());
            dimensions.put(PAGES_READ,Long.valueOf(pagesRead).toString());
            dimensions.put(NUM_PAGE_SWITCHES,Long.valueOf(numPagePageSwitches).toString());

            //TODO:describe this in privacy policy, and really think if we need THIS data in 3rd-party analytical systems
            //MyAnalytics.trackEvent("readingSessionCompleted", dimensions);
            MyAnalytics.trackTimedEventStop(READING_SESSION,dimensions);

            //clear data

            currentBookKnown = false;
            currentBookTitle = "Unknown";
            currentBookAuthor = "Unknown";
            currentBookTags = "";
            totalTimeForCurrentBook=0;
            prevCurrentPage="";



        }



    /*
        // Old code: this will be take car
        //TODO:более правильно - ловить по возврату на главное меню - иначе некорректно время начисляется.
        //но - девайс же может и вообще уснуть -  так что это тоже не стабильно...
        //хотя сон можно отдельно отслеживать как и переключение с приложения
        //но это надо отдельно разделять на activity recorder и book scrobbler (и ловить и смену активных приложений и screen_off)
        //это важнее даже возможно - железки все равно в сон уходят
        if ((prevBookAuthor != null)&&(prevBookTitle != null)) {

            if ((!prevBookAuthor.equals(currentBookAuthor)) || (!prevBookTitle.equals(currentBookTitle))) {
                Log.i(TAG, "Done reading " + prevBookTitle + " by " + prevBookAuthor + " with tags " + prevBookTags + " for " + totalTimeForCurrentBook / MS_IN_SECOND + " seconds. Last page " + prevCurrentPage + " of " + prevTotalPages);


            } else {
                totalTimeForCurrentBook = totalTimeForCurrentBook + timePassed;
            }
        }
        else {
            totalTimeForCurrentBook=totalTimeForCurrentBook+timePassed;

        }

      */
    }
}

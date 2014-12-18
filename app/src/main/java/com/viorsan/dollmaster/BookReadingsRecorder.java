package com.viorsan.dollmaster;

import android.app.ActivityManager;
import android.content.*;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import com.parse.ParseObject;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dkzm on 14.09.14.
 * incapsulated logic to monitor book readings
 *
 */
public class BookReadingsRecorder {
    public static final String REPORT_TYPE_BOOK_READING_SESSION_COMPLETED = "BookReadingSesssionCompleted";
    public static final String READING_SESSION_TIME_MS = "readingSessionTimeMS";
    public static final String READING_SESSION_TIME = "readingSessionTime";
    //Mantano Reader Premium package name. So far it's only supported book reader
    public static final String MANTANO_READER_PACKAGE_NAME = "com.mantano.reader.android";
    //How we are report ourselves in our brodcasts
    public static final String OUR_DATA_SOURCE = "com.viorsan.dollmaster.BookReadingMonitor";
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


    private static BookReadingsRecorder self=null;
    private static ***REMOVED***Service mMasterService=null;
    private BroadcastReceiver statusRequestReceiver;

    /*
      yes, I knew I can use Intents (+LocalBroadcastManager) to avoid this
      or, even better, move Parse logic in separate singleton class (current version uses a lot of code in Master Service). Maybe in future
     */

    public static void setMasterService(***REMOVED***Service masterService) {
        Debug.L.LOG_ACCESSIBILITY_SERVICE(Debug.L.LOGLEVEL_INFO,"Setting up link to master service for BookReadignsRecorder");
        mMasterService=masterService;
    }
    public static BookReadingsRecorder getBookReadingsRecorder(Context context) {
        if (self==null) {
            Debug.L.LOG_ACCESSIBILITY_SERVICE(Debug.L.LOGLEVEL_INFO,"Class BookReadingsRecorder not created. doing so");

            self=new BookReadingsRecorder();
            self.updateDeviceInfo();
            //TODO:allow user to specify device name (i.e. jessica22)
            self.updateStatusRequestReceiver(context);

        }
        //Debug.L.LOG_ACCESSIBILITY_SERVICE(Debug.L.LOGLEVEL_INFO,"Return singleton value");
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

    private void updateDeviceInfo() {
        DeviceInfoManager deviceInfoManager = new DeviceInfoManager();
        //yes, this will result in denormalized data. but I need it. and need bpm much less here
        deviceInfoString=""+Build.MANUFACTURER+" "+Build.MODEL;//+" ("+Build.PRODUCT+")";
        Debug.L.LOG_ACCESSIBILITY_SERVICE(Debug.L.LOGLEVEL_INFO,"Device information string is "+deviceInfoString+"|");
    }

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
            Debug.L.LOG_ACCESSIBILITY_SERVICE(Debug.L.LOGLEVEL_INFO,"recordNewBook: author or title are different. recording switch away");
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
        recordPageSwitch(context,timestamp,pageNumbers);




    }
    private void updateStatusRequestReceiver(Context outerContext) {
        statusRequestReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Debug.L.LOG_ACCESSIBILITY_SERVICE(Debug.L.LOGLEVEL_INFO, "Got request for status update (book)");

                sendStatusUpdateToUI(context,false);


            }
        };
        LocalBroadcastManager.getInstance(outerContext).registerReceiver(
                statusRequestReceiver, new IntentFilter(AccessibilityRecorderService.ACTIVITY_MONITORING_STATUS_UPDATE_REQUEST)
        );
    }
    private void sendStatusUpdateToUI(Context context, boolean now) {

        if (lastReadBookKnown) {

            //Debug.L.LOG_ACCESSIBILITY_SERVICE(Debug.L.LOGLEVEL_INFO, "sendStatusUpdateToUI...sending updated information");

            Intent intent = new Intent(BOOK_READING_STATUS_UPDATE);
            intent.putExtra(BOOK_TITLE,lastBookTitle);
            intent.putExtra(BOOK_AUTHOR,lastBookAuthor);
            intent.putExtra(BOOK_TAGS,lastBookTags);
            intent.putExtra(READING_SESSION_TIME,totalTimeForLastBook/ MS_IN_SECOND);
            intent.putExtra(CURRENT_PAGE,lastCurrentPage);
            intent.putExtra(TOTAL_PAGES,lastTotalPages);

            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
        else
        {
            //Debug.L.LOG_ACCESSIBILITY_SERVICE(Debug.L.LOGLEVEL_INFO, "sendStatusUpdateToUI...last current book not known");

        }
    }
    public void recordPageSwitch(Context context,long timestamp, String pageNumbers) throws InvalidArgumentsException  {

        if (pageNumbers==null) {
          throw new InvalidArgumentsException("pageNumbers==null");
        }
        Matcher matcher=pagenumberParsePattern.matcher(pageNumbers);


        if (matcher.matches()) {

            if (matcher.groupCount()!=2) {
                Debug.L.LOG_ACCESSIBILITY_SERVICE(Debug.L.LOGLEVEL_INFO,"Patter matcher has "+matcher.groupCount()+" groups (should be 2).Pagenumbers are "+pageNumbers+"|. Possible change in mantano");
            }

            String currPage = matcher.group(1);
            String totalPages = matcher.group(2);
            if (!currentBookKnown) {
                Debug.L.LOG_ACCESSIBILITY_SERVICE(Debug.L.LOGLEVEL_INFO, "Reading last known book...");
                readLastBookInfo(context,timestamp);
            }
            //currentTimestamp = SystemClock.elapsedRealtime();
            long timePassed = 0;
            timePassed=timestamp-currentTimestamp;



            double timePassedInSeconds=timePassed/ MS_IN_SECOND;
            //I like to see those stats too
            /*
            if (timePassedInSeconds < MIN_SECONDS_TO_READ_PAGE) {
                Debug.L.LOG_ACCESSIBILITY_SERVICE(Debug.L.LOGLEVEL_INFO,"BookReadingsRecorder:RecordPageSwitch:read for less than "+MIN_SECONDS_TO_READ_PAGE+" seconds. Only "+timePassedInSeconds+" seconds. Not recording. early exit");
                return;

            }
            */


            //this should never happen anymore due to logic fixes in service
            if (pageNumbers.equals(prevCurrentPage)) {
                Debug.L.LOG_ACCESSIBILITY_SERVICE(Debug.L.LOGLEVEL_INFO,"Page number is same "+pageNumbers+". early exit");
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

            if (timePassedInSeconds >MAX_SECONDS_TO_READ_PAGE) {
                Debug.L.LOG_ACCESSIBILITY_SERVICE(Debug.L.LOGLEVEL_INFO,"BookReadingsRecorder:RecordPageSwitch:read for more  than "+MAX_SECONDS_TO_READ_PAGE+" seconds. Only "+timePassedInSeconds+" seconds. Assuming user was away. Not recording.early exit");
                //account for time used!
                //currentTimestamp=currentTimestamp-timePassed;//ignore this page at all
                return;
            }

            Debug.L.LOG_ACCESSIBILITY_SERVICE(Debug.L.LOGLEVEL_INFO, "Title:" + currentBookTitle + ". author " + currentBookAuthor + ". tags:" + currentBookTags + ". page " + currentPage + " of " + currentTotalPages + ". " + timePassed/MS_IN_SECOND + " aka "+timePassedInSeconds +" seconds passed ( "+totalTimeForCurrentBook / MS_IN_SECOND + " seconds total for this book in this session)");


            copyCurrentToLast();

            sendStatusUpdateToUI(context, true);

            ParseObject report=new ParseObject(REPORT_TYPE_BOOK_READING_PROGRESS_REPORT);
            report.put(BOOK_TITLE,currentBookTitle);
            report.put(BOOK_AUTHOR,currentBookAuthor);
            report.put(BOOK_TAGS,currentBookTags);
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
               - Calibre's calculator is approximation
               For Kindle, if/then it will be supported it will be either location OR Kindle pages (depending on specific book)

             */
            report.put(CURRENT_PAGE,currentPage);
            report.put(TOTAL_PAGES,totalPages);
            report.put(DEVICE_TYPE,deviceInfoString);

            if (mMasterService!=null) {
                mMasterService.saveReportToParse(report);
            }
            else
            {
                Debug.L.LOG_ACCESSIBILITY_SERVICE(Debug.L.LOGLEVEL_INFO,"BookReadingsRecorder:RecordPageSwitch:No master service!!!");
            }






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
            Debug.L.LOG_ACCESSIBILITY_SERVICE(Debug.L.LOGLEVEL_INFO,"No matches for "+pageNumbers+"|. possible not page number");
        }

    }
    public void checkIfReadingAppActive(Context context) {
        ActivityManager activityManager = (ActivityManager)context.getSystemService(context.ACTIVITY_SERVICE);

        if (activityManager == null) {
            Debug.L.LOG_ACCESSIBILITY_SERVICE(Debug.L.LOGLEVEL_INFO, "updateProcessList:null activity manager");
            return;

        }
        List<ActivityManager.RunningTaskInfo> appProcesses = activityManager.getRunningTasks(1);
        String topActivity = appProcesses.get(0).topActivity.getPackageName();
        //For now only Mantano Reader is supported
        if (!topActivity.equals(MANTANO_READER_PACKAGE_NAME)) {
            Debug.L.LOG_ACCESSIBILITY_SERVICE(Debug.L.LOGLEVEL_INFO, "current activity is not reading app. it's "+topActivity+"|");
            recordSwitchAwayFromBook(context, SystemClock.elapsedRealtime());
        }

    }
    public void recordSwitchBackToCurrentBook(Context context, long timestamp) {
        //TODO:this is switch back to reader app if it was arleady active
        //is this ever needed or other calls will help
        // sometimes it's needed
        /*
        if (!currentBookKnown) {
            Debug.L.LOG_ACCESSIBILITY_SERVICE(Debug.L.LOGLEVEL_INFO, "Switching back to current book - reading last known book...");
            readLastBookInfo(context,timestamp);
        }
        */

    }
    public void recordSwitchAwayFromBook(Context context,long timestamp) {

        if (!currentBookKnown) {
            return;
        }
        if ((currentBookTitle != null) && (currentBookAuthor != null)) {
            Debug.L.LOG_ACCESSIBILITY_SERVICE(Debug.L.LOGLEVEL_INFO, "Done reading " + currentBookTitle + " by " + currentBookAuthor + " with tags " + currentBookTags + ". Last page " + currentPage + " of " + currentTotalPages+". Session took "+totalTimeForCurrentBook/MS_IN_SECOND+" seconds");


            ParseObject report=new ParseObject(REPORT_TYPE_BOOK_READING_SESSION_COMPLETED);
            report.put(BOOK_TITLE,currentBookTitle);
            report.put(BOOK_AUTHOR,currentBookAuthor);
            report.put(BOOK_TAGS,currentBookTags);
            //report.put(READING_SESSION_TIME_MS,totalTimeForCurrentBook);
            report.put(READING_SESSION_TIME,totalTimeForCurrentBook/MS_IN_SECOND);
            report.put(DEVICE_TYPE,deviceInfoString);



            if (totalTimeForCurrentBook > MIN_SECONDS_TO_READ_PAGE) {

                if (mMasterService!=null) {
                    mMasterService.saveReportToParse(report);

                }
                else
                {
                    Debug.L.LOG_ACCESSIBILITY_SERVICE(Debug.L.LOGLEVEL_INFO,"BookReadingsRecorder:RecordSwitchAwayFromBooks:No master service!!!");
                }

            }
            else
            {
                Debug.L.LOG_ACCESSIBILITY_SERVICE(Debug.L.LOGLEVEL_INFO,"BookReadingsRecorder:RecordSwitchAwayFromBooks:reading only "+totalTimeForCurrentBook+" seconds     is not really READING");

            }


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
                Debug.L.LOG_ACCESSIBILITY_SERVICE(Debug.L.LOGLEVEL_INFO, "Done reading " + prevBookTitle + " by " + prevBookAuthor + " with tags " + prevBookTags + " for " + totalTimeForCurrentBook / MS_IN_SECOND + " seconds. Last page " + prevCurrentPage + " of " + prevTotalPages);


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
package com.viorsan.readingtracker;


import android.accessibilityservice.AccessibilityService;
import android.content.*;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;
import java.util.MissingFormatArgumentException;
import java.util.regex.Pattern;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 06.09.14.
 * based off https://gist.github.com/qihnus/1909616
 */

public class AccessibilityRecorderService extends AccessibilityService {

    static final String TAG = "ReadingTracker::AccessibilityRecorderService";

    /* Mantano-specific things */
    /* if true - we are only scrobble readings
       if false - we are generic accessibility monitor
     */
    public static final Boolean ONLY_SCROBBLE=Boolean.TRUE;

    //Mantano Reader Premium package name. So far it's only supported book reader
    public static final String MANTANO_READER_PACKAGE_NAME = "com.mantano.reader.android";
    //How we are report ourselves in our brodcasts
    public static final String OUR_DATA_SOURCE = "com.viorsan.readingtracker.BookReadingMonitor";
    //broadcast name
    public static final String BOOK_READING_STATUS_UPDATE = "com.viorsan.BookMonitoring.BookReadingUpdate";
    //broadcast params
    public static final String BOOK_TITLE = "bookTitle";
    public static final String BOOK_AUTHOR = "bookAuthor";
    public static final String BOOK_TAGS = "bookTags";
    public static final String TOTAL_PAGES = "totalPages";
    public static final String CURRENT_PAGE = "currentPage";
    public static final String READING_APPLICATION = "readingApplication";
    public static final String DATA_SOURCE = "dataSource";
    //name to store last read book title and author

    public static final String BOOK_READING_PREFS="com.viorsan.LastReadBooks";
    public static final String ACTIVITY_MONITORING_CONNECTED = "com.viorsan.readingtracker.activityMonitoringService.connected";
    public static final String ACTIVITY_MONITORING_STATUS_UPDATE_REQUEST = "com.viorsan.readingtracker.accessibilityRecorderService.requestForStatusUpdate";

    String currentPageNumbers;
    String currentBookTitle;
    String currentBookAuthor;
    long currentTimestamp=-1;
    boolean currentBookKnown=false;
    String bookTags;
    Pattern pagenumberParsePattern=Pattern.compile("(\\d+)\\s*/\\s*(\\d+)");
    long totalPages=-1;
    long currentPage=-1;
    String prevBookTitle;
    String prevBookAuthor;
    long prevTimestamp;
    String prevBookTags;
    String prevTotalPages;
    String prevCurrentPage;
    long totalTimeForCurrentBook;
    String prevPageNumbers="";
    BroadcastReceiver statusRequestReceiver;



    private String getEventType(AccessibilityEvent event) {
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                return "TYPE_NOTIFICATION_STATE_CHANGED";
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                return "TYPE_VIEW_CLICKED";
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                return "TYPE_VIEW_FOCUSED";
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                return "TYPE_VIEW_LONG_CLICKED";
            case AccessibilityEvent.TYPE_VIEW_SELECTED:
                return "TYPE_VIEW_SELECTED";
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                return "TYPE_WINDOW_STATE_CHANGED";
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                return "TYPE_VIEW_TEXT_CHANGED";
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                return "TYPE_WINDOW_CONTENT_CHANGED";
            case AccessibilityEvent.TYPE_ANNOUNCEMENT:
                return "TYPE_ANNOUNCEMENT";
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_START:
                return "TYPE_GESTURE_DETECTION_START";
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_END:
                return "TYPE_GESTURE_DETECTION_END";
            case AccessibilityEvent.TYPE_VIEW_HOVER_ENTER:
                return "TYPE_VIEW_HOVER_ENTER";
            case AccessibilityEvent.TYPE_VIEW_HOVER_EXIT:
                return "TYPE_VIEW_HOVER_EXIT";
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                return "TYPE_VIEW_SCROLLED";
            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_START:
                return "TYPE_TOUCH_INTERACTION_START";
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END:
                return "TYPE_TOUCH_INTERACTION_END";
            case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:
                return "TYPE_TEXT_SELECTION_CHANGED";
            case AccessibilityEvent.TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY:
                return "TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY";
         }
        return "default";
    }

    private String getEventText(AccessibilityEvent event) {
        StringBuilder sb = new StringBuilder();
        for (CharSequence s : event.getText()) {
            sb.append(s+"|");
        }
        return sb.toString();
    }
    private String getSourceInfoText(AccessibilityEvent event) {
        AccessibilityNodeInfo source=event.getSource();
        if (source!=null) {
            CharSequence text=source.getText();
            if (text!=null) {
                return text.toString();
            }
            else
            {
                //Log.d(TAG,"getSourceInfoText:source.getText() returned null");
            }
        }
        else
        {
            //Log.d(TAG,"getSourceInfoText:event.getSource() returned null");
        }
        return "null";
    }
    //https://developer.android.com/reference/android/view/accessibility/AccessibilityNodeInfo.html#getViewIdResourceName()
    private String getSourceViewId(AccessibilityEvent event) {
        /*
        AccessibilityNodeInfo source=event.getSource();
        if (source!=null) {
            String viewId=source.getViewIdResourceName();
            if (viewId!=null) {
                return viewId.toString();
            }
        }
        */
        return null;
    }



    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int windowId=event.getWindowId();
        String eventType=getEventType(event);
        String eventClassName=event.getClassName().toString();
        String sourceViewId=getSourceViewId(event);
        String packageName=event.getPackageName().toString();
        long eventTime=event.getEventTime();
        String eventText=getEventText(event);
        String sourceInfoText=getSourceInfoText(event);



        //we have android:canRetrieveWindowContent="true"
        //https://developer.android.com/reference/android/accessibilityservice/AccessibilityService.html
        //we (usually) have to perform type check..
              /*
        AccessibilityNodeInfo source = event.getSource();
        if (source == null) {
            Log.v(TAG,"no extra info");
            return;
        }
        Log.v(TAG, String.format("onAccessibilityEvent:xtra info:[text] %s",
                source.getText().toString())
        );  */

        //Mantano-specific processing
        if (packageName.equals(MANTANO_READER_PACKAGE_NAME)) {
            /*
             alternative detection

             */
            if (event.getEventType()==AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                if (eventClassName.equals("android.widget.Button")) {
                    //This is unreliable on 4.2/JESSICA26. sourceInfoText just empty
                    //but sometimes it _works_ so hacks with prevPageNumbers
                    //it also can be genre or some other thing...
                    // but recordPageSwitch will take care of this.
                    // so don't perform more detailed regexp check
                    // also on jessica26 in landscape we sometimes get every odd/even page

                    if (sourceInfoText!=null) {
                        if (!sourceInfoText.equals(prevPageNumbers)) {
                            prevPageNumbers=sourceInfoText;
                            Log.d(TAG,"alternate:assuming it's pagenumber overlay, real sourceview id is "+sourceViewId+". source text:"+sourceInfoText);
                            Log.d(TAG,"calling processMantanoPagenumbers in alternate way#1");
                            processMantanoPagenumbers(sourceInfoText);
                        }
                    }

                }
                else if (eventClassName.equals("android.widget.FrameLayout")) {
                    //may be we can get pagenumbers here
                    // This is only way I found to get page numbers on HP Slate 21 with Android 4.2 (variant above didn't work here because sourceInfoText is null)
                    //TODO:validate this code on Kitkat devices too (just in case)
                    AccessibilityNodeInfo source = event.getSource();
                    if (source != null) {
                        if (source.getChildCount() > 2) {
                            AccessibilityNodeInfo child = source.getChild(1);
                            if (child != null) {
                                if (child.getClassName().equals("android.widget.Button")) {
                                    CharSequence childText = child.getText();
                                    if (childText != null) {
                                        String text = childText.toString();
                                        //it also can be genre or some other thing...
                                        // but recordPageSwitch will take care of this.
                                        // so don't perform more detailed regexp check
                                        if (text!=null) {
                                            if (!text.equals(prevPageNumbers)) {
                                                prevPageNumbers=text;
                                                Log.d(TAG, "calling processMantanoPagenumbers in alternate way#2 (got here via FrameLayout hack). text is " + text);
                                                processMantanoPagenumbers(text);
                                            }
                                        }

                                    } else {
                                        //error messages won't make sense anyway without full dump so...
                                        if (!ONLY_SCROBBLE) {
                                            Log.d(TAG, "alternate, alternatative page number hack - childText is null");
                                        }
                                    }
                                } else {
                                    if (!ONLY_SCROBBLE) {
                                        Log.d(TAG, "alternate, alternatative page number hack - child 1 is not android.widget.button");
                                    }
                                }
                            } else {
                                if (!ONLY_SCROBBLE) {
                                    Log.d(TAG, "alternate, alternatative page number hack - child 1 is null");
                                }
                            }
                        } else {
                            if (!ONLY_SCROBBLE) {
                                Log.d(TAG, "alternate, alternatative page number hack - childCount!=3");
                            }
                        }
                    } else {
                        if (!ONLY_SCROBBLE) {
                            Log.d(TAG, "alternate, alternatative page number hack - null source");
                        }
                    }
                } //frame.layout
            }
            if (event.getEventType()==AccessibilityEvent.TYPE_VIEW_CLICKED) {
               if (eventClassName.equals("android.widget.RelativeLayout")) {
                   Log.d(TAG,"alternate:assuming it's book_bloc_item_list, real sourceview id is "+sourceViewId+". source text:"+sourceInfoText);
                   List<CharSequence> unprocessedData=event.getText();
                   for (CharSequence s : event.getText()) {
                       Log.d(TAG, "alternate:Dumping event text:" + s);
                   }
                   Log.d(TAG,"Calling processMantanoBookData in alternate way");
                   processMantanoBookData(unprocessedData);


               }

            }

            if (event.getEventType()==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                if (event.getClassName().toString().equals("com.mantano.android.library.activities.LibraryActivity")) {
                    Log.i(TAG, "Switch to library");
                    BookReadingsRecorder.getBookReadingsRecorder(this.getBaseContext()).recordSwitchAwayFromBook(this.getBaseContext(), SystemClock.elapsedRealtime());

                } else if (event.getClassName().toString().equals("com.mantano.android.reader.activities.AsyncReaderActivity")) {
                    Log.i(TAG, "Switch to reading");
                    BookReadingsRecorder.getBookReadingsRecorder(this.getBaseContext()).recordSwitchBackToCurrentBook(this.getBaseContext(), SystemClock.elapsedRealtime());

                }
            }

            if (!ONLY_SCROBBLE) {

                Log.d(TAG,"onAccessibilityEvent:eventText:"+eventText);
                Log.d(TAG,"onAccessibilityEvent:sourceViewId:"+sourceViewId);
                Log.d(TAG,"onAccessibilityEvent:sourceInfoText:"+sourceInfoText);


                try {

                   Log.i(TAG, String.format(
                            "onAccessibilityEvent: [windowId] %d [type] %s [class] %s [viewId] %s [package] %s [time] %d  [text] %s",
                            windowId,eventType,eventClassName,
                            sourceViewId,packageName,
                            eventTime,eventText)
                            +"[SourceText]:"+sourceInfoText+"|");

                    AccessibilityNodeInfo source = event.getSource();
                    if (source == null) {
                        Log.d(TAG,"Empty source!");
                    }
                    else
                    {
                        Log.d(TAG,"Source:"+source.toString());
                        //Log.d(TAG,"ContentDesc:"+event.getContentDescription());
                        /*
                        AccessibilityNodeInfo parent=source.getParent();
                        if (parent!=null) {
                            Log.d(TAG,"It's parent is "+parent.toString());
                        }
                        else {
                            Log.d(TAG,"it has null parent");
                        }

                        Log.d(TAG,"Before-text:"+event.getBeforeText());
                        AccessibilityNodeInfo label=source.getLabelFor();
                        if (label!=null) {
                            Log.d(TAG,"Label is "+label.toString());
                        }
                        else {
                            Log.d(TAG,"null label");
                        }
                        */
                        Log.d(TAG,"Source contains "+source.getChildCount()+ " elements");
                        for (int i=0;i<source.getChildCount();i++){
                            AccessibilityNodeInfo child=source.getChild(i);
                            if (child!=null) {
                                Log.d(TAG,"Child "+i+" contents:"+child.toString());
                            }
                            else {
                                Log.d(TAG,"Child "+i+" is null");
                            }
                        }
                    }



                } catch (MissingFormatArgumentException ex) {
                    Log.e(TAG,"Exception "+ex.toString());
                }

            }

            /*

            if (event.getEventType()==AccessibilityEvent.TYPE_VIEW_CLICKED) {

                if (sourceViewId!=null) {
                    if (eventClassName.equals("android.widget.RelativeLayout")) {
                        if (!ONLY_SCROBBLE) {
                            if (sourceViewId.equals("com.mantano.reader.android:id/book_bloc_item_list")) {
                                Log.d(TAG,"TYPE_VIEW_CLICKED in android.widget.RelativeLayout - sourceViewId is bloc_item_list");
                            }
                            else
                            {

                                Log.d(TAG,"TYPE_VIEW_CLICKED in android.widget.RelativeLayout - sourceViewId is NOT bloc_item_list");
                            }

                        }
                    }
                    if (sourceViewId.equals("com.mantano.reader.android:id/book_bloc_item_list")) {
                        //TODO:check class to be 'android.widget.RelativeLayout' ?
                        //
                        List<CharSequence> unprocessedData=event.getText();
                        //Log.d("regular processing, bookdata, "+unprocessedData);
                        Log.d(TAG,"Calling processMantanoBookData in regular way");
                        processMantanoBookData(unprocessedData);
                    }

                }


            } else if (event.getEventType()==AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                if (sourceViewId!=null) {
                    if (sourceViewId.equals("com.mantano.reader.android:id/bookreader_pagenumber_overlay")) {
                        if (!ONLY_SCROBBLE)
                        {
                            Log.d(TAG,"TYPE_WINDOW_CONTENT_CHANGED,sourceViewID:pagenumber overlay, classname is "+eventClassName);
                        }

                        Log.d(TAG,"Calling processMantanoPagenumbers in regular way");
                        processMantanoPagenumbers(sourceInfoText);

                        //processPageNumbersAndSendInfo(currentPageNumbers);
                    }
                }
            }  else if (event.getEventType()==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                if (event.getClassName().toString().equals("com.mantano.android.library.activities.LibraryActivity")) {
                   Log.i(TAG,"Switch to library");
                    BookReadingsRecorder.getBookReadingsRecorder(this.getBaseContext()).recordSwitchAwayFromBook(this.getBaseContext(),SystemClock.elapsedRealtime());

                }
                else if (event.getClassName().toString().equals("com.mantano.android.reader.activities.AsyncReaderActivity")) {
                   Log.i(TAG,"Switch to reading");
                    BookReadingsRecorder.getBookReadingsRecorder(this.getBaseContext()).recordSwitchBackToCurrentBook(this.getBaseContext(),SystemClock.elapsedRealtime());

                }
/*                if (sourceViewId!=null) {
                    if (sourceViewId.equals("com.mantano.reader.android:id/gridview")) {
                       Log.i(TAG,"Switch to library");
                        BookReadingsRecorder.getBookReadingsRecorder().recordSwitchAwayFromBook(this.getBaseContext(),SystemClock.elapsedRealtime());
                    }

                }  */

            //}
            /*

            [1029    ] 09:10:38.894 | ReadingTracker:accessibility_service | main | onAccessibilityEvent: [windowId] 12 [type] TYPE_WINDOW_STATE_CHANGED [class] com.mantano.android.library.activities.LibraryActivity [viewId] null [package] com.mantano.reader.android [time] 841686  [text] Mantano Reader Premium|[SourceText]:null|

             */

        }

    }

    private void processMantanoPagenumbers(String sourceInfoText) {
        currentPageNumbers=sourceInfoText;
        try {
            BookReadingsRecorder.getBookReadingsRecorder(this.getBaseContext()).recordPageSwitch(this.getBaseContext(), SystemClock.elapsedRealtime(),
                    currentPageNumbers);

        } catch (BookReadingsRecorder.InvalidArgumentsException e) {
            Log.e(TAG,"Exception "+e.toString());
        }
    }

    /*
      Processing of com.mantano.reader.android:id/book_bloc_item_list block - basic book data
     */
    private void processMantanoBookData(List<CharSequence> unprocessedData) {
       Log.i(TAG,"Size of book data:"+unprocessedData.size());
        for (int i=0;i<unprocessedData.size();i++)
        {
           Log.i(TAG,"Processing item "+i+". it's "+unprocessedData.get(i).toString());
        }
        try {

            currentBookTitle=unprocessedData.get(0).toString();
            currentBookAuthor=unprocessedData.get(1).toString();
            bookTags=unprocessedData.get(3).toString();
            String bookTagsAlt=unprocessedData.get(2).toString();
            currentBookKnown=true;
            String lastAccessUnprocessed=unprocessedData.get(2).toString();
            //if book was never opened size will be 4 and not 5
            //if we don't check we can crash Mantano
            //есть у меня подозрения что тут и так бред идет
            if (unprocessedData.size()==6) {
               Log.i(TAG,"Size of book data appears correct for arleady-opened book");
                currentPageNumbers = unprocessedData.get(4).toString();   //was 5
            }
            else {
               Log.i(TAG,"Size of book data is not 6 (it's "+unprocessedData.size()+"). Likely unread-before book");
                currentPageNumbers="1/0";//we need to report SOMETHING. TODO:make it possible to report something other if this becomes issue
                //если size()==4 то возможно что tags будет 'Не синхронизировано' а реальные теги в (2) а не (3)
                //в нормальной ситуации в (2) дата доступа
                //пока вот такой хак в качестве решения - хотя надо еще проверять и случай - не синхронизировано но при этом открыто более раза
                //TODO:и что делать с другими языками? нужно какое то более универсальное решение
                //еще бывает случай когда size()=5 (но все равно не синхронизировано но есть дата последнего доступа) и все нормально
                //вообще - надо ли это фиксить или пусть живет и само потом поправится?
                if (bookTags.equals("Не синхронизировано")) {
                   Log.i(TAG,"Fixing bookTags to "+bookTagsAlt);
                    bookTags=bookTagsAlt;
                }

            }
            //new book being opened
            currentTimestamp = SystemClock.elapsedRealtime();
            try {
                BookReadingsRecorder.getBookReadingsRecorder(this.getBaseContext()).recordNewBook(this.getBaseContext(),SystemClock.elapsedRealtime(),
                        currentBookAuthor,currentBookTitle,bookTags,currentPageNumbers);
            } catch (BookReadingsRecorder.InvalidArgumentsException e) {
                Log.e(TAG,"Exception "+e.toString());
            }

            //writeLastBookInfo(currentBookTitle, currentBookAuthor, bookTags);
            //processPageNumbersAndSendInfo(currentPageNumbers);

        } catch(IndexOutOfBoundsException e) {
            //possible logic violation
            Log.e(TAG,"Exception "+e.toString());
            Log.e(TAG,"unprocessed data was "+unprocessedData+"|. Details of event should be above");

            /*
            try {

               Log.i(TAG, String.format(
                        "onAccessibilityEvent: [windowId] %d [type] %s [class] %s [viewId] %s [package] %s [time] %d  [text] %s",
                        windowId,eventType,eventClassName,
                        sourceViewId,packageName,
                        eventTime,eventText)
                        +"[SourceText]:"+sourceInfoText+"|");

            } catch (MissingFormatArgumentException ex) {
                Log.e(TAG,"Exception "+ex.toString());
            } */

        }
    }

    @Override
    public void onInterrupt() {
       Log.i(TAG, "onInterrupt");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
       Log.i(TAG, "onServiceConnected");
        if (ONLY_SCROBBLE) {
           Log.i(TAG, " scrobble-only mode");
        }

        statusRequestReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
               Log.i(TAG, "Got request for status update");
                Intent i=new Intent(ACTIVITY_MONITORING_CONNECTED);
                LocalBroadcastManager.getInstance(context).sendBroadcast(i);

            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(
                statusRequestReceiver,new IntentFilter(ACTIVITY_MONITORING_STATUS_UPDATE_REQUEST)
        );

        /*
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        setServiceInfo(info);
        */

        Intent intent=new Intent(ACTIVITY_MONITORING_CONNECTED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"Book reading tracker:Accessibility Service is starting up, onCreate");

        CoreService.writeLogBanner(TAG, getApplicationContext());
        if (ONLY_SCROBBLE) {
           Log.i(TAG, "scrobble-only mode");
        }

        //do periodic config updates in case we need them, preliminary version
        new CountDownTimer(CoreService.YEAR_IN_MS, ParseConfigHelper.configRefreshInterval) {
            public void onTick(long msUntilFinish) {
                ParseConfigHelper.refreshConfig();

                if (ParseConfigHelper.isDevUser()) {
                    Log.d(TAG,"Current user is dev user");
                }
                else {
                    Log.d(TAG,"Current user is not dev user");
                }
            }

            public  void  onFinish() {}
        }.start();

    }

}
//helpers
//http://habrahabr.ru/post/234425/
//https://code.google.com/p/eyes-free/source/browse/trunk/braille/brailleback/res/xml/accessibilityservice.xml?spec=svn819&r=819

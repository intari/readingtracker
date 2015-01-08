package com.viorsan.readingtracker;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 09.01.15.
 * based on Spoon's screenshoter from https://github.com/square/spoon/blob/01cf2ccc75d34f114f857871eec7c9fd443c534d/spoon-client/src/main/java/com/squareup/spoon/Screenshot.java
 * Spoon's license apply
 */
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Environment;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;

import static android.graphics.Bitmap.CompressFormat.PNG;
import static android.graphics.Bitmap.Config.ARGB_8888;
import static com.viorsan.readingtracker.Chmod.chmodPlusR;

class Screenshot {
    public final static String TAG="ReadingTracker::Screenshot";

    static String getTimestamp() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(new Date());
    }
    static void capture(String name,Activity activity) throws IOException {
        name=name.replaceAll("/","");
        name=name.replaceAll(":","");
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).mkdirs();
        File file=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath(),name+"_"+getTimestamp());
        //capture
        capture(file,activity);
    }
    static void capture(File file, Activity activity) throws IOException {
        captureDecorView(file, activity);
    }


    private static void captureDecorView(File file, final Activity activity) throws IOException {
        DisplayMetrics dm = activity.getResources().getDisplayMetrics();
        final Bitmap bitmap = Bitmap.createBitmap(dm.widthPixels, dm.heightPixels, ARGB_8888);

        if (Looper.myLooper() == Looper.getMainLooper()) {
            // On main thread already, Just Do It™.
            drawDecorViewToBitmap(activity, bitmap);
        } else {
            // On a background thread, post to main.
            final CountDownLatch latch = new CountDownLatch(1);
            activity.runOnUiThread(new Runnable() {
                @Override public void run() {
                    try {
                        drawDecorViewToBitmap(activity, bitmap);
                    } finally {
                        latch.countDown();
                    }
                }
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                String msg = "Unable to get screenshot " + file.getAbsolutePath();
                Log.e(TAG, msg, e);
                throw new RuntimeException(msg, e);
            }
        }

        writeBitmapToFile(bitmap, file);
    }

    private static void writeBitmapToFile(Bitmap bitmap, File file) throws IOException {
        OutputStream fos = null;
        try {
            fos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(PNG, 100 /* quality */, fos);

            chmodPlusR(file);
        } finally {
            bitmap.recycle();
            if (fos != null) {
                fos.close();
            }
        }
    }

    private static void drawDecorViewToBitmap(Activity activity, Bitmap bitmap) {
        Canvas canvas = new Canvas(bitmap);
        activity.getWindow().getDecorView().draw(canvas);
    }
}
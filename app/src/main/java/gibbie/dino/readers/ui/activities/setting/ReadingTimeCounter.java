package gibbie.dino.readers.ui.activities.setting;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import gibbie.dino.readers.R;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.ui.activities.bottomnav.BottomNavigation;
import gibbie.dino.readers.ui.activities.ownstory.ReadOwnStory;

public class ReadingTimeCounter {
    static private Handler handlerReading = new Handler();
    static private Runnable runnableReading;
    static private int delay = 1000; // 1 second
    static private int elapsedTime;
    static private int readingTime;
    static private int tempReadingTime;

    static private SessionManager sessionManager;
    static private Context mCtx;
    private static Activity mActivity;
    static boolean isShowDialogue = false;

    public static void setActivityReference(Activity activity) {
        mActivity = activity;
    }

    public static void setContextReference(Context context){
        mCtx = context;
    }

    public static void setSession(Context context){
        Activity activity = (Activity) context;
        setActivityReference(activity);
        setContextReference(context);
        sessionManager = new SessionManager(mCtx);
    }

    public static void startTimer(Context context) {
        setSession(context);
//        if(!sessionManager.getActivateReadingTime())
//            return;

        tempReadingTime = 0;
        isShowDialogue = false;
        elapsedTime = sessionManager.getCurrentReadingTime();
        readingTime = sessionManager.getReadingTime();

        runnableReading = new Runnable() {
            @Override
            public void run() {
                elapsedTime++;
                tempReadingTime++;
                checkTimer();
                handlerReading.postDelayed(this, delay);
            }
        };
        handlerReading.post(runnableReading);
    }

    public static void stopTimer() {
        sessionManager.setCurrentReadingTime(elapsedTime);
        sessionManager.setTempReadingTime(tempReadingTime);
        handlerReading.removeCallbacksAndMessages(null);
    }

    private static void checkTimer() {
        if(elapsedTime < readingTime){
//            Log.e("TAG", "Current Reading Time: " + elapsedTime);
        }
        else {
            stopTimer();
            if(!isShowDialogue)
                showReadingTimeOutAlert(mCtx);
        }
    }

    public static void showReadingTimeOutAlert(Context context) {
//        String msg = sessionManager.isCalculatedByEachLogins() ?
//                "Please take a break for rest and come back later." :
//                "Your reading time is over for today.";
        String msg = "Your reading time is up!";
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialogTheme);
        builder.setTitle("")
                .setMessage(msg)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setLastActiveTime();
                        if (mActivity != null) {
                            if (mActivity instanceof ReadOwnStory) {
                                mActivity.finish();
                            }
                            else{
                                BottomNavigation.fm.popBackStack();
                            }
                        }
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
        isShowDialogue = true;
    }

    public static void showReadingDayOutAlert(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialogTheme);
        builder.setTitle("No Reading Today!")
                .setMessage("You cannot read the book today.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
        isShowDialogue = true;
    }

    private static void setLastActiveTime(){
        Calendar calendar = Calendar.getInstance();
        String timeString = getDateTime(calendar);
        sessionManager.setLastTimeReading(timeString);
    }

    public static String getDateTime(Calendar calendar){
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        Date date = calendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = dateFormat.format(date);

        return formattedDate + " " + hour + ":" + minute + ":" + second;
    }

}
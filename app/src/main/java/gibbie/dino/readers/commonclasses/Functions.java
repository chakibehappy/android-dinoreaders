package gibbie.dino.readers.commonclasses;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.content.CursorLoader;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import gibbie.dino.readers.interfaces.NoInternet;
import gibbie.dino.readers.interfaces.PermissionListner;

public class Functions {
    public static MyProgressBar myProgressBar;
    public static final int Status = 99;

    public static void enableFullscreen(@NonNull Activity activity, @NonNull Boolean isLandscape) {
        View decorView = activity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE
        );

        if(isLandscape)
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    public static void showSettingsDialog(final Activity context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Need Permissions");
        builder.setCancelable(false);
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        showsettingdialog(context);


                    }
                }
        );

        builder.show();

    }

    public static void showErrorDialog(String msg, String action, Activity context, boolean isFinish) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);


        builder.setTitle("Dino Readers");
        builder.setCancelable(false);
        builder.setMessage(msg);
        builder.setPositiveButton(action, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isFinish) {
                    context.onBackPressed();
                    dialog.cancel();
                } else {
                    dialog.cancel();
                }


            }
        });
        AlertDialog dialog = builder.create();

        dialog.show();


    }


    public static boolean IsValidEmail(EditText editText) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if (TextUtils.isEmpty(editText.getText().toString().trim())) {
            editText.setError("Required");
            return false;
        } else if (!editText.getText().toString().trim().matches(emailPattern)) {
            editText.setError("Not a valid Email Adress");
            return false;
        }
        return true;
    }


    public static void showProgressbar(Activity activity) {
        myProgressBar = new MyProgressBar(activity);
        myProgressBar.show("Loading.............");
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void hideProgressbar(Activity activity) {
        if (activity != null) {
            if (myProgressBar != null) {
                myProgressBar.dismiss();
            }
        }
    }


    public static boolean checkFileSize(File file, Context context) {
        long fileSizeInBytes = file.length();
// Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
        long fileSizeInKB = fileSizeInBytes / 1024;
// Convert the KB to MegaBytes (1 MB = 1024 KBytes)
        long fileSizeInMB = fileSizeInKB / 1024;

        if (fileSizeInMB > 10) {
            return false;
        }
        return true;
    }


    public static String getRealPathFromURI(Activity activity, Uri tempUri) {
        String[] data = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(activity, tempUri, data, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);

    }

    public static Uri getImageUri(Activity applicationContext, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(applicationContext.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }


    private static void showsettingdialog(Activity context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivityForResult(intent, 6666);
    }

    public static void NoInternetcConnectionDialog(final Context context, final NoInternet noInternet) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("No Intenet Found");
            builder.setCancelable(false);
            builder.setMessage("Please Check Your Internet Connection");
            builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                    noInternet.Retry();
                }
            });
            builder.show();
        } catch (Exception e) {
        }
    }

    public static void animateViewDown(TextView textView) {
        Drawable[] myTextViewCompoundDrawables = textView.getCompoundDrawables();
        for (Drawable drawable : myTextViewCompoundDrawables) {

            if (drawable == null)
                continue;

            ObjectAnimator anim = ObjectAnimator.ofInt(drawable, "level", 10000, 0);
            anim.start();

        }
    }

    public static void animateViewUp(TextView textView) {
        Drawable[] myTextViewCompoundDrawables = textView.getCompoundDrawables();
        for (Drawable drawable : myTextViewCompoundDrawables) {

            if (drawable == null)
                continue;

            ObjectAnimator anim = ObjectAnimator.ofInt(drawable, "level", 0, 10000);
            anim.start();
        }
    }


    public static void NoInternetDialog(final Context context, final NoInternet noInternet) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("No Intenet Found");
            builder.setCancelable(false);
            builder.setMessage("Please Check Your Internet Connection");
          /*  builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });*/
            builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                    noInternet.Retry();
                }
            });
            builder.show();
        } catch (Exception e) {

        }

    }


    public static void SomethingWrongDialog(final Context context, final NoInternet noInternet) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Something Went Wrong!!!");
            builder.setCancelable(false);
            builder.setMessage("Can't Connect. Please try Again");
          /*  builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });*/
            builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                    noInternet.Retry();
                }
            });
            builder.show();
        } catch (Exception e) {

        }

    }


    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }


    public static String changeDateFormat(String date) {
        String formattedDate = "";
        SimpleDateFormat objSimpleDateFormatFROM = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat objSimpleDateFormatTO = new SimpleDateFormat("dd MMM yyyy");
        try {
            Date objDateFrom = objSimpleDateFormatFROM.parse(date);
            formattedDate = objSimpleDateFormatTO.format(objDateFrom);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formattedDate;
    }

    public static Date stringToDate(String dateString, SimpleDateFormat dateFormat) {


        Date date = new Date();
        try {
            date = dateFormat.parse(dateString);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return date;
    }

    public static void checkMultiplePermisionsInFragment(FragmentActivity appCompatActivity, String permsions, String permsion, final PermissionListner permisionListener) {
        Dexter.withActivity(appCompatActivity).withPermissions(permsions, permsion).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (report.areAllPermissionsGranted()) {
                    permisionListener.OnPermissionGranted();
                } else if (report.isAnyPermissionPermanentlyDenied()) {
                    permisionListener.OnPermsionDenied();
                } else {
                    permisionListener.OnPermsionDenied();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();
    }

    public static boolean checkInternet(Context context) {
        boolean flag = false;
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();

            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                flag = true;
            }
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                flag = true;
            }
        } catch (Exception exception) {


        }


          /*  try {
                InetAddress ipAddr = InetAddress.getByName(BASEURL_INTERNET_CHECK);
                //You can replace it with your name
                return !ipAddr.equals("");

            } catch (Exception e) {
                return false;
            }
           */

        return flag;
    }
    public static void checkSinglePermision(Activity context, String Permisions, final PermissionListner permisionListener) {
        if (Build.VERSION.SDK_INT >= 21) {
            Dexter.withActivity(context)
                    .withPermission(Permisions)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {

                            permisionListener.OnPermissionGranted();
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {

                            if (response.isPermanentlyDenied()) {
                                permisionListener.OnPermsionDenied();
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    }).check();
        } else {

        }


    }

    public static String changeFirstLetterTOCapital(String myString) {
        String upperString = myString.substring(0, 1).toUpperCase() + myString.substring(1);
        return upperString;

    }

    public static Spanned sethtmlData(String name) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return (Html.fromHtml(name, Html.FROM_HTML_MODE_COMPACT));


        } else {
            return (Html.fromHtml(name));
        }

    }


    public static void unBingListener(View view) {
        if (view != null) {
            try {
                if (view.hasOnClickListeners()) {
                    view.setOnClickListener(null);

                }

                if (view.getOnFocusChangeListener() != null) {
                    view.setOnFocusChangeListener(null);

                }

                if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
                    ViewGroup viewGroup = (ViewGroup) view;
                    int viewGroupChildCount = viewGroup.getChildCount();
                    for (int i = 0; i < viewGroupChildCount; i++) {
                        unBingListener(viewGroup.getChildAt(i));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static int lastIndexOfBottomMenu(Menu list) {
        if (list.size() == 0) {
            return 0;
        }
        return list.size() - 1;
    }

    public static String timeConvert24To12(String _24HourTime) {
        String result = _24HourTime;
        try {

            SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat _12HourSDF = new SimpleDateFormat("hh:mm:ss a");
            Date _24HourDt = _24HourSDF.parse(_24HourTime);

            result = (_12HourSDF.format(_24HourDt));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String timeConvert24To24(String _24HourTime) {
        String result = _24HourTime;
        try {

            SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm");
            SimpleDateFormat _12HourSDF = new SimpleDateFormat("HH:mm");
            Date _24HourDt = _24HourSDF.parse(_24HourTime);

            result = (_12HourSDF.format(_24HourDt));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void setText(TextView textView, String text) {
        if (textView == null) {
            return;
        }
        if (text == null) {
            textView.setText("");
        } else if (text.equalsIgnoreCase("null")) {
            textView.setText("");
        } else {
            textView.setText(text);
        }

    }

    public static void setText(EditText textView, String text) {
        if (textView == null) {
            return;
        }
        if (text == null) {
            textView.setText("");
        } else if (text.equalsIgnoreCase("null")) {
            textView.setText("");
        } else {
            textView.setText(text);
        }

    }


    public static List<String> getRunningApps(Context context, boolean includeSystem) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        HashSet<String> runningApps = new HashSet<>();
        try {
            List<ActivityManager.RunningAppProcessInfo> runAppsList = activityManager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runAppsList) {
                runningApps.addAll(Arrays.asList(processInfo.pkgList));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            //can throw securityException at api<18 (maybe need "android.permission.GET_TASKS")
            List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(1000);
            for (ActivityManager.RunningTaskInfo taskInfo : runningTasks) {
                runningApps.add(taskInfo.topActivity.getPackageName());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(1000);
            for (ActivityManager.RunningServiceInfo serviceInfo : runningServices) {
                runningApps.add(serviceInfo.service.getPackageName());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ArrayList<>(runningApps);
    }

    public static boolean isSystemPackage(Context context, String app) {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo pkgInfo = packageManager.getPackageInfo(app, 0);
            return (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean hasAppPermission(Context context, String app, String permission) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        try {
            packageInfo = packageManager.getPackageInfo(app, PackageManager.GET_PERMISSIONS);
            if (packageInfo.requestedPermissions != null) {
                for (String requestedPermission : packageInfo.requestedPermissions) {
                    if (requestedPermission.equals(permission)) {
                        return true;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static AppInfoModel getApplicationName(Context context, String packageName) {

        PackageManager packageManager = context.getPackageManager();
        AppInfoModel appInfoModel = new AppInfoModel();
        appInfoModel.appName = packageName;
        appInfoModel.packageName = packageName;
        try {
            appInfoModel.appName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)).toString();
            appInfoModel.appIcon = packageManager.getApplicationIcon(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appInfoModel;
    }


}

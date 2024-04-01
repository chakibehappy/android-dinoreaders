package gibbie.dino.readers.retrofitsetup;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;

import androidx.appcompat.app.AlertDialog;

import java.io.IOException;

import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.ui.activities.login.LoginActivity;

public class CommonResponse
{

    public static boolean isUnauthorized(Activity activity, int code)
    {

        if (code == 401)
        {
            SessionManager sessionManager = new SessionManager(activity);
            sessionManager.logOut();

            AlertDialog alertDialog = new AlertDialog.Builder(activity)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Dino Readers")
                    .setMessage(" Unauthorized Success" + "("+code+")")
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            sessionManager.logOut();
                            activity.startActivity(new Intent(activity, LoginActivity.class));
                            activity.finishAffinity();
                            dialogInterface.dismiss();
                        }

                    })
                    .show();
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);

            return true;
        }
        else if (code == 503)
        {

            SessionManager sessionManager = new SessionManager(activity);

            AlertDialog alertDialog = new AlertDialog.Builder(activity)
                    // .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Dino Readers")
                    .setMessage("Application is now in maintenance mode.")
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(activity!=null)
                            {
                                if (!(activity instanceof LoginActivity))
                                {
                                    sessionManager.logOut();
                                    activity.startActivity(new Intent(activity, LoginActivity.class));
                                    activity.finishAffinity();
                                }
                            }
                            dialogInterface.dismiss();
                        }
                    })
                    .show();
            return true;

        } else {
            return false;
        }


    }

    public static String getErrorMSG(Throwable t) {
        if (t instanceof IOException) {
            return "can't connect server";
// logging probably not necessary
        } else {
            return t.getMessage();

        }
    }
}

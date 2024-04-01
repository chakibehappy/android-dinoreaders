package gibbie.dino.readers.commonclasses;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Window;

import gibbie.dino.readers.R;


public class MyProgressBar
{
    Dialog dialog;
    Activity activity;

    public MyProgressBar(Activity activity) {
        this.activity = activity;
    }

    public void show(String msg) {
        if (activity == null) {
            return;
        }

            dialog = new Dialog(activity);

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.my_progress_bar);
//            if (msg.equals("")) {
//// msg="Loading...";
//                ((TextView) dialog.findViewById(R.id.mpb_msg)).setVisibility(View.GONE);
//            }
//            ((TextView) dialog.findViewById(R.id.mpb_msg)).setText(msg);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (!activity.isDestroyed()) {
                dialog.show();
            }
        }


    }

    public void dismiss() {
        if (activity != null) {
            if (!activity.isDestroyed()) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }

        }

    }


}


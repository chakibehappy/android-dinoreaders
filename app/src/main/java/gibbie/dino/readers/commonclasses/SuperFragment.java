package gibbie.dino.readers.commonclasses;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.whiteelephant.monthpicker.MonthPickerDialog;

import java.util.Calendar;

import gibbie.dino.readers.R;
import gibbie.dino.readers.database.SessionManager;


public abstract class SuperFragment extends Fragment {
    String emailpattren = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    public static boolean checkAllEditext(EditText editText, EditText et1, EditText et2, EditText et3, EditText et4) {
        if (editText.getText().toString().trim().length() == 0 && et1.getText().toString().trim().length() == 0 && et2.getText().toString().trim().length() == 0 && et3.getText().toString().trim().length() == 0 && et4.getText().toString().trim().length() == 0) {
            editText.setError("Required");
            editText.requestFocus();
            et1.setError("Required");

            et2.setError("Required");

            et3.setError("Required");

            et4.setError("Required");

            return false;

        }
        return true;


    }

    public static boolean isValidMobileNo(EditText editText) {
        if (TextUtils.isEmpty(editText.getText().toString().trim())) {
            editText.setError("Required");
            editText.requestFocus();
            return false;
        } else if (editText.getText().toString().trim().length() < 8) {
            editText.setError("Not a Valid Mobile Number");
            editText.requestFocus();
            return false;
        }
        return true;
    }

    public static boolean CheckEditext(EditText editText) {
        if (TextUtils.isEmpty(editText.getText().toString().trim())) {
            editText.setError("Required");
            editText.requestFocus();
            return false;
        }
        return true;
    }

    public static boolean CheckTextView(TextView textView) {
        if (TextUtils.isEmpty(textView.getText().toString().trim())) {
            textView.setError("Required");
            textView.requestFocus();
            return false;
        }
        return true;
    }

    public static boolean isPasswordSame(EditText editText, EditText editText1) {
        if (!editText.getText().toString().trim().equals(editText1.getText().toString().trim())) {

            editText1.setError("Not Matched");
            editText1.requestFocus();
            return false;
        } else {
            editText.clearFocus();
            editText1.clearFocus();

        }
        return true;

    }

    public static boolean isPasswordGooD(EditText editText) {
        if (editText.getText().toString().trim().length() < 8) {
            editText.setError("Password Length must be greater then or equals eight");
            return false;
        }
        return true;

    }

    public void MakeToast(String msg) {
        try {
            Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();

        } catch (NullPointerException e) {

        }
    }

    public void toolBarClick(final Context context, Toolbar toolbar, final String checkstatus) {

        ImageView imageView = toolbar.findViewById(R.id.iv_back);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkstatus.equalsIgnoreCase("")) {

                    int fragments = getChildFragmentManager().getBackStackEntryCount();
                    if (fragments == 1) {
                        getActivity().finish();
                    } else if (getFragmentManager().getBackStackEntryCount() > 1) {
                        getFragmentManager().popBackStack();
                    } else {
                        getActivity().onBackPressed();
                    }
                } else {

                }

            }
        });


    }

    public void startNewActivity(Activity a, Class<? extends Activity> class1, boolean isfinis, Intent intent) {

        try {
            Bundle bundle = null;
            if (intent != null) {
                bundle = intent.getExtras();

            }
            Intent i = new Intent(a, class1);
            if (bundle != null) {
                i.putExtras(bundle);

            }
            a.startActivity(i);
            getActivity().overridePendingTransition(R.anim.left_in, R.anim.right_out);


            if (isfinis) {
                a.finish();


            }


        } catch (ActivityNotFoundException e) {
            MakeToast("Something Went Wrong Please Restart the application!!!!!!!!");
        } catch (Exception e) {
            MakeToast("Something Went Wrong!!!!!!!!");
        }


    }

    public void showToastOnMainThread(final String msg) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {

        }
    }

    public boolean checkInternet() {
        try {
            return Functions.checkInternet(getActivity());
        } catch (NullPointerException e) {
            return false;

        }

    }

    public void showSnackbar(View view, String text, final String from) {
        try {
            final Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG);
            View sbView = snackbar.getView();
            TextView snackbarActionTextView = (TextView) snackbar.getView().findViewById(R.id.snackbar_action);
            snackbarActionTextView.setTextSize(18);
            snackbarActionTextView.setTypeface(snackbarActionTextView.getTypeface(), Typeface.BOLD);
            snackbarActionTextView.setTextColor(getResources().getColor(R.color.colorPrimary));

            TextView textView = (TextView) sbView.findViewById(

                    R.id.snackbar_text);
            textView.setTextColor(Color.WHITE);
            textView.setTextSize(15);
            textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
            sbView.setBackgroundColor(getResources().getColor(R.color.colorAccent));

            snackbar.setActionTextColor(getResources().getColor(R.color.colorAccent));
            snackbar.setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (from.equalsIgnoreCase("Reset")) {

                    }
                    snackbar.dismiss();
                }
            });
            snackbar.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showSnackbarLong(View view, String text, final String from) {
        try {
            final Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE);
            View sbView = snackbar.getView();
            TextView snackbarActionTextView = (TextView) snackbar.getView().findViewById(R.id.snackbar_action);
            snackbarActionTextView.setTextSize(18);
            snackbarActionTextView.setTypeface(snackbarActionTextView.getTypeface(), Typeface.BOLD);
            snackbarActionTextView.setTextColor(getResources().getColor(R.color.colorPrimary));

            TextView textView = (TextView) sbView.findViewById(

                    R.id.snackbar_text);
            textView.setTextColor(Color.WHITE);
            textView.setTextSize(15);
            textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
            sbView.setBackgroundColor(getResources().getColor(R.color.colorAccent));

            snackbar.setActionTextColor(getResources().getColor(R.color.white));
            snackbar.setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (from.equalsIgnoreCase("Reset")) {

                    }
                    snackbar.dismiss();
                }
            });
            snackbar.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean checkStringISNull(String check) {
        if (check == null) {
            return false;
        } else {
            return true;
        }
    }

    public void showDialog(String msg) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme);
        builder.setTitle("Affinite");
        builder.setCancelable(false);
        builder.setMessage(msg);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();


                    }
                }
        );


        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();


    }

    public boolean checkStringNullEmpty(String check) {
        if (check == null || check.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isEMailOk(EditText email) {
        if (!email.getText().toString().trim().matches(emailpattren)) {
            email.setError("Enter valid email");
            email.requestFocus();
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void showErrorDialog(Activity activity, String message) {
        SessionManager sessionManager = new SessionManager(activity);

        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                // .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Affinite")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (activity != null) {
//                            sessionManager.logOut();
//                            activity.startActivity(new Intent(activity, LoginActivity.class));
//                            activity.finishAffinity();
                        }
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }
    public TextView MakeCategory(Activity activity,String text,int tintColor,boolean isCarousel){
        LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        TextView tv_category = new TextView(activity);
        tv_category.setText(text.toUpperCase());
        tv_category.setBackgroundResource(R.drawable.rounded_green_rectangle);
        tv_category.setBackgroundTintList(ColorStateList.valueOf(tintColor));
        tv_category.setTextColor(activity.getResources().getColor(R.color.white));
        if(isCarousel)
            tv_category.setTextSize(10);
        else
            tv_category.setTextSize(15);
        tv_category.setMaxLines(1);
        lparams.leftMargin = 10;
        lparams.topMargin = 10;
        tv_category.setPadding((int) activity.getResources().getDimension(R.dimen._15sdp),0,(int) activity.getResources().getDimension(R.dimen._15sdp),0);
        tv_category.setLayoutParams(lparams);
        return tv_category;
    }
    public void MonthYearPicker(View v,TextView setText){
        final Calendar today = Calendar.getInstance();
        MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(getContext(),
                new MonthPickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(int selectedMonth, int selectedYear) { // on date set }
                        setText.setText((selectedMonth+1)+"-"+String.valueOf(selectedYear));
                    }
                }, today.get(Calendar.YEAR), today.get(Calendar.MONTH));

        builder.setActivatedMonth(Calendar.JULY)
                .setMinYear(1900)
                .setActivatedYear(2021)
                .setMaxYear(2030)
                .setMinMonth(Calendar.JANUARY)
                .setTitle("Select Date of Birth")
                .build().show();

    }
}

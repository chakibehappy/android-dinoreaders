package gibbie.dino.readers.commonclasses;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import gibbie.dino.readers.R;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;

public abstract class SuperActivity extends AppCompatActivity {
    String emailpattren = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    String backsttaus = "";
    File filepaths;
    private Toolbar mTopToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(MyView());
    }

    public void changeStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(getResources().getColor(color, null));
            getWindow().setStatusBarColor(getResources().getColor(color, null));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(color));
        }

    }

    public void showDialog(String msg) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle("Dino Readers");
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

    public void hideShowToolbar(boolean value) {
        try {
            if (value) {
                getSupportActionBar().hide();
            } else {


            }

        } catch (Exception e) {

        }

    }


    public void ShowKeyboard(EditText editText) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    public void HideKeyboard(RelativeLayout relativeLayout) {
        if (relativeLayout != null) {

        }
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(relativeLayout.getWindowToken(), 0);
    }


    public void ToolbarClick(Toolbar toolbar, final String checkstatus) {
        backsttaus = checkstatus;
        ImageView imageView = toolbar.findViewById(R.id.iv_back);
        imageView.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View view) {
                                             if (checkstatus.equalsIgnoreCase("")) {
                                                 onBackPressed();
                                             } else {

                                             }

                                         }
                                     }
        );


    }


    public void changeStatusBarColor() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            getWindow().setStatusBarColor(getResources().getColor(R.color.white, this.getTheme()));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.white));
        }
    }


    protected abstract int MyView();

    public void MakeToast(String msg) {
        try {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        } catch (NullPointerException e) {

        }
    }

    public boolean checkInternet() {
        try {
            return Functions.checkInternet(getApplicationContext());

        } catch (NullPointerException e) {
            return false;

        }

    }

    public void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, 2);


    }

    public void openCamera() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        startActivityForResult(intent, 1);

    }

    public void showSnackbar(View view, String text, final String from) {
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

        snackbar.show();
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
            overridePendingTransition(R.anim.left_in, R.anim.right_out);


            if (isfinis) {
                a.finish();


            }


        } catch (ActivityNotFoundException e) {
            MakeToast("Something Went Wrong Please Restart the application!!!!!!!!");
        } catch (Exception e) {
            MakeToast("Something Went Wrong!!!!!!!!");
        }


    }


    @Override
    public void onBackPressed() {
        if (backsttaus.equalsIgnoreCase("")) {
            super.onBackPressed();
            overridePendingTransition(R.anim.right_in, R.anim.left_out);

        } else {

        }


    }

    public boolean checkStringISNull(String check) {
        if (!TextUtils.isEmpty(check)) {
            return true;
        } else {
            return false;
        }
    }


    public boolean checkStringISNullEmpty(String check) {
        if (check != null) {
            if (!check.isEmpty()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
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


    public void makeTextUnderline(TextView textView) {
        textView.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

    }


    public static boolean cHECKALLEDITEXT(EditText editText, EditText et1, EditText et2, EditText et3, EditText et4) {
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

    public boolean checkLoginFields(EditText editText, EditText editText1) {
        if (editText.getText().toString().trim().length() == 0 && editText1.getText().toString().trim().length() == 0) {
            editText.setError("Required");
            editText1.setError("Required");
            return false;
        }
        return true;
    }


    public boolean checkUsernameField(EditText editText) {
        if (editText.getText().toString().trim().length() == 0) {
            editText.setError("Required");
            return false;
        }
        return true;
    }


    public void showhidePasswordFunctionality(TextView PasswordVisble, EditText password) {
        if (PasswordVisble.getText().toString().equals("SHOW")) {
            if (TextUtils.isEmpty(password.getText().toString().trim())) {

            } else {
                PasswordVisble.setText("HIDE");
                password.setTransformationMethod(null);
            }
        } else {
            PasswordVisble.setText("SHOW");
            password.setTransformationMethod(new PasswordTransformationMethod());
        }


    }

    public static boolean isValidMobileNo(EditText editText) {
        if (TextUtils.isEmpty(editText.getText().toString().trim())) {
            editText.setError("Required");
            editText.requestFocus();
            return false;
        } else if (editText.getText().toString().trim().length() != 10) {
            editText.setError("Not a Valid Mobile Number");
            editText.requestFocus();
            return false;
        }
        return true;
    }

    public static boolean checkEditext(EditText editText) {
        if (TextUtils.isEmpty(editText.getText().toString().trim())) {
            editText.setError("Required");
            editText.requestFocus();
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

    public void printLog(Context context, String tag, String msg) {
        Log.e(context.getClass().getSimpleName(), tag + "+++++++" + msg);
    }
}

package gibbie.dino.readers.ui.fragments.profile;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import gibbie.dino.readers.R;
import gibbie.dino.readers.commonclasses.Functions;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.interfaces.NoInternet;
import gibbie.dino.readers.interfaces.PermissionListner;

public class ProfilePictureActivity extends AppCompatActivity implements NoInternet {
    private ScaleGestureDetector scaleGestureDetector;
    private float mScaleFactor = 1.0f;
    private ImageView imageView, iv_dot, iv_back;
    private SessionManager sessionManager;
    private Uri mCropImageUri;
    private RelativeLayout rl;
    File imagefile;

    private Context mContext;
    private Activity mActivity;

    private ProgressDialog mProgressDialog;
    private ImageView mImageView;
    private ImageView mImageViewInternal;
    private long lastDownload = -1L;

    private DownloadManager mgr = null;
    String filename = "";
    TextView toolbar_txt_bt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_picture);
        changeStatusBarColor(R.color.bg_color1);
        init();
        //initDownloadmanager();
        iv_dot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOptionDialog();
            }
        }); // MENU CLICK
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }); // BACK PRESS CLICK
        sessionManager = new SessionManager(this);
        getUploadImageLocal(); // SET PROFILE PICTURE
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
    }

    // Initialisation of objects
    private void init() {
        imageView = findViewById(R.id.imageView);
        iv_back = findViewById(R.id.iv_back);
        iv_dot = findViewById(R.id.iv_dot);
        rl = findViewById(R.id.rl);
        toolbar_txt_bt = findViewById(R.id.txt_bt);
    }
    // SET PROFILE PICTURE
    private void getUploadImageLocal(){
        if(sessionManager.getCreateProfilePicturePath() != ""){
            if(sessionManager.getCreateProfilePicturePath().contains("cache")) {
                File imgFile = new  File(sessionManager.getCreateProfilePicturePath());
                Picasso.get().load(imgFile).placeholder(R.drawable.profile).error(R.drawable.profile).into(imageView);
            }else {
                Picasso.get().load(sessionManager.getCreateProfilePicturePath()).placeholder(R.drawable.profile).error(R.drawable.profile).into(imageView);
            }
        }
    }
    //SHOW OPTIONS DIALOG
    private void showOptionDialog() {
        String[] colors = { getString(R.string.update_photo)};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.choose_action);
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
//                    case (0): {
//                        checkHasPermisionorNot();
//                        break;
//                    }
                    case (0): {
                        checkPermsionFirst(imageView);
                        break;
                    }
                }
            }
        });
        builder.show();
    }

    // FOR ZOOM
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        scaleGestureDetector.onTouchEvent(motionEvent);
        return true;
    }

    // CHECK STORAGE PERMISSIONS
    private void checkPermsionFirst(View view) {
        Functions.checkMultiplePermisionsInFragment(this, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, new PermissionListner() {
            @Override
            public void OnPermissionGranted() {
                onSelectImageClick(view);
            }

            @Override
            public void OnPermsionDenied() {

            }
        });
    }

    // CHANGE STATUS BAR COLOR
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

    @Override
    public void Retry() {
       // uploadProfileImage(imagefile);

    }

    // SET ZOOM CONTROLS
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));
            imageView.setScaleX(mScaleFactor);
            imageView.setScaleY(mScaleFactor);
            return true;
        }
    }

    //  PROFILE IMAGE CLICK
    public void onSelectImageClick(View view) {
        CropImage.startPickImageActivity(this);
    }

    // CROP OF IMAGE RESULT

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                mCropImageUri = imageUri;
            } else {
                startCropImageActivity(imageUri);
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                getFilePath(result.getUri());
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                showSnackbar(rl, getResources().getString(R.string.valid_image), "");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // required permissions granted, start crop image activity
            startCropImageActivity(mCropImageUri);
        }
    }

    // CROP OF IMAGE
    private void startCropImageActivity(Uri imageUri) {

        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setAspectRatio(1, 1)
                .start(this);
    }

    // GET PATH OF IMAGE FILE
    public void getFilePath(Uri uri) {
        if (uri != null) {
            String path = uri.getPath();
            imagefile = new File(path);
            imagefile = compressImage(imagefile);
           // Log.e("PATH",imagefile.getPath());
            sessionManager.setCreateFilePicturePath(imagefile.getPath());
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
           // uploadProfileImage(imagefile);
        }
    }

    // SHOW RESPONSE MESSAGE
    public void showSnackbar(View view, String text, final String from) {
        final Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        TextView snackbarActionTextView = (TextView) snackbar.getView().findViewById(R.id.snackbar_action);
        snackbarActionTextView.setTextSize(18);
        snackbarActionTextView.setTypeface(snackbarActionTextView.getTypeface(), Typeface.BOLD);
        snackbarActionTextView.setTextColor(getResources().getColor(R.color.colorPrimary));

        TextView textView = (TextView) sbView.findViewById(R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(15);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        sbView.setBackgroundColor(getResources().getColor(R.color.colorAccent));

        snackbar.setActionTextColor(getResources().getColor(R.color.colorAccent));

        snackbar.show();
    }

    // COMPRESSION OF IMAGE
    public File compressImage(File file) {
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 6;
            FileInputStream inputStream = new FileInputStream(file);
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();
            final int REQUIRED_SIZE = 75;
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(file);
            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);
            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            return file;
        } catch (Exception e) {
            return null;
        }
    }
}

package gibbie.dino.readers.retrofitsetup;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import com.jakewharton.threetenabp.AndroidThreeTen;
import java.io.File;

public class DinoReadersApplication extends Application
{
    private static DinoReadersApplication currentApplication = null;


    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);
        currentApplication = this;
    }

    @Override
    protected void attachBaseContext(Context base)
    {
        super.attachBaseContext(base);
    }

    // region Helper Methods
    public static DinoReadersApplication getInstance() {
        return currentApplication;
    }

    public static File getCacheDirectory() {
        return currentApplication.getCacheDir();
    }

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }




}
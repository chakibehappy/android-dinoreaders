package gibbie.dino.readers.ui.activities.placementtest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import gibbie.dino.readers.R;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.ui.activities.bottomnav.BottomNavigation;

public class PlacementTestActivity extends AppCompatActivity {

    LinearLayout vw_pre_reading, vw_early_emergent_reader, vw_emergent_reader, vw_early_fluent_reader,
            vw_fluent_reader, vw_advanced_fluent_reader, vw_proficient_reader, vw_independent_reader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placement_test);
        init();
        setButtonClick();
    }

    private void init(){
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE
        );

        vw_pre_reading = findViewById(R.id.vw_pre_reading);
        vw_early_emergent_reader = findViewById(R.id.vw_early_emergent_reader);
        vw_emergent_reader = findViewById(R.id.vw_emergent_reader);
        vw_early_fluent_reader = findViewById(R.id.vw_early_fluent_reader);
        vw_fluent_reader = findViewById(R.id.vw_fluent_reader);
        vw_advanced_fluent_reader = findViewById(R.id.vw_advanced_fluent_reader);
        vw_proficient_reader = findViewById(R.id.vw_proficient_reader);
        vw_independent_reader = findViewById(R.id.vw_independent_reader);
    }

    private void setButtonClick() {
        vw_pre_reading.setOnClickListener(v -> openBookByGrade(1));
        vw_early_emergent_reader.setOnClickListener(v -> openBookByGrade(2));
        vw_emergent_reader.setOnClickListener(v -> openBookByGrade(3));
        vw_early_fluent_reader.setOnClickListener(v -> openBookByGrade(4));
        vw_fluent_reader.setOnClickListener(v -> openBookByGrade(5));
        vw_advanced_fluent_reader.setOnClickListener(v -> openBookByGrade(6));
        vw_proficient_reader.setOnClickListener(v -> openBookByGrade(7));
        vw_independent_reader.setOnClickListener(v -> openBookByGrade(8));
    }

    private void openBookByGrade(int readingLevel){
        Intent i = new Intent(PlacementTestActivity.this, BookGradeSelectionActivity.class);
        i.putExtra("readingLevel", readingLevel);
        startActivity(i);
    }
}
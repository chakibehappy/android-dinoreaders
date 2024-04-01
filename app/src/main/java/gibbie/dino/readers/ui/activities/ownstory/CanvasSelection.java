package gibbie.dino.readers.ui.activities.ownstory;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import gibbie.dino.readers.R;
import gibbie.dino.readers.ui.fragments.collection.CollectionDetailListAdapter;

public class CanvasSelection extends AppCompatActivity {

    RecyclerView rv_canvas;
    List<CanvasData> canvasDataList;
    CanvasSelectionAdapter canvasAdapter;

    ImageView btn_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canvas_selection);
        getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_brown));

        rv_canvas = findViewById(R.id.rv_canvas);
        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener( v -> this.finish());

        loadCanvasData();
    }

    private void loadCanvasData(){
        canvasDataList = new ArrayList<>();
        canvasDataList.add( new CanvasData(1, "Design 1", "canvas_1", "full_canvas_1"));
        canvasDataList.add( new CanvasData(2, "Design 2", "canvas_2", "full_canvas_2"));
        canvasDataList.add( new CanvasData(3, "Design 3", "canvas_3", "full_canvas_3"));

        canvasAdapter = new CanvasSelectionAdapter(this, canvasDataList);
        CanvasSelectionAdapter.OnItemClickListener clickListener = new CanvasSelectionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                goToCreateOwnStory(canvasDataList.get(position));
            }
        };
        canvasAdapter.setOnItemClickListener(clickListener);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rv_canvas.setLayoutManager(layoutManager);
        rv_canvas.setAdapter(canvasAdapter);
    }

    private void goToCreateOwnStory(CanvasData canvas){
        Intent intent = new Intent(this, CreateOwnStory.class);
        intent.putExtra("canvas", canvas);
        startActivity(intent);
    }

}
package gibbie.dino.readers.ui.activities.readingbuddy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import gibbie.dino.readers.R;
import gibbie.dino.readers.database.SessionManager;

public class PickAvatarBuddyActivity extends AppCompatActivity {

    SessionManager sessionManager;
    ImageView iv_reading_buddy;

    ImageView iv_selected_avatar, iv_selected_frame;
    TextView tv_avatar_name;

    RecyclerView rv_avatar_image;
    private List<ReadingBuddyAvatarModel> avatarList;
    AvatarListAdapter adapter_avatar;

    RecyclerView rv_frame;
    private List<AvatarFrameModel> frameList;
    AvatarFrameListAdapter adapter_frame;

    ReadingBuddyAvatarModel selectedAvatar;
    AvatarFrameModel selectedFrame;
    AppCompatButton btn_select;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_avatar_buddy);

//        iv_reading_buddy = findViewById(R.id.iv_reading_buddy);
        iv_reading_buddy.setVisibility(View.GONE);

        iv_selected_avatar = findViewById(R.id.iv_selected_avatar);
        iv_selected_frame = findViewById(R.id.iv_selected_frame);

        tv_avatar_name = findViewById(R.id.tv_avatar_name);
        rv_avatar_image = findViewById(R.id.rv_avatar_image);
        rv_frame = findViewById(R.id.rv_frame);
        btn_select = findViewById(R.id.btn_select);
        btn_select.setVisibility(View.GONE);
        btn_select.setOnClickListener(v -> selectAvatar());

        sessionManager = new SessionManager(this);

//        loadAvatarList();
//        loadFrameList();
//        showSelectedAvatar();
    }

    private void showSelectedAvatar(){
        if(sessionManager.getReadingBuddy() == null){
            sessionManager.setReadingBuddy(
                    new ReadingBuddyAvatarModel(1, "Cherie", "", "reader_buddy_1", "")
            );
        }
        if(sessionManager.getReadingBuddyFrame() == null){
            sessionManager.setReadingBuddyFrame(
                    new AvatarFrameModel(1, "", "avatar_frame_1", "")
            );
        }
        fillImageByAsetName(iv_selected_avatar, sessionManager.getReadingBuddy().getAssetName());
        fillImageByAsetName(iv_selected_frame, sessionManager.getReadingBuddyFrame().getAssetName());
        tv_avatar_name.setText(sessionManager.getReadingBuddy().getName());
    }

    private void loadAvatarList(){
        rv_avatar_image.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        rv_avatar_image.setLayoutManager(layoutManager);

        avatarList = new ArrayList<>();
        avatarList.add(new ReadingBuddyAvatarModel(1, "Cherie", "", "reader_buddy_1", ""));
        avatarList.add(new ReadingBuddyAvatarModel(2, "Joni", "", "reader_buddy_2", ""));
        avatarList.add(new ReadingBuddyAvatarModel(3, "Violet", "", "reader_buddy_3", ""));

        adapter_avatar = new AvatarListAdapter(this, avatarList, iv_selected_avatar, tv_avatar_name);
        rv_avatar_image.setAdapter(adapter_avatar);
    }

    private void fillImageByAsetName(ImageView imageView, String name){
        String uri = "@drawable/" + name;  // where myresource (without the extension) is the file
        int imageResource = getResources().getIdentifier(uri, null, getPackageName());
        Drawable res = getResources().getDrawable(imageResource);
        imageView.setImageDrawable(res);
    }

    private void loadFrameList(){
        rv_frame.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        rv_frame.setLayoutManager(layoutManager);

        frameList = new ArrayList<>();
        frameList.add(new AvatarFrameModel(1, "", "avatar_frame_1", ""));
        frameList.add(new AvatarFrameModel(2, "", "avatar_frame_2", ""));
        frameList.add(new AvatarFrameModel(3, "", "avatar_frame_3", ""));

        adapter_frame = new AvatarFrameListAdapter(this, frameList, iv_selected_frame);
        rv_frame.setAdapter(adapter_frame);
    }

    public void checkAvatarSelection(){
        selectedAvatar = adapter_avatar.getSelectedAvatar();
        selectedFrame = adapter_frame.getSelectedFrame();
        if(selectedAvatar != null && selectedFrame != null){
            btn_select.setVisibility(View.VISIBLE);
        }
    }

    private void selectAvatar(){
        sessionManager.setReadingBuddy(selectedAvatar);
        sessionManager.setReadingBuddyFrame(selectedFrame);
//        Intent i = new Intent(this, ReadingBuddyActivity.class);
//        startActivity(i);
        finish();
    }

}
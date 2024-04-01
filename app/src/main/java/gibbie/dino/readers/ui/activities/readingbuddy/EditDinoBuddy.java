package gibbie.dino.readers.ui.activities.readingbuddy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import gibbie.dino.readers.R;
import gibbie.dino.readers.database.SessionManager;

public class EditDinoBuddy extends AppCompatActivity {

    SessionManager sessionManager;

    LinearLayout btn_save;
    ImageView btn_back;

    MaterialCardView tab_avatar, tab_accessory, tab_background;

    RecyclerView rv_avatar_image, rv_accessory, rv_background;
    private List<ReadingBuddyAvatarModel> avatarList;
    AvatarItemListAdapter adapter_avatar;
    private List<AvatarFrameModel> accessoryList;
    AvatarFrameListAdapter adapter_accessory;
    private List<AvatarBackgroundModel> bgList;
    AvatarBackgroundListAdapter adapter_bg;

    MaterialCardView background_selected_avatar;
    ImageView iv_selected_avatar, iv_selected_accessory, iv_selected_bg;
    ReadingBuddyAvatarModel selectedAvatar;
    AvatarFrameModel selectedAccessory;
    AvatarBackgroundModel selectedBg;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_dino_buddy);
        getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_brown));

        sessionManager = new SessionManager(this);

        btn_save = findViewById(R.id.btn_save);
        btn_back = findViewById(R.id.btn_back);
        tab_avatar = findViewById(R.id.tab_avatar);
        tab_accessory = findViewById(R.id.tab_accessory);
        tab_background = findViewById(R.id.tab_background);

        rv_avatar_image = findViewById(R.id.rv_avatar_image);
        rv_accessory = findViewById(R.id.rv_accessory);
        rv_background = findViewById(R.id.rv_background);
        background_selected_avatar = findViewById(R.id.background_selected_avatar);
        iv_selected_avatar = findViewById(R.id.iv_selected_avatar);
        iv_selected_accessory = findViewById(R.id.iv_selected_accessory);
        iv_selected_bg = findViewById(R.id.iv_background);

        btn_back.setOnClickListener(v -> this.finish());
        btn_save.setOnClickListener(v -> saveAvatar());

        tab_avatar.setOnClickListener(v -> showTabSelection(0));
        tab_accessory.setOnClickListener(v -> showTabSelection(1));
        tab_background.setOnClickListener(v -> showTabSelection(2));

        showSelectedAvatar();
        loadAvatarList();
        loadCollectionList();
        loadBackgroundList();
        showTabSelection(0);
    }

    void showTabSelection(int menu)
    {
        int activeTabColor = getResources().getColor(R.color.active_tab);
        int inactiveTabColor = getResources().getColor(R.color.inactive_tab);

        rv_avatar_image.setVisibility(menu == 0 ? View.VISIBLE : View.GONE);
        rv_accessory.setVisibility(menu == 1 ? View.VISIBLE : View.GONE);
        rv_background.setVisibility(menu == 2 ? View.VISIBLE : View.GONE);

        tab_avatar.setCardBackgroundColor(menu == 0 ? activeTabColor : inactiveTabColor);
        tab_accessory.setCardBackgroundColor(menu == 1 ? activeTabColor : inactiveTabColor);
        tab_background.setCardBackgroundColor(menu == 2 ? activeTabColor : inactiveTabColor);
    }

    private void showSelectedAvatar(){
        if(sessionManager.getReadingBuddy() == null){
            sessionManager.setReadingBuddy(
                    new ReadingBuddyAvatarModel(1, "T-Rex", "", "t_rex", "")
            );
        }
        if(sessionManager.getReadingBuddyFrame() == null){
            sessionManager.setReadingBuddyFrame(
                    new AvatarFrameModel(1, "", "avatar_frame_1", "")
            );
        }
        if(sessionManager.getReadingBuddyBg() == null){
            sessionManager.setReadingBuddyBackground(
                    new AvatarBackgroundModel(1, "#FFB441",  "")
            );
        }
        selectedAvatar = sessionManager.getReadingBuddy();
        selectedAccessory = sessionManager.getReadingBuddyFrame();
        selectedBg = sessionManager.getReadingBuddyBg();

        fillImageByAsetName(iv_selected_avatar, sessionManager.getReadingBuddy().getAssetName());
        fillImageByAsetName(iv_selected_accessory, sessionManager.getReadingBuddyFrame().getAssetName());
        int bg_color = Color.parseColor(sessionManager.getReadingBuddyBg().getColor());
        background_selected_avatar.setCardBackgroundColor(bg_color);
    }

    private void loadAvatarList()
    {
        rv_avatar_image.setHasFixedSize(true);
        avatarList = new ArrayList<>();
        avatarList.add(new ReadingBuddyAvatarModel(1, "T-Rex", "", "t_rex", ""));
        avatarList.add(new ReadingBuddyAvatarModel(2, "Brachiosaurus", "", "brachiosaurus", ""));
        avatarList.add(new ReadingBuddyAvatarModel(3, "Hadrosaurus", "", "hadrosaurus", ""));
        avatarList.add(new ReadingBuddyAvatarModel(4, "Spinosaurus", "", "spinosaurus", ""));
        avatarList.add(new ReadingBuddyAvatarModel(5, "Plesiosaurus", "", "plesiosaurus", ""));
        avatarList.add(new ReadingBuddyAvatarModel(6, "Ankylosaurus", "", "ankylosaurus", ""));
        avatarList.add(new ReadingBuddyAvatarModel(7, "Dimetrodon", "", "dimetrodon", ""));
        avatarList.add(new ReadingBuddyAvatarModel(8, "Styracosaurus", "", "styracosaurus", ""));
        avatarList.add(new ReadingBuddyAvatarModel(9, "Stegosaurus", "", "stegosaurus", ""));
        avatarList.add(new ReadingBuddyAvatarModel(10, "Kentrosaurus", "", "kentrosaurus",     ""));
        avatarList.add(new ReadingBuddyAvatarModel(11, "Dilophosaurus", "", "dilophosaurus", ""));
        avatarList.add(new ReadingBuddyAvatarModel(12, "Triceratops", "", "triceratops", ""));
        adapter_avatar = new AvatarItemListAdapter(this, avatarList, iv_selected_avatar);
        rv_avatar_image.setAdapter(adapter_avatar);
    }

    void loadCollectionList()
    {
        rv_accessory.setHasFixedSize(true);

        accessoryList = new ArrayList<>();
        accessoryList.add(new AvatarFrameModel(1, "", "avatar_frame_1", ""));
        accessoryList.add(new AvatarFrameModel(2, "", "avatar_frame_2", ""));
        accessoryList.add(new AvatarFrameModel(3, "", "avatar_frame_3", ""));

        adapter_accessory = new AvatarFrameListAdapter(this, accessoryList, iv_selected_accessory);
        rv_accessory.setAdapter(adapter_accessory);
    }

    void loadBackgroundList(){
        rv_background.setHasFixedSize(true);

        bgList = new ArrayList<>();
        bgList.add(new AvatarBackgroundModel(1, "#FFB441",  ""));
        bgList.add(new AvatarBackgroundModel(2, "#FEF200",  ""));
        bgList.add(new AvatarBackgroundModel(3, "#0054A5",  ""));
        bgList.add(new AvatarBackgroundModel(4, "#67E093",  ""));
        bgList.add(new AvatarBackgroundModel(5, "#FF6060",  ""));
        bgList.add(new AvatarBackgroundModel(6, "#00D5D3",  ""));

        adapter_bg = new AvatarBackgroundListAdapter(this, bgList, iv_selected_bg, background_selected_avatar);
        rv_background.setAdapter(adapter_bg);
    }

    private void fillImageByAsetName(ImageView imageView, String name)
    {
        String uri = "@drawable/" + name;
        int imageResource = getResources().getIdentifier(uri, null, getPackageName());
        Drawable res = getResources().getDrawable(imageResource);
        imageView.setImageDrawable(res);
    }


    public void setSelectedAvatar() {
        selectedAvatar = adapter_avatar.getSelectedAvatar();
    }

    public void setSelectedAccessory(){
        selectedAccessory = adapter_accessory.getSelectedFrame();
    }

    public void setSelectedBg(){
        selectedBg = adapter_bg.getSelectedBackground();
    }

    void saveAvatar(){
        sessionManager.setReadingBuddy(selectedAvatar);
        sessionManager.setReadingBuddyFrame(selectedAccessory);
        sessionManager.setReadingBuddyBackground(selectedBg);
        this.finish();
    }
}
package gibbie.dino.readers.ui.activities.readingbuddy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import gibbie.dino.readers.R;

public class AvatarItemListAdapter extends RecyclerView.Adapter<AvatarItemListAdapter.DataViewHolder>{
    private Context mCtx;
    private List<ReadingBuddyAvatarModel> avatarList;
    private ImageView iv_selected_avatar;
    private String selectedName = "";

    private ReadingBuddyAvatarModel selected_avatar;

    public AvatarItemListAdapter(Context mCtx, List<ReadingBuddyAvatarModel> avatarList, ImageView iv_selected_avatar) {
        this.mCtx = mCtx;
        this.avatarList = avatarList;
        this.iv_selected_avatar = iv_selected_avatar;
    }

    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.avatar_buddy_item, parent,false);
        return new DataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DataViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final ReadingBuddyAvatarModel avatar = avatarList.get(position);

        if(!avatar.getAssetName().equals("")){
            fillImageByAsetName(holder.iv_avatar, avatar.getAssetName());
        }
        else if(!avatar.getLocalPath().equals("")){

        }
        else if(!avatar.getImageUrl().equals("")){

        }

        ColorStateList stateList = ColorStateList.valueOf(mCtx.getResources().getColor(R.color.transparent_white));
        if(selectedName.equals(avatar.getName())){
            stateList = ColorStateList.valueOf(mCtx.getResources().getColor(R.color.selectedAvatarColor));
        }
        holder.iv_avatar.setBackgroundTintList(stateList);

        holder.iv_avatar.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                if(!avatar.getAssetName().equals("")){
                    fillImageByAsetName(iv_selected_avatar, avatar.getAssetName());
                }
                selected_avatar = avatar;
                selectedName = avatar.getName();
                notifyDataSetChanged();
                ((EditDinoBuddy)mCtx).setSelectedAvatar();
            }
        });
    }

    @Override
    public int getItemCount() {
        return avatarList.size();
    }

    class DataViewHolder extends RecyclerView.ViewHolder {

        ImageView iv_avatar;

        public DataViewHolder(View itemView) {
            super(itemView);
            iv_avatar = itemView.findViewById(R.id.iv_avatar);
        }
    }

    private void fillImageByAsetName(ImageView imageView, String name){
        String uri = "@drawable/" + name;  // where myresource (without the extension) is the file
        int imageResource = mCtx.getResources().getIdentifier(uri, null,mCtx. getPackageName());
        Drawable res = mCtx.getResources().getDrawable(imageResource);
        imageView.setImageDrawable(res);
    }

    public ReadingBuddyAvatarModel getSelectedAvatar(){
        return selected_avatar;
    }
}
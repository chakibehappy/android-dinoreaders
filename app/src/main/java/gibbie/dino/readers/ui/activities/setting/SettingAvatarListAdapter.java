package gibbie.dino.readers.ui.activities.setting;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import gibbie.dino.readers.R;

public class SettingAvatarListAdapter extends RecyclerView.Adapter<SettingAvatarListAdapter.AvatarViewHolder> {
    private List<UserProfileSetting> userProfileSettingList;
    private int selectedItem = 0;
    private OnSelectionChangeListener selectionChangeListener;

    public interface OnSelectionChangeListener {
        void onSelectionChanged(UserProfileSetting selectedProfile);
    }
    public void setOnSelectionChangeListener(OnSelectionChangeListener listener) {
        this.selectionChangeListener = listener;
    }

    public SettingAvatarListAdapter(List<UserProfileSetting> userProfileSettingList) {
        this.userProfileSettingList = userProfileSettingList;
    }

    @NonNull
    @Override
    public AvatarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.setting_avatar_item, parent, false);
        return new AvatarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AvatarViewHolder holder, @SuppressLint("RecyclerView") int position) {
        UserProfileSetting userProfileSetting = userProfileSettingList.get(position);
        boolean isSelected = (position == selectedItem);

        if (isSelected) {
            holder.cvRoot.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.selected_avatar_outline));
        } else {
            holder.cvRoot.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.transparent_white));
        }

        holder.tvName.setText(userProfileSetting.getProfile().get(0).getName());
        String imageUrl = userProfileSetting.getProfile().get(0).getImgUrl();
        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.profile)
                .into(holder.ivAvatar);

        holder.cvRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSelection(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userProfileSettingList.size();
    }

    static class AvatarViewHolder extends RecyclerView.ViewHolder {
        CardView cvRoot;
        ImageView ivAvatar;
        TextView tvName;

        AvatarViewHolder(@NonNull View itemView) {
            super(itemView);
            cvRoot = itemView.findViewById(R.id.cv_root);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateSelection(int position) {
        selectedItem = position;
        notifyDataSetChanged();

        if (selectionChangeListener != null) {
            UserProfileSetting selectedProfile = userProfileSettingList.get(selectedItem);
            selectionChangeListener.onSelectionChanged(selectedProfile);
        }
    }

    public UserProfileSetting getSelectedProfile(){
        return userProfileSettingList.get(selectedItem);
    }

}

package gibbie.dino.readers.ui.activities.profile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import gibbie.dino.readers.R;
import gibbie.dino.readers.ui.activities.placementtest.GradeBookData;
import gibbie.dino.readers.ui.fragments.collection.CollectionData;
import gibbie.dino.readers.ui.fragments.profile.ProfileModel;

public class ProfileGridViewAdapter extends RecyclerView.Adapter<ProfileGridViewAdapter.DataViewHolder>{
    private Context mCtx;
    private List<ProfileModel> profiles;
    private ProfileGridViewAdapter.OnItemClickListener itemClickListener;

    public ProfileGridViewAdapter(Context mCtx, List<ProfileModel> profiles) {
        this.mCtx = mCtx;
        this.profiles = profiles;
    }

    public void setOnItemClickListener(ProfileGridViewAdapter.OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setFilteredList(List<ProfileModel> profiles){
        this.profiles = profiles;
        notifyDataSetChanged();
    }

    @Override
    public ProfileGridViewAdapter.DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.profile_grid, parent,false);
        return new ProfileGridViewAdapter.DataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProfileGridViewAdapter.DataViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final ProfileModel profile = profiles.get(position);
        final String id = profile.getId();
        final String name = profile.getName();
        holder.tv_name.setText("");
        Picasso.get().load(profile.getImg_url()).placeholder(R.drawable.profile).error(R.drawable.profile).into(holder.iv_profile);

        holder.ll_root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    class DataViewHolder extends RecyclerView.ViewHolder {
        LinearLayout ll_root;
        ImageView iv_profile;
        TextView tv_name;

        public DataViewHolder(View itemView) {
            super(itemView);
            ll_root = itemView.findViewById(R.id.ll_root);
            iv_profile = itemView.findViewById(R.id.iv_profile);
            tv_name = itemView.findViewById(R.id.tv_name);
        }
    }
}
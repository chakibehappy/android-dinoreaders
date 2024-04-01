package gibbie.dino.readers.ui.activities.readingbuddy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import gibbie.dino.readers.R;

public class AvatarBackgroundListAdapter extends RecyclerView.Adapter<AvatarBackgroundListAdapter.DataViewHolder>{
    private Context mCtx;
    private List<AvatarBackgroundModel> bgList;

    private ImageView iv_selected_bg;
    private AvatarBackgroundModel selected_bg;
    private MaterialCardView card_bg;

    public AvatarBackgroundListAdapter(Context mCtx, List<AvatarBackgroundModel> bgList, ImageView iv_selected_bg, MaterialCardView card_bg) {
        this.mCtx = mCtx;
        this.bgList = bgList;
        this.iv_selected_bg = iv_selected_bg;
        this.card_bg = card_bg;
    }

    @Override
    public DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.avatar_background_item, parent,false);
        return new DataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DataViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final AvatarBackgroundModel background = bgList.get(position);

        if(!background.getImageUrl().equals("")){
            // fill iv_selected_bg with picasso
        }

        ColorStateList stateList = ColorStateList.valueOf(mCtx.getResources().getColor(R.color.transparent_white));
        if(selected_bg == background){
            stateList = ColorStateList.valueOf(mCtx.getResources().getColor(R.color.black));
        }
        holder.iv_border.setBackgroundTintList(stateList);

        int bg_color = Color.parseColor(background.getColor());
        holder.card_background.setCardBackgroundColor(bg_color);

        holder.card_background.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                if(!background.getImageUrl().equals("")){
                    // fill iv_selected_bg on edit dino buddy with picasso
                }
                card_bg.setCardBackgroundColor(bg_color);
                selected_bg = background;
                notifyDataSetChanged();
                ((EditDinoBuddy)mCtx).setSelectedBg();
            }
        });
    }

    @Override
    public int getItemCount() {
        return bgList.size();
    }

    class DataViewHolder extends RecyclerView.ViewHolder {

        ImageView iv_border, iv_background;
        MaterialCardView card_background;

        public DataViewHolder(View itemView) {
            super(itemView);
            iv_border = itemView.findViewById(R.id.iv_border);
            iv_background = itemView.findViewById(R.id.iv_background);
            card_background = itemView.findViewById(R.id.card_background);
        }
    }

    public AvatarBackgroundModel getSelectedBackground(){
        return selected_bg;
    }
}
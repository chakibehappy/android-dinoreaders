package gibbie.dino.readers.ui.activities.readingbuddy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import gibbie.dino.readers.R;

public class AvatarFrameListAdapter extends RecyclerView.Adapter<AvatarFrameListAdapter.DataViewHolder>{
    private Context mCtx;
    private List<AvatarFrameModel> frameList;

    private ImageView iv_selected_frame;
    private AvatarFrameModel selected_frame;

    public AvatarFrameListAdapter(Context mCtx, List<AvatarFrameModel> frameList, ImageView iv_selected_frame) {
        this.mCtx = mCtx;
        this.frameList = frameList;
        this.iv_selected_frame = iv_selected_frame;
    }

    @Override
    public DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.avatar_frame_list_item, parent,false);
        return new DataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DataViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final AvatarFrameModel frame = frameList.get(position);

        if(!frame.getAssetName().equals("")){
            fillImageByAsetName(holder.iv_frame, frame.getAssetName());
        }
        else if(!frame.getLocalPath().equals("")){

        }
        else if(!frame.getImageUrl().equals("")){

        }

        ColorStateList stateList = ColorStateList.valueOf(mCtx.getResources().getColor(R.color.transparent_white));
        if(selected_frame == frame){
            stateList = ColorStateList.valueOf(mCtx.getResources().getColor(R.color.yellow));
        }
        holder.iv_frame.setBackgroundTintList(stateList);

        holder.iv_frame.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                if(!frame.getAssetName().equals("")){
                    fillImageByAsetName(iv_selected_frame, frame.getAssetName());
                }
                selected_frame = frame;
                notifyDataSetChanged();
                ((EditDinoBuddy)mCtx).setSelectedAccessory();
            }
        });
    }

    @Override
    public int getItemCount() {
        return frameList.size();
    }

    class DataViewHolder extends RecyclerView.ViewHolder {

        ImageView iv_frame;

        public DataViewHolder(View itemView) {
            super(itemView);
            iv_frame = itemView.findViewById(R.id.iv_frame);
        }
    }

    private void fillImageByAsetName(ImageView imageView, String name){
        String uri = "@drawable/" + name;  // where myresource (without the extension) is the file
        int imageResource = mCtx.getResources().getIdentifier(uri, null,mCtx. getPackageName());
        Drawable res = mCtx.getResources().getDrawable(imageResource);
        imageView.setImageDrawable(res);
    }

    public AvatarFrameModel getSelectedFrame(){
        return selected_frame;
    }
}
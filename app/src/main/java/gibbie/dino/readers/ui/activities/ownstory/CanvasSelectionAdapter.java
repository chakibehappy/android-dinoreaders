package gibbie.dino.readers.ui.activities.ownstory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;
import gibbie.dino.readers.R;

public class CanvasSelectionAdapter extends RecyclerView.Adapter<CanvasSelectionAdapter.DataViewHolder>{
    private Context mCtx;
    private List<CanvasData> canvasDataList;
    private CanvasSelectionAdapter.OnItemClickListener itemClickListener;

    public CanvasSelectionAdapter(Context mCtx, List<CanvasData> canvasDataList) {
        this.mCtx = mCtx;
        this.canvasDataList = canvasDataList;
    }

    public void setOnItemClickListener(CanvasSelectionAdapter.OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @Override
    public CanvasSelectionAdapter.DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.canvas_own_story_item, parent,false);
        return new CanvasSelectionAdapter.DataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CanvasSelectionAdapter.DataViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final CanvasData canvas = canvasDataList.get(position);
        final String title;
        title = canvas.getName();
        holder.tv_name.setText(title);

//        Picasso.get().load(canvas.getThumbnail()).into(holder.iv_thumbnail);
        int resourceImage = mCtx.getResources().getIdentifier(canvas.getThumbnail(), "drawable", mCtx.getPackageName());
        holder.iv_thumbnail.setImageResource(resourceImage);

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
        return canvasDataList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int id);
    }

    class DataViewHolder extends RecyclerView.ViewHolder {
        LinearLayout ll_root;
        ImageView iv_thumbnail;
        TextView tv_name;

        public DataViewHolder(View itemView) {
            super(itemView);
            ll_root = itemView.findViewById(R.id.ll_root);
            iv_thumbnail = itemView.findViewById(R.id.iv_thumbnail);
            tv_name = itemView.findViewById(R.id.tv_name);
        }
    }
}
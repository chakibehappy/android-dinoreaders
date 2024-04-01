package gibbie.dino.readers.ui.fragments.ownstory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import gibbie.dino.readers.R;

public class StoryTemplateAdapter extends RecyclerView.Adapter<StoryTemplateAdapter.DataViewHolder>{
    private Context mCtx;
    private LinearLayout buttonGroup;
    private List<BookTemplateModel> templateList;
    private BookTemplateModel selectedTemplate;

    public StoryTemplateAdapter(Context mCtx, LinearLayout buttonGroup, List<BookTemplateModel> templateList) {
        this.mCtx = mCtx;
        this.buttonGroup = buttonGroup;
        this.templateList = templateList;
    }

    @Override
    public DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.own_story_template_item, parent,false);
        return new DataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DataViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final BookTemplateModel template = templateList.get(position);
        final String name, thumbnail;
        final int id = template.getId();

        if(selectedTemplate != null && selectedTemplate.getId() == id) {
            holder.ll_root.setBackgroundResource(R.drawable.button_outline);
        } else {
            holder.ll_root.setBackgroundResource(R.drawable.button_outline_null);
        }

        name = template.getName();
        thumbnail = template.getThumbnail();
        int resourceImage = mCtx.getResources().getIdentifier(thumbnail, "drawable", mCtx.getPackageName());
        holder.iv_thumbnail.setImageResource(resourceImage);
        holder.tv_name.setText(name);

        holder.iv_thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedTemplate != null) {
                    int prevSelectedPos = templateList.indexOf(selectedTemplate);
                    notifyItemChanged(prevSelectedPos);
                }

                holder.ll_root.setBackgroundResource(R.drawable.button_outline);
                openSelectedTemplate(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return templateList.size();
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

    private void openSelectedTemplate(int pos){
        selectedTemplate = templateList.get(pos);
        buttonGroup.setVisibility(View.VISIBLE);
    }

    public BookTemplateModel getSelectedTemplate(){
        return selectedTemplate;
    }
}
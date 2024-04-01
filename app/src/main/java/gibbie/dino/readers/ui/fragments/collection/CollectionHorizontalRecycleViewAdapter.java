package gibbie.dino.readers.ui.fragments.collection;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import gibbie.dino.readers.R;
import gibbie.dino.readers.ui.fragments.home.BookModel;

public class CollectionHorizontalRecycleViewAdapter extends RecyclerView.Adapter<CollectionHorizontalRecycleViewAdapter.MyViewHolder> {
    public ArrayList<BookModel.Data> childModelArrayList;
    Context cxt;
    private AddOnItemClickListener addOnItemClickListener;

    public CollectionHorizontalRecycleViewAdapter(ArrayList<BookModel.Data> arrayList, Context mContext, AddOnItemClickListener addOnItemClickListener) {
        this.cxt = mContext;
        this.childModelArrayList = arrayList;
        this.addOnItemClickListener = addOnItemClickListener;
    }

    public void setAddOnItemClickListener(AddOnItemClickListener addOnItemClickListener) {
        this.addOnItemClickListener = addOnItemClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.child_recyclerview_items_new, parent, false);
        return new MyViewHolder(view, addOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        BookModel.Data currentItem = childModelArrayList.get(position);
        //holder.heroImage.setImageResource(currentItem.getHeroImage());
//        Picasso.get().load(currentItem.getBaseAndImage_url()).into(holder.heroImage);
        Picasso.get().load(currentItem.getImg_url_only()).into(holder.heroImage);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((Activity) cxt).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        //if you need three fix imageview in width
        int devicewidth = displaymetrics.widthPixels / 3;

        if (currentItem.getLevel().size() > 0) {
            BookModel.Data.Categories cat = currentItem.getLevel().get(0);
            if(cat.getName() != null && cat.getColor() != null) {
                switch (cat.getName()) {
                    case "LEVEL 1 (AGE 3 TO 5)":
                        // card_background
                        holder.cardBackground.setImageResource(R.drawable.level_1);
                        break;
                    case "LEVEL 2 (AGE 4 TO 6)":
                        holder.cardBackground.setImageResource(R.drawable.level_2);
                        break;
                    case "LEVEL 3 (AGE 5 TO 7)":
                        holder.cardBackground.setImageResource(R.drawable.level_3);
                        break;
                    case " LEVEL 4 (AGE 6 TO 8)":
                        holder.cardBackground.setImageResource(R.drawable.level_4);
                        break;
                    default:
                        holder.cardBackground.setImageResource(R.drawable.level_default);
                }
            }
        }
        holder.llRoot.getLayoutParams().width = devicewidth;
//        holder.llRoot.getLayoutParams().height = deviceheight;
        holder.bookTitle.setText(currentItem.getTitle());
        if(currentItem.getFavourite())
            holder.favouriteImage.setImageResource(R.drawable.favourite_button);
        else
            holder.favouriteImage.setImageResource(R.drawable.white_fav_button);

        if(currentItem.getRead_to_me() != null && currentItem.getRead_to_me())
            holder.readToMeImage.setImageResource(R.drawable.read_to_me);
    }

    @Override
    public int getItemCount() {
        return childModelArrayList.size();
    }

    public interface AddOnItemClickListener {
        void onItemClick(int position);
        void onFavouriteClick(int position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView heroImage;
        public TextView bookTitle;
        public LinearLayout llRoot;
        public RelativeLayout rlFavourite;
        public ImageView favouriteImage;
        public ImageView readToMeImage;
        public LinearLayout llReadingLevel;
        public ImageView cardBackground;

        public MyViewHolder(View itemView, AddOnItemClickListener listener) {
            super(itemView);
            addOnItemClickListener = listener;
            heroImage = itemView.findViewById(R.id.hero_image);
            bookTitle = itemView.findViewById(R.id.book_name);
            llRoot = itemView.findViewById(R.id.ll_root);
            rlFavourite = itemView.findViewById(R.id.rl_favourite);
            favouriteImage = itemView.findViewById(R.id.iv_favourite_icon);
            readToMeImage = itemView.findViewById(R.id.iv_read_to_me);
//            llReadingLevel = itemView.findViewById(R.id.ll_reading_level);
//            cardBackground = itemView.findViewById(R.id.card_background);
//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (addOnItemClickListener != null) {
//                        int position = getAdapterPosition();
//                        if (position != RecyclerView.NO_POSITION) {
//                            addOnItemClickListener.onItemClick(position);
//                        }
//                    }
//                }
//            });
            rlFavourite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (addOnItemClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            addOnItemClickListener.onFavouriteClick(position);
                        }
                    }
                }
            });
        }
    }
    private TextView MakeReadingLevel(Activity activity,String text,int tintColor){
        LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        TextView tv_category = new TextView(activity);
        tv_category.setText(text.toUpperCase());
        tv_category.setBackgroundResource(R.drawable.rounded_green_rectangle);
        tv_category.setBackgroundTintList(ColorStateList.valueOf(tintColor));
        tv_category.setTextColor(activity.getResources().getColor(R.color.white));

        tv_category.setTextSize(10);
        tv_category.setMaxLines(1);
        lparams.leftMargin = 10;
        lparams.topMargin = 10;
        tv_category.setPadding((int) activity.getResources().getDimension(R.dimen._15sdp),0,(int) activity.getResources().getDimension(R.dimen._15sdp),0);
        tv_category.setLayoutParams(lparams);
        return tv_category;
    }
}

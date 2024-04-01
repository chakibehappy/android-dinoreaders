package gibbie.dino.readers.ui.fragments.ownstory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;
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

import java.io.File;
import java.util.List;

import gibbie.dino.readers.R;
import gibbie.dino.readers.commonclasses.Functions;
import gibbie.dino.readers.ui.activities.ownstory.ReadOwnStory;
import gibbie.dino.readers.ui.activities.readingbuddy.ReadingBuddyActivity;

public class OwnStoryBookListAdapter extends RecyclerView.Adapter<OwnStoryBookListAdapter.DataViewHolder>{
    private Context mCtx;
    private List<OwnStoryBookModel> ownStoryBookList;

    public OwnStoryBookListAdapter(Context mCtx, List<OwnStoryBookModel> ownStoryBookList) {
        this.mCtx = mCtx;
        this.ownStoryBookList = ownStoryBookList;
    }

    @Override
    public DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.own_story_book_item, parent,false);
        return new DataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DataViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final OwnStoryBookModel book = ownStoryBookList.get(position);
        final String title;
        final int id = book.getId();

        title = book.getTitle();

        if(book.getCoverLocalPath() != null){
            File imageFile = new File(book.getCoverLocalPath());
            if (imageFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(book.getCoverLocalPath());
                holder.iv_cover.setImageBitmap(bitmap);
            }
        }
        else {
            if(Functions.checkInternet(mCtx)){
                Picasso.get().load(book.getCover()).into(holder.iv_cover);
            }
        }

        holder.tv_title.setText(title);

//        if(position == 0){
//            ViewGroup.MarginLayoutParams layoutParams =
//                    (ViewGroup.MarginLayoutParams)  holder.cv_book.getLayoutParams();
//            layoutParams.setMargins(24, 24, 24, 24);
//            holder.cv_book.setLayoutParams(layoutParams);
//        }

        holder.iv_cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mCtx, ReadOwnStory.class);
                i.putExtra("book_id", id);
                mCtx.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ownStoryBookList.size();
    }

    class DataViewHolder extends RecyclerView.ViewHolder {

        CardView cv_book;
        ImageView iv_cover;
        TextView tv_title;

        public DataViewHolder(View itemView) {
            super(itemView);
            cv_book = itemView.findViewById(R.id.cv_book);
            iv_cover = itemView.findViewById(R.id.iv_cover);
            tv_title = itemView.findViewById(R.id.tv_title);
        }
    }

    /**
     * @param encodedString
     * @return bitmap (from given string)
     */
    public Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte=Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }
}
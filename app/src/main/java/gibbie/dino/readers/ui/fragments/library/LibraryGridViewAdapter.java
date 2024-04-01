package gibbie.dino.readers.ui.fragments.library;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import gibbie.dino.readers.R;
import gibbie.dino.readers.ui.fragments.home.BookModel;

public class LibraryGridViewAdapter extends ArrayAdapter<BookModel.Data> {

    private AddOnItemClickListener addOnItemClickListener;
    public interface AddOnItemClickListener {
        void onItemClick(int position);
        void onFavouriteClick(int position);
    }
    public void setAddOnItemClickListener(AddOnItemClickListener addOnItemClickListener) {
        this.addOnItemClickListener = addOnItemClickListener;
    }
    public LibraryGridViewAdapter(Context context, int resource, List<BookModel.Data> objects) {
        super(context,resource,objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if(null == v) {
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.child_recyclerview_items_new, null);
        }
        BookModel.Data book = getItem(position);
        ImageView img = (ImageView) v.findViewById(R.id.hero_image);
        TextView txtTitle = (TextView) v.findViewById(R.id.book_name);
        RelativeLayout rlFavourite = (RelativeLayout)v.findViewById(R.id.rl_favourite);
        ImageView favouriteImage = (ImageView) v.findViewById(R.id.iv_favourite_icon);
//        ImageView cardBackground = (ImageView) v.findViewById(R.id.card_background);

        //Picasso.get().load(book.getBaseAndImage_url()).into(img);
        Picasso.get().load(book.getImg_url_only()).into(img);
        txtTitle.setText(book.getTitle());
        if(book.getFavourite())
            favouriteImage.setImageResource(R.drawable.favourite_button);
        else
            favouriteImage.setImageResource(R.drawable.white_fav_button);

//        if (book.getLevel().size() > 0 && book.getLevel().size() != 0) {
//            BookModel.Data.Categories cat = book.getLevel().get(0);
//            if(cat.getName() != null && cat.getColor() != null) {
//                switch (cat.getName()) {
//                    case "Level 1":
//                        // card_background
//                        cardBackground.setImageResource(R.drawable.level_1);
//                        break;
//                    case "Level 2":
//                        cardBackground.setImageResource(R.drawable.level_2);
//                        break;
//                    case "Level 3":
//                        cardBackground.setImageResource(R.drawable.level_3);
//                        break;
//                    case "Level 4":
//                        cardBackground.setImageResource(R.drawable.level_4);
//                        break;
//                    default:
//                        cardBackground.setImageResource(R.drawable.level_default);
//                }
//            }
//        }
        
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addOnItemClickListener != null) {
                    if (position != RecyclerView.NO_POSITION) {
                        addOnItemClickListener.onItemClick(position);
                    }
                }
            }
        });
        rlFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addOnItemClickListener != null) {
                    if (position != RecyclerView.NO_POSITION) {
                        addOnItemClickListener.onFavouriteClick(position);
                    }
                }
            }
        });

        return v;
    }


}

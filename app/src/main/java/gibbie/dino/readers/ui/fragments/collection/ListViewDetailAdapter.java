package gibbie.dino.readers.ui.fragments.collection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import gibbie.dino.readers.R;
import gibbie.dino.readers.ui.fragments.home.BookModel;

public class ListViewDetailAdapter extends ArrayAdapter<BookModel.Data> {

    Context context;
    public ArrayList<BookModel.Data> childModelArrayList;

    public ListViewDetailAdapter(ArrayList<BookModel.Data> arrayList, Context c) {
        super(c, R.layout.child_collection_details, R.id.tv_title,arrayList);
        this.context = c;
        this.childModelArrayList = arrayList;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        BookModel.Data currentItem = childModelArrayList.get(position);
        LayoutInflater layoutInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = layoutInflater.inflate(R.layout.child_collection_details, parent, false);
        ImageView images = row.findViewById(R.id.image);
        TextView myTitle = row.findViewById(R.id.tv_title);
        TextView myDescription = row.findViewById(R.id.tv_author);

        // now set our resources on views
        Picasso.get().load(currentItem.getImg_url_only()).placeholder(R.drawable.logo).into(images);
        //Picasso.get().load(currentItem.getBaseAndImage_url()).placeholder(R.drawable.logo).into(images);
        myTitle.setText(currentItem.getTitle());
        myDescription.setText(currentItem.getAuthor());

        return row;
    }
}

package gibbie.dino.readers.ui.fragments.collection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import gibbie.dino.readers.R;
import gibbie.dino.readers.ui.activities.placementtest.GradeBookData;

public class CollectionListAdapter extends RecyclerView.Adapter<CollectionListAdapter.DataViewHolder>{
    private Context mCtx;
    private List<CollectionData> collections;
    private OnItemClickListener itemClickListener;

    public CollectionListAdapter(Context mCtx, List<CollectionData> collections) {
        this.mCtx = mCtx;
        this.collections = collections;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @Override
    public CollectionListAdapter.DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.collection_books_item, parent,false);
        return new CollectionListAdapter.DataViewHolder(view);
    }


    @SuppressLint("NotifyDataSetChanged")
    public void setFilteredList(List<CollectionData> collections){
        this.collections = collections;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(CollectionListAdapter.DataViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final CollectionData collection = collections.get(position);
        final String title;
        List<ImageView> covers = new ArrayList<>();
        covers.add(holder.iv_cover_1);
        covers.add(holder.iv_cover_2);
        covers.add(holder.iv_cover_3);
        covers.add(holder.iv_cover_4);

        title = collection.getName();

        for (int i = 0; i <  Math.min(collection.getBooks().size(), 4); i++) {
            covers.get(i).setVisibility(View.VISIBLE);
            Picasso.get().load(collection.getBooks().get(i).getImage_url()).into(covers.get(i));
        }
        holder.tv_title.setText(title);

        holder.cv_book.setOnClickListener(new View.OnClickListener() {
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
        return collections.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int id);
    }

    class DataViewHolder extends RecyclerView.ViewHolder {

        CardView cv_book;
        ImageView iv_cover_1, iv_cover_2, iv_cover_3, iv_cover_4;
        TextView tv_title;

        public DataViewHolder(View itemView) {
            super(itemView);
            cv_book = itemView.findViewById(R.id.cv_book);
            iv_cover_1 = itemView.findViewById(R.id.iv_cover_1);
            iv_cover_2 = itemView.findViewById(R.id.iv_cover_2);
            iv_cover_3 = itemView.findViewById(R.id.iv_cover_3);
            iv_cover_4 = itemView.findViewById(R.id.iv_cover_4);
            tv_title = itemView.findViewById(R.id.tv_title);
        }
    }
}
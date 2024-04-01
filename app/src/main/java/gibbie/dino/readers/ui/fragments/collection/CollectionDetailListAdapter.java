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

public class CollectionDetailListAdapter extends RecyclerView.Adapter<CollectionDetailListAdapter.DataViewHolder>{
    private Context mCtx;
    private List<CollectionData.Book> collections;
    private OnItemClickListener itemClickListener;

    public CollectionDetailListAdapter(Context mCtx, List<CollectionData.Book> collections) {
        this.mCtx = mCtx;
        this.collections = collections;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @Override
    public CollectionDetailListAdapter.DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.collections_detail_book_item, parent,false);
        return new CollectionDetailListAdapter.DataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CollectionDetailListAdapter.DataViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final CollectionData.Book book = collections.get(position);
        final String title;
        final int id = book.getId();
        title = book.getTitle();
        holder.tv_title.setText(title);
        Picasso.get().load(book.getImage_url()).into(holder.iv_cover);

        holder.cv_book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(id);
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
        ImageView iv_cover;
        TextView tv_title;

        public DataViewHolder(View itemView) {
            super(itemView);
            cv_book = itemView.findViewById(R.id.cv_book);
            iv_cover = itemView.findViewById(R.id.iv_cover);
            tv_title = itemView.findViewById(R.id.tv_title);
        }
    }
}
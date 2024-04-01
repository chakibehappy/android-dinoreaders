package gibbie.dino.readers.ui.fragments.home;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import gibbie.dino.readers.R;

public class HorizontalRecyclerViewAdapter extends RecyclerView.Adapter<HorizontalRecyclerViewAdapter.MyViewHolder> {
    public Context cxt;
    private final ArrayList<BookCategoryModel> parentModelArrayList;
    private VerticalRecyclerViewAdapter.AddOnItemClickListener verticalAddOnItemClickListener;
    private boolean whiteColorText;
    private boolean showFavouriteButton;

    public HorizontalRecyclerViewAdapter(ArrayList<BookCategoryModel> bookCategoryList, Context context, boolean whiteColorText, boolean showFavouriteButton) {
        this.parentModelArrayList = bookCategoryList;
        this.cxt = context;
        this.whiteColorText = whiteColorText;
        this.showFavouriteButton = showFavouriteButton;
    }

    public void setVerticalAddOnItemClickListener(VerticalRecyclerViewAdapter.AddOnItemClickListener addOnItemClickListener) {
        this.verticalAddOnItemClickListener = addOnItemClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.parent_recyclerview_items, parent, false);

        MyViewHolder viewholder = new MyViewHolder(view, verticalAddOnItemClickListener);
        return viewholder;
    }

    @Override
    public int getItemCount() {
        return parentModelArrayList.size();
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        BookCategoryModel currentItem = parentModelArrayList.get(position);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(cxt, LinearLayoutManager.HORIZONTAL, false);
        holder.childRecyclerView.setLayoutManager(layoutManager);
        holder.childRecyclerView.setHasFixedSize(true);

        holder.category.setText(currentItem.bookCategory());

        VerticalRecyclerViewAdapter childRecyclerViewAdapter = new VerticalRecyclerViewAdapter(currentItem.books().getData(), holder.childRecyclerView.getContext(), verticalAddOnItemClickListener, whiteColorText, showFavouriteButton);
        childRecyclerViewAdapter.setAddOnItemClickListener(verticalAddOnItemClickListener);
        holder.childRecyclerView.setAdapter(childRecyclerViewAdapter);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView category;
        public RecyclerView childRecyclerView;

        public MyViewHolder(View itemView, VerticalRecyclerViewAdapter.AddOnItemClickListener childListener) {
            super(itemView);
            verticalAddOnItemClickListener = childListener;
            category = itemView.findViewById(R.id.books_category);
            childRecyclerView = itemView.findViewById(R.id.Child_RV);
        }
    }
}
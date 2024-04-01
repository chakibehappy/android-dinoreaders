package gibbie.dino.readers.ui.fragments.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import gibbie.dino.readers.R;

public class DashboardHorizontalRecycleViewAdapter  extends RecyclerView.Adapter<DashboardHorizontalRecycleViewAdapter.MyViewHolder> {
    public Context cxt;
    private final ArrayList<DashboardBookCategoryModel> parentModelArrayList;
    private ArrayList<VerticalRecyclerViewAdapter.AddOnItemClickListener> arrayAdapter;
    private VerticalRecyclerViewAdapter.AddOnItemClickListener ChildVerticalAddOnItemClickListener;
    public DashboardHorizontalRecycleViewAdapter(ArrayList<DashboardBookCategoryModel> bookCategoryList, Context context) {
        this.parentModelArrayList = bookCategoryList;
        this.cxt = context;
        this.arrayAdapter = new ArrayList<>();
    }

    public void addVerticalAddOnItemClickListener(VerticalRecyclerViewAdapter.AddOnItemClickListener addOnItemClickListener) {
        arrayAdapter.add(addOnItemClickListener);
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.parent_recyclerview_items, parent, false);

        return new MyViewHolder(view, ChildVerticalAddOnItemClickListener);
    }


    @Override
    public int getItemCount() {
        return parentModelArrayList.size();
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        DashboardBookCategoryModel currentItem = parentModelArrayList.get(position);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(cxt, LinearLayoutManager.HORIZONTAL, false);
        holder.childRecyclerView.setLayoutManager(layoutManager);
        holder.childRecyclerView.setHasFixedSize(true);

        holder.category.setText(currentItem.bookCategory());
        Picasso.get().load(currentItem.bookCategoryIcon()).into(holder.categoryIcon);
        ChildVerticalAddOnItemClickListener = arrayAdapter.get(position);

        boolean showFavouriteButton = true;
        if(currentItem.bookCategory().equals("Top Books"))
            showFavouriteButton = false;

        VerticalRecyclerViewAdapter childRecyclerViewAdapter = new VerticalRecyclerViewAdapter(currentItem.books().getContent(), holder.childRecyclerView.getContext(), ChildVerticalAddOnItemClickListener, false, showFavouriteButton);

        childRecyclerViewAdapter.setAddOnItemClickListener(ChildVerticalAddOnItemClickListener);
        holder.childRecyclerView.setAdapter(childRecyclerViewAdapter);


    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView category;
        public ImageView categoryIcon;
        public RecyclerView childRecyclerView;

        public MyViewHolder(View itemView, VerticalRecyclerViewAdapter.AddOnItemClickListener childListener) {
            super(itemView);
            ChildVerticalAddOnItemClickListener = childListener;
            category = itemView.findViewById(R.id.books_category);
            childRecyclerView = itemView.findViewById(R.id.Child_RV);
            categoryIcon = itemView.findViewById(R.id.books_category_icon);
        }
    }
}

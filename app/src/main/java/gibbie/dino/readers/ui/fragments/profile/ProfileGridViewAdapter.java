package gibbie.dino.readers.ui.fragments.profile;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import gibbie.dino.readers.R;

public class ProfileGridViewAdapter extends BaseAdapter {

    private Context mContext;

    private ArrayList<ProfileModel> profilesModel;
    private AddOnItemClickListener addOnItemClickListener;

    private boolean showEdit = false;
    private boolean addLast = true;
    private int profileWidth = 180;
    private int profileHeight = 180;
    private boolean setProfileSize = false;
    private boolean setTextSize = false;
    private int textSize;
    public interface AddOnItemClickListener {
        void onItemClick(int position);
    }
    public void setAddOnItemClickListener(AddOnItemClickListener addOnItemClickListener) {
        this.addOnItemClickListener = addOnItemClickListener;
    }
    public ProfileGridViewAdapter(Context context, ArrayList<ProfileModel> profiles, boolean showEdit, boolean addLast) {
        mContext = context;
        this.profilesModel = profiles;
        this.showEdit = showEdit;
        this.addLast = addLast;

    }
    public void setProfileSize(int width, int height){
        this.setProfileSize = true;
        this.profileWidth = width;
        this.profileHeight = height;
    }
    public void setTextSize(int size){
        this.setTextSize = true;
        this.textSize = size;
    }


    @Override
    public int getCount() {
        return profilesModel.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View gridViewAndroid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            gridViewAndroid = inflater.inflate(R.layout.profile_grid, null);
            ImageView iv_profile = (ImageView) gridViewAndroid.findViewById(R.id.iv_profile);
            ImageView edit_pic = (ImageView) gridViewAndroid.findViewById(R.id.iv_edit);
            FrameLayout fl_edit = (FrameLayout) gridViewAndroid.findViewById(R.id.fl_edit);
            CardView cv_parent = (CardView) gridViewAndroid.findViewById(R.id.cv_parent);
            TextView tv_name = (TextView) gridViewAndroid.findViewById(R.id.tv_name);
            ProfileModel data = profilesModel.get(position);
            if(setProfileSize){
                iv_profile.getLayoutParams().height = this.profileHeight;
                iv_profile.getLayoutParams().width = this.profileWidth;
                cv_parent.getLayoutParams().height = this.profileHeight;
                cv_parent.getLayoutParams().width = this.profileWidth;
            }
            if(addLast) {
                if (position == profilesModel.size() - 1) {
                    iv_profile.getLayoutParams().height = 180;
                    iv_profile.getLayoutParams().width = 180;
                }
                if (showEdit && position < profilesModel.size() - 1) {
                    fl_edit.setBackgroundColor(mContext.getResources().getColor(R.color.editDarkBg));
                    edit_pic.setImageResource(R.drawable.edit_big);

                }
            }

            if(data.getLocal_image() != 0) {
                iv_profile.setImageResource(data.getLocal_image());
            }else {
//                Log.e("LOL",data.getImg_url());
                Picasso.get().load(data.getImg_url()).placeholder(R.drawable.profile).error(R.drawable.profile).into(iv_profile);
            }
            if(setTextSize){
                tv_name.setTextSize(TypedValue.COMPLEX_UNIT_PX, gridViewAndroid.getResources().getDimension(textSize));
            }
            tv_name.setText(data.getName());
            gridViewAndroid.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (addOnItemClickListener != null) {
                        if (position != RecyclerView.NO_POSITION) {
                            addOnItemClickListener.onItemClick(position);
                        }
                    }
                }
            });
        } else {
            gridViewAndroid = (View) convertView;
        }

        return gridViewAndroid;
    }
}

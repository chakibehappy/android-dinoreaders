package gibbie.dino.readers.ui.activities.setting;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import gibbie.dino.readers.R;

public class CustomSpinnerAdapter extends ArrayAdapter<ComboBoxItem> {
    private final LayoutInflater inflater;
    private final boolean[] selectedItems;
    private final String allSelectedtext;
    ApplicationSettingActivity applicationSettingActivity;

    public CustomSpinnerAdapter(Context context, List<ComboBoxItem> items, boolean[] selectedItems,
                                String allSelectedtext, ApplicationSettingActivity applicationSettingActivity) {
        super(context, 0, items);
        inflater = LayoutInflater.from(context);
        this.selectedItems = selectedItems;
        this.allSelectedtext = allSelectedtext;
        this.applicationSettingActivity = applicationSettingActivity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.item_spinner_selected, parent, false);
        }

        TextView selectedTextView = view.findViewById(R.id.tv_selected_item);
        String selectedItemsText = getSelectedItemsText();
        selectedTextView.setText(selectedItemsText);

        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.item_spinner_dropdown, parent, false);
        }

        LinearLayout ll_root = view.findViewById(R.id.ll_root);
        CheckBox checkBox = view.findViewById(R.id.checkbox_item);
        TextView labelTextView = view.findViewById(R.id.tv_item_label);

        ComboBoxItem item = getItem(position);
        checkBox.setChecked(selectedItems[position]);
        labelTextView.setText(item.getValue());

        ll_root.setOnClickListener(v -> selectingItem(position, checkBox, false));
        checkBox.setOnClickListener(v -> selectingItem(position, checkBox, true));

        return view;
    }

    private void selectingItem(int position, CheckBox checkBox, Boolean onCheckBox){
        if(!onCheckBox){
            checkBox.setChecked(!checkBox.isChecked());
        }
        selectedItems[position] = checkBox.isChecked();
        boolean isEmpty = true;
        for (int i = 0; i < selectedItems.length; i++) {
            if(selectedItems[i])
                isEmpty = false;
        }

        if(isEmpty){
            Arrays.fill(selectedItems,  true);
        }
        notifyDataSetChanged();
        if(applicationSettingActivity != null){
            applicationSettingActivity.saveSelectedDays(getSelectedValueText());
        }
    }

    private String getSelectedItemsText() {
        StringBuilder selectedTextBuilder = new StringBuilder();
        boolean isAllSelected = true;
        for (int i = 0; i < getCount(); i++) {
            if (selectedItems[i]) {
                ComboBoxItem item = getItem(i);
                if (selectedTextBuilder.length() > 0) {
                    selectedTextBuilder.append(", ");
                }
                selectedTextBuilder.append(item.getLabel());
            }
            else{
                isAllSelected = false;
            }
        }

        return isAllSelected? allSelectedtext : selectedTextBuilder.toString();
    }

    public String getSelectedValueText() {
        StringBuilder selectedTextBuilder = new StringBuilder();
        for (int i = 0; i < getCount(); i++) {
            if (selectedItems[i]) {
                ComboBoxItem item = getItem(i);
                if (selectedTextBuilder.length() > 0) {
                    selectedTextBuilder.append(", ");
                }
                selectedTextBuilder.append(item.getValue());
            }
        }

        return selectedTextBuilder.toString();
    }
}

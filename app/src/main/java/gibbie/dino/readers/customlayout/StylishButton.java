package gibbie.dino.readers.customlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import com.google.android.material.card.MaterialCardView;

import gibbie.dino.readers.R;

public class StylishButton extends MaterialCardView {

    OutlineTextView txt_button;
    LinearLayout linear_skin;
    MaterialCardView material_skin;

    // add another layout background and text color

    public StylishButton(Context context) {
        super(context);
        init(context, null);
    }

    public StylishButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public StylishButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.custom_stylish_button_layout, this, true);

        txt_button  = findViewById(R.id.btn_text);
        linear_skin = findViewById(R.id.linear_skin);
        material_skin = findViewById(R.id.material_skin);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StylishButton);
            String text = a.getString(R.styleable.StylishButton_buttonText);
            float textSize = a.getDimensionPixelSize(R.styleable.StylishButton_buttonTextSize, 24);
            Drawable backgroundDrawable = a.getDrawable(R.styleable.StylishButton_gradientBackground);
            int backgroundColor = a.getColor(R.styleable.StylishButton_buttonColor, 0);

            txt_button.setText(text);
            txt_button.setTextSize(textSize);
            if (backgroundDrawable != null)
                linear_skin.setBackground(backgroundDrawable);
            if (backgroundColor != 0)
                material_skin.setCardBackgroundColor(backgroundColor);

            a.recycle();
        }
    }
}

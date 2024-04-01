package gibbie.dino.readers.customlayout;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.List;

import gibbie.dino.readers.R;

public class OutlineTextView extends RelativeLayout {

    private TextView textFill;
    private List<TextView> textShadows;
    public List<Integer> textFillCharPosList;

    public OutlineTextView(Context context) {
        super(context);
        init(null);
    }

    public OutlineTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public OutlineTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.custom_text_view_layout, this, true);

        textFill = findViewById(R.id.text_fill);
        textShadows = new ArrayList<>();
        textShadows.add(findViewById(R.id.text_shadow_1));
        textShadows.add(findViewById(R.id.text_shadow_2));
        textShadows.add(findViewById(R.id.text_shadow_3));
        textShadows.add(findViewById(R.id.text_shadow_4));
        textShadows.add(findViewById(R.id.text_shadow_5));
        textShadows.add(findViewById(R.id.text_shadow_6));
        textShadows.add(findViewById(R.id.text_shadow_7));
        textShadows.add(findViewById(R.id.text_shadow_8));

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.OutlineTextView);

            String text = a.getString(R.styleable.OutlineTextView_text);
            float textSize = a.getDimensionPixelSize(R.styleable.OutlineTextView_textSize, 16); // Default text size in dp
            int fontFamily = a.getResourceId(R.styleable.OutlineTextView_customFont, 0);
            int strokeWidth = a.getDimensionPixelSize(R.styleable.OutlineTextView_strokeWidth, 2);
            int negativeStroke = -1 * strokeWidth;
            int padding = strokeWidth * 2;
            int textColor = a.getColor(R.styleable.OutlineTextView_textFillColor, Color.WHITE);
            int outlineColor = a.getColor(R.styleable.OutlineTextView_textOutlineColor, Color.BLACK);
            int textAlignment = a.getInt(R.styleable.OutlineTextView_textAlignment, 0);
            a.recycle();

            Typeface typeface;

            if(fontFamily != 0){
                typeface = ResourcesCompat.getFont(getContext(), fontFamily);
                textFill.setTypeface(typeface);
                for (int i = 0; i < textShadows.size(); i++) {
                    textShadows.get(i).setTypeface(typeface);
                }
            }

            textFill.setText(text);
            textFill.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            textFill.setTextColor(textColor);
            textFill.setPadding(padding, padding, padding, padding);
            applyTextAlignment(textFill, textAlignment);

            int[] marginStarts =  {negativeStroke, negativeStroke, negativeStroke, strokeWidth, strokeWidth, strokeWidth, 0, 0};
            int[] marginTops =  {negativeStroke, 0, strokeWidth, negativeStroke, 0, strokeWidth, negativeStroke, strokeWidth};

            for (int i = 0; i < textShadows.size(); i++) {
                textShadows.get(i).setText(text);
                textShadows.get(i).setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                textShadows.get(i).setTextColor(outlineColor);
                textShadows.get(i).setPadding(padding, padding, padding, padding);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) textShadows.get(i).getLayoutParams();
                layoutParams.setMargins(marginStarts[i], marginTops[i], 0, 0);
                textShadows.get(i).setLayoutParams(layoutParams);
                applyTextAlignment(textShadows.get(i), textAlignment);
            }
        }
    }

    public void setText(String newText) {
        textFill.setText(newText);
        for (TextView shadowTextView : textShadows) {
            shadowTextView.setVisibility(VISIBLE);
            shadowTextView.setText(newText);
        }
    }

    public void setTextFillCharPosition(){
        textFillCharPosList = new ArrayList<>();
        String displayText = (String) textFill.getText();
        Layout layout = textFill.getLayout();
        for (int j = 0; j < displayText.length(); j++) {
            textFillCharPosList.add((int) layout.getPrimaryHorizontal(j));
        }
    }
    public void removeOverlapShadowText(){
        setTextFillCharPosition();
        String displayText = (String) textFill.getText();
        for (int i = 0; i < textShadows.size(); i++) {
            Layout layout = textShadows.get(i).getLayout();
            for (int j = 0; j < displayText.length(); j++) {
                int shadowX = (int) layout.getPrimaryHorizontal(j);
                String modifiedString = displayText;
                if(shadowX != textFillCharPosList.get(j)){
                    Log.e("OutlineText", "overlap start at " + j);

                    String fitTexts = displayText.substring(0, displayText.length() - j);
                    String overlapTexts = displayText.substring(j);
                    Log.d("OutlineText", "fit texts : " + fitTexts);
                    Log.d("OutlineText", "overlap texts : " + overlapTexts);
                    String htmlText = fitTexts + "<font color='#00000000'>"+ overlapTexts +"</font>";

                    CharSequence spannedText = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        spannedText = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY);
                    }

                    textShadows.get(i).setText(spannedText);
                    break;
                }
            }
        }
    }

    public int[] getEndTextPosition(TextView textView){
        Layout layout = textView.getLayout();
        int[] pos = { 0, 0 };
        if (layout != null) {
            int lineOfText = layout.getLineForOffset(textView.getText().length());
            pos[0] = (int) layout.getPrimaryHorizontal(textView.getText().length());
            pos[1] = (int) (layout.getLineTop(lineOfText) + textView.getY());
            // Log.d( "OutlineText", textView.getTag() + " position-> x : " + pos[0] + ", y : " + pos[1]);
        }
        return pos;
    }

    public void checkAndHideOverlapShadow(){
        setTextFillCharPosition();
        textFill.setTag("text_fill");
        int[] fillPos = getEndTextPosition(textFill);
        int i = 1;
        for (TextView shadowTextView : textShadows) {
            shadowTextView.setTag("text_shadow_" + i);
            int[] shadowPos = getEndTextPosition(shadowTextView);
            if(shadowPos[0] != fillPos[0]){
                Log.w("OutlineText", "Find outline text shadow overlapping at " + shadowTextView.getTag() + ", hiding the shadow.");
                shadowTextView.setVisibility(GONE);
            }
            i++;
        }
    }

    public void setHighlightText(Spannable newText) {
        textFill.setText(newText);
    }

    public void setHighlightText(Spanned newText) {
        textFill.setText(newText);
    }

    public void setTextSize(float size){
        textFill.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        for (TextView shadowTextView : textShadows) {
            shadowTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        }
    }

    // Add a new method for applying text alignment
    private void applyTextAlignment(TextView textView, int alignment) {
        switch (alignment) {
            case 0: // Center
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                break;
            case 1: // Start
                textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                break;
            case 2: // End
                textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                break;
            default:
                // Do nothing or handle default case
                break;
        }
    }
}


package gibbie.dino.readers.ui.activities.quiz;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import gibbie.dino.readers.R;
import gibbie.dino.readers.customlayout.OutlineTextView;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.ui.activities.bottomnav.BottomNavigation;
import gibbie.dino.readers.ui.fragments.singlebook.SingleBookFragment;

public class QuizActivity extends AppCompatActivity {

    private List<Quiz> quizList;
    int currentQuizIndex = 0;

    // Question Quiz Section :
    LinearLayout ll_question_quiz_section;
    LinearLayout ll_answer_1, ll_answer_2;
    OutlineTextView tv_question;
    OutlineTextView tv_answer_1, tv_answer_2;
    List<String> answerList;

    // Jumble Word Section :
    ConstraintLayout ll_jumble_word_section;
    LinearLayout ll_jumble_word_question, ll_jumble_word_answer;

    List<View> initialLetterBoxs;
    List<View> answerLetterBoxs;

    List<String> playerAnswer;
    List<Integer> initialBoxOrder;

    List<Integer> xStartBoxs, yStartBoxs, xEndBoxs, yEndBoxs;
    Integer minTextSize = 20;
    Integer maxTextSize = 48;
    Integer boxWidth = 20;

    ImageView btn_close;
    String defaultMenu = "Home";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_brown));
        init();
    }


    private void init() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE
        );

        ll_question_quiz_section = findViewById(R.id.ll_question_quiz_section);
        tv_question = findViewById(R.id.tv_question);
        ll_answer_1 = findViewById(R.id.ll_answer_1);
        ll_answer_2 = findViewById(R.id.ll_answer_2);
        tv_answer_1 = findViewById(R.id.tv_answer_1);
        tv_answer_2 = findViewById(R.id.tv_answer_2);

        ll_jumble_word_section = findViewById(R.id.ll_jumble_word_section);
        ll_jumble_word_question = findViewById(R.id.ll_jumble_word_question);
        ll_jumble_word_answer = findViewById(R.id.ll_jumble_word_answer);

        btn_close = findViewById(R.id.btn_close);
        btn_close.setOnClickListener( v -> {this.finish();});

        Intent intent = getIntent();
        quizList = (List<Quiz>) intent.getSerializableExtra("quizList");
        defaultMenu = intent.getStringExtra("defaultMenu");

        initQuiz();
    }

    private void initQuiz(){
        ll_question_quiz_section.setVisibility(View.GONE);
        ll_jumble_word_section.setVisibility(View.GONE);
        if(quizList.get(currentQuizIndex).getQuiz_type().equals("Multiple Choice"))
            initMultipleChoice();
        else
            initJumbleWords();
    }

    private void initMultipleChoice()
    {
        tv_question.setText(quizList.get(currentQuizIndex).getQuestion());
        answerList = new ArrayList<>();
        answerList.add(quizList.get(currentQuizIndex).getWrong_answer());
        answerList.add(quizList.get(currentQuizIndex).getRight_answer());
        Collections.shuffle(answerList);

        tv_answer_1.setText(answerList.get(0));
        tv_answer_2.setText(answerList.get(1));

        ll_answer_1.setOnClickListener(v -> {
            if(quizList.get(currentQuizIndex).getRight_answer().equals(answerList.get(0))){
                goToNextQuiz();
            }
        });

        ll_answer_2.setOnClickListener(v -> {
            if(quizList.get(currentQuizIndex).getRight_answer().equals(answerList.get(1))){
                goToNextQuiz();
            }
        });

        ll_question_quiz_section.setVisibility(View.VISIBLE);
    }

    private void goToNextQuiz(){
        currentQuizIndex++;
        if(quizList.size() - 1 >= currentQuizIndex)
            initQuiz();
        else
            closeQuiz();
    }

    void closeQuiz(){
        Intent intents = new Intent(this, BottomNavigation.class);
        intents.putExtra("defaultMenu", defaultMenu);
        intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intents);
        this.finish();
    }

    private void initJumbleWords()
    {
        ll_jumble_word_question.removeAllViews();
        ll_jumble_word_answer.removeAllViews();
        ll_jumble_word_section.setVisibility(View.VISIBLE);
        String shuffleWord = shuffleString(quizList.get(currentQuizIndex).getQuestion());

        initialLetterBoxs = new ArrayList<>();
        answerLetterBoxs = new ArrayList<>();
        xStartBoxs = new ArrayList<>();
        yStartBoxs = new ArrayList<>();
        xEndBoxs = new ArrayList<>();
        yEndBoxs = new ArrayList<>();

        playerAnswer = new ArrayList<>();
        initialBoxOrder = new ArrayList<>();

        for (int i = 0; i < shuffleWord.length(); i++)
        {
            View letterBox = LayoutInflater.from(this).inflate(R.layout.jumble_letter_box_active, ll_jumble_word_question, false);
            TextView letter = letterBox.findViewById(R.id.tv_letter);
            String charString = Character.toString(shuffleWord.charAt(i));
            letter.setText(charString.toUpperCase());
            ll_jumble_word_question.addView(letterBox);
            initialLetterBoxs.add(letterBox);

            View emptyLetterBox = LayoutInflater.from(this).inflate(R.layout.jumble_letter_box_white, ll_jumble_word_answer, false);
            ll_jumble_word_answer.addView(emptyLetterBox);
            answerLetterBoxs.add(emptyLetterBox);

            // Use ViewTreeObserver to wait for layout measurement
            ViewTreeObserver viewTreeObserver = letterBox.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // Remove the listener to prevent multiple calls
                    letterBox.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int[] location = new int[2];
                    letterBox.getLocationOnScreen(location);
                    xStartBoxs.add((int) letterBox.getX());
                    yStartBoxs.add(location[1]);
                }
            });


            // Use ViewTreeObserver to wait for layout measurement
            ViewTreeObserver viewTreeObserverAnswer = emptyLetterBox.getViewTreeObserver();
            viewTreeObserverAnswer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // Remove the listener to prevent multiple calls
                    emptyLetterBox.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int[] location = new int[2];
                    emptyLetterBox.getLocationOnScreen(location);
                    xEndBoxs.add(((int) emptyLetterBox.getX()));
                    yEndBoxs.add(location[1]);
                }
            });

            final int boxIndex = i;
            letterBox.setOnClickListener(v -> {
                String text = letter.getText().toString();
                if(text.equals(""))
                    return;
                final int boxTarget = playerAnswer.size();
                View animBox = LayoutInflater.from(QuizActivity.this).inflate(R.layout.jumble_letter_box_active, ll_jumble_word_question, false);
                ll_jumble_word_section.addView(animBox);
                animBox.setVisibility(View.GONE);

                Animation animation = new TranslateAnimation(
                        Animation.ABSOLUTE, xStartBoxs.get(boxIndex), // From X
                        Animation.ABSOLUTE, xEndBoxs.get(boxTarget), // To X
                        Animation.ABSOLUTE, yStartBoxs.get(boxIndex), // From Y
                        Animation.ABSOLUTE, yEndBoxs.get(boxTarget) // To Y
                );
                animation.setDuration(250);
                animation.setFillAfter(true);

                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        TextView animBoxText = animBox.findViewById(R.id.tv_letter);
                        animBoxText.setText(text);
                        animBox.setVisibility(View.VISIBLE);
                        letterBox.setAlpha(0);
                        letter.setText("");
                    }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        ll_jumble_word_section.removeView(animBox);
                        answerLetterBoxs.get(boxTarget).setBackgroundResource(R.drawable.jumble_word_box);
                        TextView answerBoxText = answerLetterBoxs.get(boxTarget).findViewById(R.id.tv_letter);
                        answerBoxText.setText(text);
                        checkJumbleWordsAnswer();
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                animBox.startAnimation(animation);
                playerAnswer.add(text);
                initialBoxOrder.add(boxIndex);
            });

            emptyLetterBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView answerBoxText = emptyLetterBox.findViewById(R.id.tv_letter);
                    String text = answerBoxText.getText().toString();
                    if(text.equals(""))
                        return;

                    int boxTarget = initialBoxOrder.get(boxIndex);
                    View animBox = LayoutInflater.from(QuizActivity.this).inflate(R.layout.jumble_letter_box_active, ll_jumble_word_question, false);
                    ll_jumble_word_section.addView(animBox);
                    animBox.setVisibility(View.GONE);

                    Animation animation = new TranslateAnimation(
                            Animation.ABSOLUTE, xEndBoxs.get(boxIndex), // From X
                            Animation.ABSOLUTE, xStartBoxs.get(boxTarget), // To X
                            Animation.ABSOLUTE, yEndBoxs.get(boxIndex), // From Y
                            Animation.ABSOLUTE, yStartBoxs.get(boxTarget) // To Y
                    );
                    animation.setDuration(250);
                    animation.setFillAfter(true);

                    // Set animation listeners
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            TextView animBoxText = animBox.findViewById(R.id.tv_letter);
                            animBoxText.setText(text);
                            animBox.setVisibility(View.VISIBLE);
                        }
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            ll_jumble_word_section.removeView(animBox);
                            initialLetterBoxs.get(boxTarget).setBackgroundResource(R.drawable.jumble_word_box);
                            initialLetterBoxs.get(boxTarget).setAlpha(1);
                            TextView initialBoxText = initialLetterBoxs.get(boxTarget).findViewById(R.id.tv_letter);
                            initialBoxText.setText(text);
                        }
                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                    });
                    animBox.startAnimation(animation);
                    playerAnswer.remove(boxIndex);
                    initialBoxOrder.remove(boxIndex);
                    reorderAnswerBox();
                }
            });
        }
    }

    private void reorderAnswerBox(){
        for (int i = 0; i < answerLetterBoxs.size(); i++) {
            answerLetterBoxs.get(i).setBackgroundResource(R.drawable.jumble_word_box_white);
            TextView answerBoxText = answerLetterBoxs.get(i).findViewById(R.id.tv_letter);
            answerBoxText.setText("");
        }
        for (int i = 0; i < playerAnswer.size(); i++) {
            answerLetterBoxs.get(i).setBackgroundResource(R.drawable.jumble_word_box);
            TextView answerBoxText = answerLetterBoxs.get(i).findViewById(R.id.tv_letter);
            answerBoxText.setText(playerAnswer.get(i));
        }
    }

    public String shuffleString(String input) {
        // Convert the input string to a char array
        char[] charArray = input.toCharArray();

        // Shuffle the char array
        Random random = new Random();
        for (int i = charArray.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            char temp = charArray[index];
            charArray[index] = charArray[i];
            charArray[i] = temp;
        }

        // Convert the shuffled char array back to a string
        return new String(charArray);
    }

    private void checkJumbleWordsAnswer(){
        String jumbleWordAnswer = "";
        for (int i = 0; i < playerAnswer.size(); i++) {
            jumbleWordAnswer += playerAnswer.get(i);
        }

        if(jumbleWordAnswer.equals(quizList.get(currentQuizIndex).getQuestion().toUpperCase())){
            goToNextQuiz();
        }
    }
}
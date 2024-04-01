package gibbie.dino.readers.ui.activities.readingbuddy;

import android.widget.TextView;

import gibbie.dino.readers.commonclasses.AudioController;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.ui.fragments.ownstory.SpellChecker;

public class ReadingBuddyPointHelper {

    public static final int MIN_SESSION_POINT = 1;
    public static final int MAX_SESSION_POINT = 3;

    int pointToAdd = MAX_SESSION_POINT;
    TextView tv_point;

    private int totalPoint = 0;

    SessionManager sessionManager = new SessionManager(SpellChecker.getAppContext());

    public int getTotalPoint(){
        totalPoint = sessionManager.getReadingBuddyPoint();
        return totalPoint;
    }

    public void setCurrentPoint(int point){
        sessionManager.setReadingBuddyPoint(point);
    }

    public void addPoint(int point){
        totalPoint += point;
        setCurrentPoint(totalPoint);
        showPoint();
    }

    public void setTextBoxPoint(TextView textView){
        tv_point = textView;
        showPoint();
    }

    public void showPoint(){
        tv_point.setText(String.valueOf(getTotalPoint()));
    }

    public void wrongAnswer(){
        if(pointToAdd > MIN_SESSION_POINT)
            pointToAdd--;
    }

    public void rightAnswer(){
        AudioController audioController = new AudioController();
        audioController.playSFX(SpellChecker.getAppContext(), "success");
        addPoint(pointToAdd);
        pointToAdd = MAX_SESSION_POINT;
    }
}

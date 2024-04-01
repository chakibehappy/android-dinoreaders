package gibbie.dino.readers.ui.activities.quiz;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Quiz implements Serializable {
    @SerializedName("id")
    private int id;

    @SerializedName("quiz_type")
    private String quiz_type;

    @SerializedName("question")
    private String question = "";

    @SerializedName("right_answer")
    private String right_answer = "";

    @SerializedName("wrong_answer")
    private String wrong_answer = "";

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }
    public void setQuestion(String question) {
        this.question = question;
    }

    public String getQuiz_type() {
        return quiz_type;
    }
    public void setQuiz_type(String quiz_type) {
        this.quiz_type = quiz_type;
    }

    public String getWrong_answer() {
        return wrong_answer;
    }
    public void setWrong_answer(String wrong_answer) {
        this.wrong_answer = wrong_answer;
    }

    public String getRight_answer() {
        return right_answer;
    }
    public void setRight_answer(String right_answer) {
        this.right_answer = right_answer;
    }
}

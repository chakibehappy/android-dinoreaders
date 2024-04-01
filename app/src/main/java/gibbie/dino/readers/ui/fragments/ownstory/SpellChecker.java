package gibbie.dino.readers.ui.fragments.ownstory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;

public class SpellChecker extends Application {

    @SuppressLint("StaticFieldLeak")
    private static SpellChecker mInstance;
    @SuppressLint("StaticFieldLeak")
    private static Context context;


    private static String TAG = "SPELL_CHECKER";
    private final List<String> englishWordDB = new ArrayList<>();
    private final List<String> syllableWordDB = new ArrayList<>();
    private final List<String> syllables = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        SpellChecker.context = getApplicationContext();

        generateEnglishWordDatabase(getAppContext());
    }

    public static Context getAppContext() {
        return SpellChecker.context;
    }

    public static synchronized SpellChecker getInstance() {
        return mInstance;
    }

    public void generateEnglishWordDatabase(Context context){
        if(englishWordDB.size() > 0)
            return;

        Log.d(TAG, "Generating English Word Database");
        try {
            BufferedReader bReader = new BufferedReader(new InputStreamReader(context.getAssets().open("english_word_db.txt")));
            String line = bReader.readLine();
            while (line != null) {
                englishWordDB.add(line);
                line = bReader.readLine();
            }
            bReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Generating Syllable Database");
        try {
            BufferedReader bReader = new BufferedReader(new InputStreamReader(context.getAssets().open("syllables_db.txt")));
            String line = bReader.readLine();
            while (line != null) {
                String[] part = line.split("=");
                syllableWordDB.add(part[0]);
                syllables.add(part[1]);
                line = bReader.readLine();
            }
            bReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getSyllable(String word){
        String wordToCheck = word.toLowerCase(Locale.ROOT);
        if(syllableWordDB.contains(wordToCheck)){
            return syllables.get(syllableWordDB.indexOf(wordToCheck));
        }
        return word;
    }

    public String checkSentence(String input) {

        List<String> wordsToReplace = Arrays.asList(".", ",");
        for (String word : wordsToReplace) {
            input = input.replace(word, "");
        }

        ArrayList<String> misspelledWords = checkSpelling(input);
        return TextUtils.join(" ", misspelledWords) + ".";
    }

    private ArrayList<String> checkSpelling(String input) {
        String[] words = input.split("\\s+");
        ArrayList<String> combinedWords = new ArrayList<>();
        for (String word : words) {
            if (!word.isEmpty()) {
                if (!wordIsExist(word)) {
                    String wrongWord = "<u>" + word + "</u>";
                    combinedWords.add(wrongWord);
                }else{
                    combinedWords.add(word);
                }
            }
        }
        return combinedWords;
    }

    public boolean wordIsExist(String word) {
        return englishWordDB.contains(word.toUpperCase());
    }
}
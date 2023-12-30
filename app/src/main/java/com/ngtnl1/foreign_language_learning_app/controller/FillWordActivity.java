package com.ngtnl1.foreign_language_learning_app.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ngtnl1.foreign_language_learning_app.R;
import com.ngtnl1.foreign_language_learning_app.model.Vocabulary;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FillWordActivity extends AppCompatActivity {
    TextView tv_fillWord,tv_questionNumber;
    Button btn_submit;
    EditText  ed_fillWord;
    List<Vocabulary> vocabularyArrayList;
    Random random ;
    Button btn_audio;
    TextToSpeech mTTS;
    int currentScore = 0, currentPosition, questionAttempted = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_word);

        bindUIControl();

        Intent intent = getIntent();
        vocabularyArrayList = (List<Vocabulary>) intent.getSerializableExtra("vocabularies");

        if (vocabularyArrayList == null ||vocabularyArrayList.isEmpty() || vocabularyArrayList.size() < 10) {
            Toast.makeText(this, "Chủ đề phải có ít nhất 10 từ vựng để thực hiện chức năng này", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentPosition = random.nextInt(vocabularyArrayList.size());

        setDataToViews(currentPosition);

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ed_fillWord.getText().toString().trim().toUpperCase().equals(vocabularyArrayList.get(currentPosition).getWord().toUpperCase())){
                    currentScore++;
                }
                questionAttempted++;
                currentPosition = random.nextInt(vocabularyArrayList.size());
                setDataToViews(currentPosition);
            }
        });

        mTTS = new TextToSpeech(getApplicationContext(), status -> {
            if (status != TextToSpeech.ERROR) {
                mTTS.setLanguage(Locale.US);
            }
        });

        btn_audio.setOnClickListener(v -> speak(currentPosition));
    }

    private  void speak(int currentPosition){
        try {
            String text = vocabularyArrayList.get(currentPosition).getWord().toUpperCase();
            mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(getApplicationContext(), "Lỗi! Không thể phát bản ghi âm thanh của từ vựng", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if(mTTS !=null){
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();
    }

    private void setDataToViews(int currentPosition) {
        if(questionAttempted == 11){
            showScore();
            return;
        }

        tv_questionNumber.setText("Question Attemped:" + questionAttempted + "/10");
        String question = vocabularyArrayList.get(currentPosition).getWord().toUpperCase();
            ed_fillWord.setText("");
    }

    private void showScore() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(FillWordActivity.this);
        View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_score,(LinearLayout)findViewById(R.id.layout_score));
        TextView tv_score = bottomSheetView.findViewById(R.id.tv_score);
        Button btn_restart = bottomSheetView.findViewById(R.id.btn_restart);
        Button btn_back = bottomSheetView.findViewById(R.id.btn_back);

        tv_score.setText("Điểm số của bạn là "+ currentScore +"/10");
        btn_restart.setOnClickListener(v -> {
            currentPosition = random.nextInt(vocabularyArrayList.size());
            currentScore = 0;
            questionAttempted = 1;
            bottomSheetDialog.dismiss();
            setDataToViews(currentPosition);
        });

        btn_back.setOnClickListener(v -> {
            finish();
        });

        bottomSheetDialog.setCancelable(false);
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();

    }

    private static String replaceChars(String input, int position, char replacement) {
        if (position >= 1 && position <= input.length()) {
            char[] charArray = input.toCharArray();
            charArray[position - 1] = replacement;
            return new String(charArray);
        } else {
            return input;
        }
    }

    public static boolean checkDuplicates(int var1, int var2, int var3) {
        return (var1 == var2 || var1 == var3 || var2 == var3);
    }

    private void bindUIControl() {
        tv_fillWord = findViewById(R.id.tv_fillWord);
        ed_fillWord = findViewById(R.id.ed_fillWord);
        btn_submit = findViewById(R.id.btn_submitButton);
        tv_questionNumber = findViewById(R.id.tv_questionNumber);
        btn_audio = findViewById(R.id.btn_speak);

        vocabularyArrayList = new ArrayList<>();
        random = new Random();
    }
}
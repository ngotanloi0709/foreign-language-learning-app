package com.ngtnl1.foreign_language_learning_app.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ngtnl1.foreign_language_learning_app.R;
import com.ngtnl1.foreign_language_learning_app.model.Vocabulary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class QuizzActivity extends AppCompatActivity {

    Button btn_option1, btn_option2, btn_option3, btn_option4;
    TextView tv_generateQuestion,tv_questionNumber;
    List<Vocabulary> vocabularyArrayList;
    Random random ;
    int currentScore = 0,currentPosition,questionAttempted = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quizz);
        bindUIControl();

        Intent intent = getIntent();
        vocabularyArrayList = (List<Vocabulary>) intent.getSerializableExtra("vocabularies");

        if (vocabularyArrayList == null ||vocabularyArrayList.isEmpty() || vocabularyArrayList.size() < 4) {
            Toast.makeText(this, "Chủ đề phải có ít nhất 4 từ vựng để thực hiện chức năng này", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentPosition = random.nextInt(vocabularyArrayList.size());
        setDataToViews(currentPosition);

        btn_option1.setOnClickListener(v -> {
            if(btn_option1.getText().toString().trim().equalsIgnoreCase(vocabularyArrayList.get(currentPosition).getMeaning())){
                currentScore++;
            }

            questionAttempted++;
            currentPosition = random.nextInt(vocabularyArrayList.size());
            setDataToViews(currentPosition);
        });

        btn_option2.setOnClickListener(v -> {
            if(btn_option2.getText().toString().trim().equalsIgnoreCase(vocabularyArrayList.get(currentPosition).getMeaning())){
                currentScore++;
            }
            questionAttempted++;
            currentPosition = random.nextInt(vocabularyArrayList.size());
            setDataToViews(currentPosition);
        });


        btn_option3.setOnClickListener(v -> {
            if(btn_option3.getText().toString().trim().equalsIgnoreCase(vocabularyArrayList.get(currentPosition).getMeaning())){
                currentScore++;
            }
            questionAttempted++;
            currentPosition = random.nextInt(vocabularyArrayList.size());
            setDataToViews(currentPosition);
        });

        btn_option4.setOnClickListener(v -> {
            if(btn_option4.getText().toString().trim().toUpperCase().equals(vocabularyArrayList.get(currentPosition).getMeaning().toUpperCase())){
                currentScore++;
            }
            questionAttempted++;
            currentPosition = random.nextInt(vocabularyArrayList.size());
            setDataToViews(currentPosition);
        });
    }

    private void setDataToViews(int currentPosition) {
        if (questionAttempted == 11) {
            showScore();
            return;
        }

        tv_questionNumber.setText("Question Attemped:" + questionAttempted + "/10");
        String question = vocabularyArrayList.get(currentPosition).getWord().toUpperCase();
        tv_generateQuestion.setText("What does "+ question + " mean ?");

        String answer = vocabularyArrayList.get(currentPosition).getMeaning().toUpperCase();


        int indexOption1 = random.nextInt(vocabularyArrayList.size());
        int indexOption2 = random.nextInt(vocabularyArrayList.size());
        int indexOption3 = random.nextInt(vocabularyArrayList.size());

        while(checkDuplicates(currentPosition,indexOption1,indexOption2,indexOption3)){
            indexOption1 = random.nextInt(vocabularyArrayList.size());
            indexOption2 = random.nextInt(vocabularyArrayList.size());
            indexOption3 = random.nextInt(vocabularyArrayList.size());
        }
        String option1 = vocabularyArrayList.get(indexOption1).getMeaning().toUpperCase();
        String option2 = vocabularyArrayList.get(indexOption2).getMeaning().toUpperCase();
        String option3 = vocabularyArrayList.get(indexOption3).getMeaning().toUpperCase();

        ArrayList<String> stringArray = new ArrayList<>();

        stringArray.add(answer);
        stringArray.add(option1);
        stringArray.add(option2);
        stringArray.add(option3);

        Collections.shuffle(stringArray);
        btn_option1.setText(stringArray.get(0));
        btn_option2.setText(stringArray.get(1));
        btn_option3.setText(stringArray.get(2));
        btn_option4.setText(stringArray.get(3));
    }

    private void showScore() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(QuizzActivity.this);
        View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_score,(LinearLayout)findViewById(R.id.layout_score));
        TextView tv_score = bottomSheetView.findViewById(R.id.tv_score);
        Button btn_restart = bottomSheetView.findViewById(R.id.btn_restart);
        Button btn_back = bottomSheetView.findViewById(R.id.btn_back);

        tv_score.setText("Điểm số của bạn là "+ currentScore + "/10");

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

    public static boolean checkDuplicates(int var1, int var2, int var3, int var4) {
        return (var1 == var2 || var1 == var3 || var1 == var4 ||
                var2 == var3 || var2 == var4 ||
                var3 == var4);
    }

    private void bindUIControl() {
        btn_option1 = findViewById(R.id.option1);
        btn_option2 = findViewById(R.id.option2);
        btn_option3 = findViewById(R.id.option3);
        btn_option4 = findViewById(R.id.option4);

        tv_generateQuestion = findViewById(R.id.tv_generateQuestion);
        tv_questionNumber = findViewById(R.id.tv_questionNumber);
        vocabularyArrayList = new ArrayList<>();
        random = new Random();
    }
}
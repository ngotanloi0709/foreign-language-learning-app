package com.ngtnl1.foreign_language_learning_app.controller;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ngtnl1.foreign_language_learning_app.R;
import com.ngtnl1.foreign_language_learning_app.model.Vocabulary;

import java.util.List;

public class FlipCardActivity extends AppCompatActivity {

    AnimatorSet frontAnim, backAnim;
    boolean isFront = true;
    List<Vocabulary> vocabularyArrayList;
    int currentVocabularyIndex = 0;
    TextView cardFront, cardBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flipcard);

        initializeViews();
        setupAnimations();
        setOnClickListener();
        // Set initial data
        setDataToViews(currentVocabularyIndex);
    }

    private void initializeViews() {
        float scale = getApplicationContext().getResources().getDisplayMetrics().density;

        Intent intent = getIntent();
        if (intent != null) {
            vocabularyArrayList = (List<Vocabulary>) intent.getSerializableExtra("vocabularies");
        }

        cardFront = findViewById(R.id.cardFront);
        cardBack = findViewById(R.id.cardBack);

        cardFront.setCameraDistance(8000 * scale);
        cardBack.setCameraDistance(8000 * scale);

        frontAnim = (AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.front_animator);
        backAnim = (AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.back_animator);
    }

    private void setupAnimations() {
        // Additional setup for animations if needed
    }

    private void setOnClickListener() {
        Button flipBtn = findViewById(R.id.btn_flipCard);
        Button nextVocab = findViewById(R.id.btn_nextVocab);

        flipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFront) {
                    startAnimations(frontAnim, backAnim);
                    isFront = false;
                } else {
                    startAnimations(backAnim, frontAnim);
                    isFront = true;
                }
            }
        });

        nextVocab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentVocabularyIndex++;
                if (currentVocabularyIndex >= vocabularyArrayList.size()) {
                    currentVocabularyIndex = 0; // Reset to the first vocabulary
                }
                setDataToViews(currentVocabularyIndex);
            }
        });
    }

    private void startAnimations(AnimatorSet first, AnimatorSet second) {
        if (!first.isRunning() && !second.isRunning()) {
            first.setTarget(findViewById(R.id.cardFront));
            second.setTarget(findViewById(R.id.cardBack));
            first.start();
            second.start();
        }
    }

    private void setDataToViews(int currentPosition) {
        // Ensure that currentVocabularyIndex is within bounds
        if (currentVocabularyIndex < 0 || currentVocabularyIndex >= vocabularyArrayList.size()) {
            // Handle the case where the index is out of bounds
            return;
        }

        Vocabulary currentVocabulary = vocabularyArrayList.get(currentPosition);
        cardFront.setText(currentVocabulary.getWord());
        cardBack.setText(currentVocabulary.getMeaning());
    }
}

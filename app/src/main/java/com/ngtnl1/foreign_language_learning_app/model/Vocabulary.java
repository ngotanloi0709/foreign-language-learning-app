package com.ngtnl1.foreign_language_learning_app.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Vocabulary implements Serializable {
    private String topicId;
    private String word;
    private String meaning;
    private String phonetic;
    private String type;
    private String description;
    private String example;
    private String frontText;
    private String backText;
    private boolean isNoted;
}



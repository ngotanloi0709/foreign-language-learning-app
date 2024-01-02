package com.ngtnl1.foreign_language_learning_app.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Definition {
    String definition = "";
    String example = "";
    List<String> synonyms = null;
    List<String> antonyms = null;
}

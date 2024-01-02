package com.ngtnl1.foreign_language_learning_app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Ranking {
    private int id;
    private int userId;
    private int score;
}

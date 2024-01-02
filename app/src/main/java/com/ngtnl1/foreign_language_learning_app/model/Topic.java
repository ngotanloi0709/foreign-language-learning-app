package com.ngtnl1.foreign_language_learning_app.model;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Topic implements Comparable<Topic>{
    private String id;
    private String userId;
    private String name;
    private String description;
    private boolean isPublic;
    private int views = 0;
    private List<Vocabulary> vocabularies;

    public Topic(String userId, String name, String description, boolean isPublic, List<Vocabulary> vocabularies) {
        this.userId = userId;
        this.name = name;
        this.description = String.valueOf(description);
        this.isPublic = isPublic;
        this.vocabularies = vocabularies;
    }

    public void incrementViews() {
        this.views++;
    }

    @Override
    public int compareTo(Topic other) {
        return Integer.compare(other.views, this.views);
    }
}

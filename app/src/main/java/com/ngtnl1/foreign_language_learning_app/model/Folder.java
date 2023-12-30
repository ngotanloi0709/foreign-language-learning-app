package com.ngtnl1.foreign_language_learning_app.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Folder {
    private String id;
    private String userId;
    private String name;
    private List<String> topics;
}

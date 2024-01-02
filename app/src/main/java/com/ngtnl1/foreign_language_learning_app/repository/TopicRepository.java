package com.ngtnl1.foreign_language_learning_app.repository;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ngtnl1.foreign_language_learning_app.model.Topic;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.Getter;

@Singleton
@Getter
public class TopicRepository extends BaseRepository<Topic>{
    @Inject
    public TopicRepository() {
        super("topics");
    }

    public Task<QuerySnapshot> getTopicsByUserId(String userId) {
        return collectionReference.whereEqualTo("userId", userId).get();
    }

    public Task<List<Topic>> findTopicsById(List<String> topicIds) {
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String id : topicIds) {
            tasks.add(collectionReference.document(id).get());
        }

        return Tasks.whenAllSuccess(tasks).continueWith(task -> {
            List<Topic> topics = new ArrayList<>();
            Topic topic;
            for (Task<DocumentSnapshot> singleTask : tasks) {
                try {
                    topic = singleTask.getResult().toObject(Topic.class);
                    topic.setId(singleTask.getResult().getId());
                    topics.add(topic);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return topics;
        });
    }

    public Task<List<Topic>> findTopicsByPublic() {
        return collectionReference.whereEqualTo("public", true).get().continueWith(task -> {
            List<Topic> topics = new ArrayList<>();
            Topic topic;
            for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                topic = documentSnapshot.toObject(Topic.class);
                topic.setId(documentSnapshot.getId());
                topics.add(topic);
            }
            return topics;
        });
    }
}

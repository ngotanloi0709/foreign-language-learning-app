package com.ngtnl1.foreign_language_learning_app.service;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ngtnl1.foreign_language_learning_app.model.Topic;
import com.ngtnl1.foreign_language_learning_app.repository.TopicRepository;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TopicService {
    TopicRepository topicRepository;

    @Inject
    public TopicService(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    public Task<DocumentReference> create(Topic topic) {
        return topicRepository.create(topic);
    }

    public Task<QuerySnapshot> getTopicsByUserId(String userId) {
        return topicRepository.getTopicsByUserId(userId);
    }

    public Task<Void> update(String id, Topic topic) {
        return topicRepository.update(id, topic);
    }

    public Task<Void> remove(String id) {
        return topicRepository.remove(id);
    }

    public Task<List<Topic>> findTopicsById(List<String> topicIds) {
        return topicRepository.findTopicsById(topicIds);
    }

    public Task<List<Topic>> findTopicsByPublic() {
        return topicRepository.findTopicsByPublic();
    }

    public Task<DocumentSnapshot> getTopicById(String id) {
        return topicRepository.find(id);
    }
}

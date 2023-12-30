package com.ngtnl1.foreign_language_learning_app.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class BaseRepository<T> {

    protected String collectionName;
    protected FirebaseFirestore db;
    protected CollectionReference collectionReference;


    public BaseRepository(String collectionName) {
        this.collectionName = collectionName;
        this.db = FirebaseFirestore.getInstance();
        this.collectionReference = db.collection(collectionName);

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
    }

    public Task<DocumentReference> create(T item) {
        return collectionReference.add(item);
    }

    public Task<Void> update(String id, T item) {
        return collectionReference.document(id).set(item);
    }

    public Task<Void> remove(String id) {
        return collectionReference.document(id).delete();
    }

    public Task<QuerySnapshot> findAll() {
        return collectionReference.get();
    }

    public Task<DocumentSnapshot> find(String id) {
        return collectionReference.document(id).get();
    }
}
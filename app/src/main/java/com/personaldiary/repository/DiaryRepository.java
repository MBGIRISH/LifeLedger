package com.personaldiary.repository;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.personaldiary.firebase.FirebaseConfig;
import com.personaldiary.models.Attachment;
import com.personaldiary.utils.Constants;
import com.personaldiary.utils.DateUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiaryRepository {

    private final FirebaseFirestore db;
    private final StorageReference storageRef;
    private final String userId;

    public DiaryRepository(String userId) {
        this.db = FirebaseConfig.getDb();
        this.storageRef = FirebaseStorage.getInstance().getReference();
        this.userId = userId;
    }

    private com.google.firebase.firestore.CollectionReference getNotesCollection() {
        return db.collection(Constants.COLLECTION_DIARY_NOTES)
                .document(userId)
                .collection(Constants.SUBCOLLECTION_NOTES);
    }

    public Task<DocumentSnapshot> getNote(String date) {
        return getNotesCollection().document(date).get();
    }

    public Task<Void> saveNote(String date, String content, String tag,
                               List<Attachment> attachments) {
        return getNote(date).continueWithTask(task -> {
            Map<String, Object> noteData = new HashMap<>();
            noteData.put(Constants.FIELD_CONTENT, content);
            noteData.put(Constants.FIELD_TAG, tag);
            noteData.put(Constants.FIELD_DATE, date);
            noteData.put(Constants.FIELD_UPDATED_AT, Timestamp.now());

            if (attachments != null) {
                List<Map<String, String>> attachList = new ArrayList<>();
                for (Attachment a : attachments) {
                    Map<String, String> map = new HashMap<>();
                    map.put("url", a.getUrl());
                    map.put("name", a.getName());
                    map.put("type", a.getType());
                    attachList.add(map);
                }
                noteData.put(Constants.FIELD_ATTACHMENTS, attachList);
            }

            if (task.isSuccessful() && task.getResult() != null
                    && task.getResult().exists()) {
                return getNotesCollection().document(date).update(noteData);
            } else {
                noteData.put(Constants.FIELD_CREATED_AT, Timestamp.now());
                return getNotesCollection().document(date).set(noteData);
            }
        });
    }

    public Task<Uri> uploadAttachment(String date, String fileName, Uri fileUri) {
        String path = "attachments/" + userId + "/" + date + "/"
                + System.currentTimeMillis() + "_" + fileName;
        StorageReference fileRef = storageRef.child(path);
        return fileRef.putFile(fileUri).continueWithTask(task -> {
            if (!task.isSuccessful() && task.getException() != null) {
                throw task.getException();
            }
            return fileRef.getDownloadUrl();
        });
    }

    public Task<QuerySnapshot> getNotesByMonth(int year, int month) {
        String startDate = DateUtils.getMonthStartDate(year, month);
        String endDate = DateUtils.getMonthEndDateExclusive(year, month);

        return getNotesCollection()
                .whereGreaterThanOrEqualTo(Constants.FIELD_DATE, startDate)
                .whereLessThan(Constants.FIELD_DATE, endDate)
                .orderBy(Constants.FIELD_DATE, Query.Direction.ASCENDING)
                .get();
    }

    public Task<QuerySnapshot> getNotesByTag(String tag) {
        return getNotesCollection()
                .whereEqualTo(Constants.FIELD_TAG, tag)
                .get();
    }
}

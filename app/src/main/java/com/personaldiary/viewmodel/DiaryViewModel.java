package com.personaldiary.viewmodel;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.personaldiary.firebase.FirebaseConfig;
import com.personaldiary.models.Attachment;
import com.personaldiary.models.DiaryNote;
import com.personaldiary.repository.DiaryRepository;
import com.personaldiary.utils.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiaryViewModel extends ViewModel {

    private DiaryRepository repository;

    private final MutableLiveData<DiaryNote> currentNote = new MutableLiveData<>();
    private final MutableLiveData<DiaryNote> memoryNote = new MutableLiveData<>();
    private final MutableLiveData<List<DiaryNote>> notesLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> uploadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Attachment> newAttachment = new MutableLiveData<>();

    public DiaryViewModel() {
        FirebaseUser user = FirebaseConfig.getAuth().getCurrentUser();
        if (user != null) {
            repository = new DiaryRepository(user.getUid());
        }
    }

    public LiveData<DiaryNote> getCurrentNote() { return currentNote; }
    public LiveData<DiaryNote> getMemoryNote() { return memoryNote; }
    public LiveData<List<DiaryNote>> getNotesLiveData() { return notesLiveData; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }
    public LiveData<Boolean> getLoadingLiveData() { return loadingLiveData; }
    public LiveData<String> getSuccessMessage() { return successMessage; }
    public LiveData<Boolean> getUploadingLiveData() { return uploadingLiveData; }
    public LiveData<Attachment> getNewAttachment() { return newAttachment; }

    private boolean ensureRepository() {
        if (repository != null) return true;
        FirebaseUser user = FirebaseConfig.getAuth().getCurrentUser();
        if (user != null) {
            repository = new DiaryRepository(user.getUid());
            return true;
        }
        errorLiveData.setValue("User not authenticated.");
        return false;
    }

    public void loadNoteByDate(String date) {
        if (!ensureRepository()) return;

        loadingLiveData.setValue(true);
        repository.getNote(date)
                .addOnSuccessListener(documentSnapshot -> {
                    loadingLiveData.setValue(false);
                    if (documentSnapshot.exists()) {
                        DiaryNote note = documentSnapshot.toObject(DiaryNote.class);
                        currentNote.setValue(note);
                    } else {
                        currentNote.setValue(null);
                    }
                })
                .addOnFailureListener(e -> {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(e.getMessage());
                });
    }

    public void loadMemoryNote(String currentDate) {
        if (!ensureRepository()) return;

        String oneYearAgo = DateUtils.getOneYearAgoDate(currentDate);
        if (oneYearAgo == null) {
            memoryNote.setValue(null);
            return;
        }

        repository.getNote(oneYearAgo)
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        DiaryNote note = documentSnapshot.toObject(DiaryNote.class);
                        memoryNote.setValue(note);
                    } else {
                        memoryNote.setValue(null);
                    }
                })
                .addOnFailureListener(e -> memoryNote.setValue(null));
    }

    public void saveNote(String date, String content, String tag,
                         List<Attachment> attachments) {
        if (!ensureRepository()) return;

        loadingLiveData.setValue(true);
        repository.saveNote(date, content, tag, attachments)
                .addOnSuccessListener(unused -> {
                    loadingLiveData.setValue(false);
                    successMessage.setValue("Diary note saved successfully!");
                    loadNoteByDate(date);
                })
                .addOnFailureListener(e -> {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(e.getMessage());
                });
    }

    public void uploadAttachment(String date, String fileName, String type, Uri fileUri) {
        if (!ensureRepository()) return;

        uploadingLiveData.setValue(true);
        repository.uploadAttachment(date, fileName, fileUri)
                .addOnSuccessListener(downloadUrl -> {
                    uploadingLiveData.setValue(false);
                    Attachment attachment = new Attachment(
                            downloadUrl.toString(), fileName, type);
                    newAttachment.setValue(attachment);
                })
                .addOnFailureListener(e -> {
                    uploadingLiveData.setValue(false);
                    errorLiveData.setValue("Upload failed: " + e.getMessage());
                });
    }

    public void searchByMonth(int year, int month) {
        if (!ensureRepository()) return;

        loadingLiveData.setValue(true);
        repository.getNotesByMonth(year, month)
                .addOnSuccessListener(querySnapshot -> {
                    loadingLiveData.setValue(false);
                    notesLiveData.setValue(parseNotes(querySnapshot));
                })
                .addOnFailureListener(e -> {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(e.getMessage());
                });
    }

    public void searchByTag(String tag) {
        if (!ensureRepository()) return;

        loadingLiveData.setValue(true);
        repository.getNotesByTag(tag)
                .addOnSuccessListener(querySnapshot -> {
                    loadingLiveData.setValue(false);
                    List<DiaryNote> notes = parseNotes(querySnapshot);
                    Collections.sort(notes, (a, b) -> b.getDate().compareTo(a.getDate()));
                    notesLiveData.setValue(notes);
                })
                .addOnFailureListener(e -> {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(e.getMessage());
                });
    }

    private List<DiaryNote> parseNotes(QuerySnapshot querySnapshot) {
        List<DiaryNote> notes = new ArrayList<>();
        if (querySnapshot != null) {
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                DiaryNote note = doc.toObject(DiaryNote.class);
                if (note != null) {
                    notes.add(note);
                }
            }
        }
        return notes;
    }
}

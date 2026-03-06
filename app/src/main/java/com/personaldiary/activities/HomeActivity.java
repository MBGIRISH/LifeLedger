package com.personaldiary.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.personaldiary.R;
import com.personaldiary.adapters.AttachmentPreviewAdapter;
import com.personaldiary.databinding.ActivityHomeBinding;
import com.personaldiary.models.Attachment;
import com.personaldiary.models.DiaryNote;
import com.personaldiary.utils.Constants;
import com.personaldiary.utils.DateUtils;
import com.personaldiary.viewmodel.AuthViewModel;
import com.personaldiary.viewmodel.DiaryViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private DiaryViewModel diaryViewModel;
    private AuthViewModel authViewModel;
    private String selectedDate;
    private final List<Attachment> currentAttachments = new ArrayList<>();
    private String pendingAttachType;
    private AttachmentPreviewAdapter attachmentPreviewAdapter;
    private AttachmentPreviewAdapter memoryAttachmentPreviewAdapter;

    private final ActivityResultLauncher<String> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null && pendingAttachType != null) {
                    String fileName = getFileName(uri);
                    diaryViewModel.uploadAttachment(
                            selectedDate, fileName, pendingAttachType, uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        diaryViewModel = new ViewModelProvider(this).get(DiaryViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        selectedDate = DateUtils.getTodayDate();
        updateDateDisplay();

        setupToolbar();
        setupAttachmentPreviews();
        setupObservers();
        setupListeners();

        diaryViewModel.loadNoteByDate(selectedDate);
        diaryViewModel.loadMemoryNote(selectedDate);
    }

    private void setupAttachmentPreviews() {
        attachmentPreviewAdapter = new AttachmentPreviewAdapter();
        binding.rvAttachments.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvAttachments.setAdapter(attachmentPreviewAdapter);

        memoryAttachmentPreviewAdapter = new AttachmentPreviewAdapter();
        binding.rvMemoryAttachments.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvMemoryAttachments.setAdapter(memoryAttachmentPreviewAdapter);
    }

    private void updateDateDisplay() {
        boolean isToday = selectedDate.equals(DateUtils.getTodayDate());
        binding.tvDate.setText(DateUtils.formatDisplayDate(selectedDate));

        if (isToday) {
            binding.tvDiaryTitle.setText(R.string.todays_diary);
            binding.btnToday.setVisibility(View.GONE);
        } else {
            binding.tvDiaryTitle.setText(getString(R.string.diary_for_date,
                    DateUtils.formatShortDate(selectedDate)));
            binding.btnToday.setVisibility(View.VISIBLE);
        }
    }

    private void setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_home) {
                switchToToday();
                return true;
            } else if (id == R.id.menu_search) {
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            } else if (id == R.id.menu_logout) {
                performLogout();
                return true;
            }
            return false;
        });
    }

    private void setupObservers() {
        diaryViewModel.getCurrentNote().observe(this, this::populateNote);

        diaryViewModel.getMemoryNote().observe(this, this::showMemoryCard);

        diaryViewModel.getErrorLiveData().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
            }
        });

        diaryViewModel.getLoadingLiveData().observe(this, loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnSave.setEnabled(!loading);
        });

        diaryViewModel.getSuccessMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_LONG).show();
            }
        });

        diaryViewModel.getUploadingLiveData().observe(this, uploading -> {
            binding.progressUpload.setVisibility(uploading ? View.VISIBLE : View.GONE);
            binding.btnAttach.setEnabled(!uploading);
        });

        diaryViewModel.getNewAttachment().observe(this, attachment -> {
            if (attachment != null) {
                currentAttachments.add(attachment);
                refreshAttachmentPreviews();
                Snackbar.make(binding.getRoot(),
                        R.string.attachment_uploaded, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void showMemoryCard(DiaryNote memoryNote) {
        if (memoryNote == null) {
            binding.cardMemory.setVisibility(View.GONE);
            return;
        }
        binding.cardMemory.setVisibility(View.VISIBLE);
        binding.tvMemoryDate.setText(DateUtils.formatShortDate(memoryNote.getDate()));

        String content = memoryNote.getContent();
        if (content != null && !content.isEmpty()) {
            binding.tvMemoryContent.setText(content);
            binding.tvMemoryContent.setVisibility(View.VISIBLE);
        } else {
            binding.tvMemoryContent.setVisibility(View.GONE);
        }

        String tag = memoryNote.getTag();
        if (tag != null && !tag.isEmpty()) {
            binding.chipMemoryTag.setText(tag);
            binding.chipMemoryTag.setVisibility(View.VISIBLE);
        } else {
            binding.chipMemoryTag.setVisibility(View.GONE);
        }

        if (memoryNote.hasAttachments()) {
            int count = memoryNote.getAttachments().size();
            binding.tvMemoryAttachments.setText(
                    getString(R.string.attachment_count, count));
            binding.tvMemoryAttachments.setVisibility(View.VISIBLE);

            binding.rvMemoryAttachments.setVisibility(View.VISIBLE);
            memoryAttachmentPreviewAdapter.setAttachments(memoryNote.getAttachments());
        } else {
            binding.tvMemoryAttachments.setVisibility(View.GONE);
            binding.rvMemoryAttachments.setVisibility(View.GONE);
            memoryAttachmentPreviewAdapter.setAttachments(null);
        }
    }

    private void populateNote(DiaryNote note) {
        if (note != null) {
            binding.etNote.setText(note.getContent());
            selectTag(note.getTag());
            showTimestamps(note);

            currentAttachments.clear();
            if (note.hasAttachments()) {
                currentAttachments.addAll(note.getAttachments());
            }
            refreshAttachmentPreviews();
        } else {
            binding.etNote.setText("");
            binding.chipNormal.setChecked(true);
            binding.tvTimestamps.setVisibility(View.GONE);
            currentAttachments.clear();
            refreshAttachmentPreviews();
        }
    }

    private void refreshAttachmentPreviews() {
        if (currentAttachments.isEmpty()) {
            binding.rvAttachments.setVisibility(View.GONE);
            binding.tvAttachCount.setVisibility(View.GONE);
            attachmentPreviewAdapter.setAttachments(null);
            return;
        }

        binding.rvAttachments.setVisibility(View.VISIBLE);
        binding.tvAttachCount.setText(
                getString(R.string.attachment_count, currentAttachments.size()));
        binding.tvAttachCount.setVisibility(View.VISIBLE);
        attachmentPreviewAdapter.setAttachments(currentAttachments);
    }

    private void selectTag(String tag) {
        if (tag == null) {
            binding.chipNormal.setChecked(true);
            return;
        }
        switch (tag) {
            case Constants.TAG_SPECIAL:
                binding.chipSpecial.setChecked(true);
                break;
            case Constants.TAG_IMPORTANT:
                binding.chipImportant.setChecked(true);
                break;
            case Constants.TAG_BAD_NEWS:
                binding.chipBadNews.setChecked(true);
                break;
            default:
                binding.chipNormal.setChecked(true);
                break;
        }
    }

    private void showTimestamps(DiaryNote note) {
        StringBuilder sb = new StringBuilder();
        if (note.getCreatedAt() != null) {
            sb.append("Created: ").append(DateUtils.formatTimestamp(note.getCreatedAt()));
        }
        if (note.getUpdatedAt() != null) {
            if (sb.length() > 0) sb.append("\n");
            sb.append("Updated: ").append(DateUtils.formatTimestamp(note.getUpdatedAt()));
        }
        if (sb.length() > 0) {
            binding.tvTimestamps.setText(sb.toString());
            binding.tvTimestamps.setVisibility(View.VISIBLE);
        }
    }

    private void setupListeners() {
        binding.btnSave.setOnClickListener(v -> saveNote());
        binding.btnChangeDate.setOnClickListener(v -> showDatePicker());
        binding.btnToday.setOnClickListener(v -> switchToToday());
        binding.btnAttach.setOnClickListener(v -> showAttachmentPicker());
    }

    private void showAttachmentPicker() {
        String[] items = {
                getString(R.string.attach_image),
                getString(R.string.attach_document),
                getString(R.string.attach_audio),
                getString(R.string.attach_video)
        };
        int[] icons = {
                R.drawable.ic_image,
                R.drawable.ic_document,
                R.drawable.ic_audio,
                R.drawable.ic_video
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.choose_attachment)
                .setItems(items, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            pendingAttachType = Constants.ATTACH_TYPE_IMAGE;
                            filePickerLauncher.launch("image/*");
                            break;
                        case 1:
                            pendingAttachType = Constants.ATTACH_TYPE_DOCUMENT;
                            filePickerLauncher.launch("application/*");
                            break;
                        case 2:
                            pendingAttachType = Constants.ATTACH_TYPE_AUDIO;
                            filePickerLauncher.launch("audio/*");
                            break;
                        case 3:
                            pendingAttachType = Constants.ATTACH_TYPE_VIDEO;
                            filePickerLauncher.launch("video/*");
                            break;
                    }
                })
                .show();
    }

    private String getFileName(Uri uri) {
        String name = "attachment";
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) {
                    name = cursor.getString(idx);
                }
            }
        } catch (Exception ignored) {}
        return name;
    }

    private void showDatePicker() {
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())
                .build();

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.select_date))
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(constraints)
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            selectedDate = DateUtils.formatStorageDate(selection);
            updateDateDisplay();
            currentAttachments.clear();
            refreshAttachmentPreviews();
            diaryViewModel.loadNoteByDate(selectedDate);
            diaryViewModel.loadMemoryNote(selectedDate);
        });

        picker.show(getSupportFragmentManager(), "HOME_DATE_PICKER");
    }

    private void switchToToday() {
        selectedDate = DateUtils.getTodayDate();
        updateDateDisplay();
        currentAttachments.clear();
        refreshAttachmentPreviews();
        diaryViewModel.loadNoteByDate(selectedDate);
        diaryViewModel.loadMemoryNote(selectedDate);
    }

    private void saveNote() {
        String content = binding.etNote.getText() != null
                ? binding.etNote.getText().toString().trim() : "";

        String tag = getSelectedTag();
        diaryViewModel.saveNote(selectedDate, content, tag,
                currentAttachments.isEmpty() ? null : new ArrayList<>(currentAttachments));
    }

    private String getSelectedTag() {
        int checkedId = binding.chipGroupTags.getCheckedChipId();
        if (checkedId == R.id.chipSpecial) return Constants.TAG_SPECIAL;
        if (checkedId == R.id.chipImportant) return Constants.TAG_IMPORTANT;
        if (checkedId == R.id.chipBadNews) return Constants.TAG_BAD_NEWS;
        return Constants.TAG_NORMAL;
    }

    private void performLogout() {
        authViewModel.logout();
        startActivity(new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}

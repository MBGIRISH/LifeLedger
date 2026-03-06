package com.personaldiary.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.personaldiary.R;
import com.personaldiary.adapters.AttachmentPreviewAdapter;
import com.personaldiary.databinding.ActivitySearchBinding;
import com.personaldiary.models.DiaryNote;
import com.personaldiary.utils.Constants;
import com.personaldiary.utils.DateUtils;
import com.personaldiary.viewmodel.AuthViewModel;
import com.personaldiary.viewmodel.DiaryViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private DiaryViewModel diaryViewModel;
    private AuthViewModel authViewModel;
    private String selectedDate;
    private AttachmentPreviewAdapter dateAttachmentsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        diaryViewModel = new ViewModelProvider(this).get(DiaryViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupToolbar();
        setupSearchModeChips();
        setupDateAttachmentsPreview();
        setupDateSearch();
        setupMonthSearch();
        setupTagSearch();
        setupObservers();
    }

    private void setupDateAttachmentsPreview() {
        dateAttachmentsAdapter = new AttachmentPreviewAdapter();
        binding.rvDateAttachments.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvDateAttachments.setAdapter(dateAttachmentsAdapter);
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_home) {
                startActivity(new Intent(this, HomeActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
                return true;
            } else if (id == R.id.menu_search) {
                return true;
            } else if (id == R.id.menu_logout) {
                authViewModel.logout();
                startActivity(new Intent(this, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
                return true;
            }
            return false;
        });
    }

    private void setupSearchModeChips() {
        binding.chipGroupSearchMode.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);

            binding.cardDateSearch.setVisibility(
                    checkedId == R.id.chipByDate ? View.VISIBLE : View.GONE);
            binding.cardMonthSearch.setVisibility(
                    checkedId == R.id.chipByMonth ? View.VISIBLE : View.GONE);
            binding.cardTagSearch.setVisibility(
                    checkedId == R.id.chipByTag ? View.VISIBLE : View.GONE);

            resetDateSearchResults();
        });
    }

    // ===================== DATE SEARCH =====================

    private void setupDateSearch() {
        binding.btnPickDate.setOnClickListener(v -> {
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
                binding.tvSelectedDate.setText(DateUtils.formatDisplayDate(selectedDate));
                binding.tvSelectedDate.setVisibility(View.VISIBLE);
                diaryViewModel.loadNoteByDate(selectedDate);
            });

            picker.show(getSupportFragmentManager(), "DATE_PICKER");
        });

        binding.btnSaveDateNote.setOnClickListener(v -> saveDateNote());
    }

    private void resetDateSearchResults() {
        binding.tvSelectedDate.setVisibility(View.GONE);
        binding.tilDateNote.setVisibility(View.GONE);
        binding.rvDateAttachments.setVisibility(View.GONE);
        binding.tvDateTagLabel.setVisibility(View.GONE);
        binding.chipGroupDateTags.setVisibility(View.GONE);
        binding.btnSaveDateNote.setVisibility(View.GONE);
        binding.tvNoDateNote.setVisibility(View.GONE);
        if (dateAttachmentsAdapter != null) dateAttachmentsAdapter.setAttachments(null);
    }

    private void showDateNoteResult(DiaryNote note) {
        binding.tilDateNote.setVisibility(View.VISIBLE);
        binding.tvDateTagLabel.setVisibility(View.VISIBLE);
        binding.chipGroupDateTags.setVisibility(View.VISIBLE);
        binding.btnSaveDateNote.setVisibility(View.VISIBLE);
        binding.tvNoDateNote.setVisibility(View.GONE);

        if (note != null) {
            binding.etDateNote.setText(note.getContent());
            selectDateTag(note.getTag());

            if (note.hasAttachments()) {
                binding.rvDateAttachments.setVisibility(View.VISIBLE);
                dateAttachmentsAdapter.setAttachments(note.getAttachments());
            } else {
                binding.rvDateAttachments.setVisibility(View.GONE);
                dateAttachmentsAdapter.setAttachments(null);
            }
        } else {
            binding.etDateNote.setText("");
            binding.chipDateNormal.setChecked(true);
            binding.tvNoDateNote.setVisibility(View.VISIBLE);
            binding.rvDateAttachments.setVisibility(View.GONE);
            dateAttachmentsAdapter.setAttachments(null);
        }
    }

    private void selectDateTag(String tag) {
        if (tag == null) {
            binding.chipDateNormal.setChecked(true);
            return;
        }
        switch (tag) {
            case Constants.TAG_SPECIAL:
                binding.chipDateSpecial.setChecked(true);
                break;
            case Constants.TAG_IMPORTANT:
                binding.chipDateImportant.setChecked(true);
                break;
            case Constants.TAG_BAD_NEWS:
                binding.chipDateBadNews.setChecked(true);
                break;
            default:
                binding.chipDateNormal.setChecked(true);
                break;
        }
    }

    private String getSelectedDateTag() {
        int checkedId = binding.chipGroupDateTags.getCheckedChipId();
        if (checkedId == R.id.chipDateSpecial) return Constants.TAG_SPECIAL;
        if (checkedId == R.id.chipDateImportant) return Constants.TAG_IMPORTANT;
        if (checkedId == R.id.chipDateBadNews) return Constants.TAG_BAD_NEWS;
        return Constants.TAG_NORMAL;
    }

    private void saveDateNote() {
        if (selectedDate == null) return;

        String content = binding.etDateNote.getText() != null
                ? binding.etDateNote.getText().toString().trim() : "";

        String tag = getSelectedDateTag();
        diaryViewModel.saveNote(selectedDate, content, tag, null);
    }

    // ===================== MONTH SEARCH =====================

    private void setupMonthSearch() {
        String[] months = getResources().getStringArray(R.array.months);
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, months);
        binding.actvMonth.setAdapter(monthAdapter);

        List<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int y = currentYear; y >= currentYear - 10; y--) {
            years.add(String.valueOf(y));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, years);
        binding.actvYear.setAdapter(yearAdapter);

        binding.btnSearchMonth.setOnClickListener(v -> performMonthSearch(months));
    }

    private void performMonthSearch(String[] months) {
        String monthStr = binding.actvMonth.getText().toString();
        String yearStr = binding.actvYear.getText().toString();

        if (monthStr.isEmpty() || yearStr.isEmpty()) {
            Snackbar.make(binding.getRoot(),
                    "Please select both month and year", Snackbar.LENGTH_SHORT).show();
            return;
        }

        int month = -1;
        for (int i = 0; i < months.length; i++) {
            if (months[i].equals(monthStr)) {
                month = i + 1;
                break;
            }
        }

        if (month == -1) return;

        int year = Integer.parseInt(yearStr);

        Intent intent = new Intent(this, MonthNotesActivity.class);
        intent.putExtra(Constants.EXTRA_SEARCH_TYPE, Constants.SEARCH_TYPE_MONTH);
        intent.putExtra(Constants.EXTRA_MONTH, month);
        intent.putExtra(Constants.EXTRA_YEAR, year);
        startActivity(intent);
    }

    // ===================== TAG SEARCH =====================

    private void setupTagSearch() {
        binding.btnSearchTag.setOnClickListener(v -> {
            int checkedId = binding.chipGroupTagSearch.getCheckedChipId();
            String tag = null;

            if (checkedId == R.id.chipSearchSpecial) {
                tag = Constants.TAG_SPECIAL;
            } else if (checkedId == R.id.chipSearchImportant) {
                tag = Constants.TAG_IMPORTANT;
            } else if (checkedId == R.id.chipSearchBadNews) {
                tag = Constants.TAG_BAD_NEWS;
            }

            if (tag == null) {
                Snackbar.make(binding.getRoot(),
                        "Please select a tag", Snackbar.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, MonthNotesActivity.class);
            intent.putExtra(Constants.EXTRA_SEARCH_TYPE, Constants.SEARCH_TYPE_TAG);
            intent.putExtra(Constants.EXTRA_TAG, tag);
            startActivity(intent);
        });
    }

    // ===================== OBSERVERS =====================

    private void setupObservers() {
        diaryViewModel.getCurrentNote().observe(this, this::showDateNoteResult);

        diaryViewModel.getErrorLiveData().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
            }
        });

        diaryViewModel.getLoadingLiveData().observe(this, loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        diaryViewModel.getSuccessMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_LONG).show();
            }
        });
    }
}

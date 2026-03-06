package com.personaldiary.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.personaldiary.adapters.DiaryNoteAdapter;
import com.personaldiary.databinding.ActivityMonthNotesBinding;
import com.personaldiary.utils.Constants;
import com.personaldiary.viewmodel.DiaryViewModel;

public class MonthNotesActivity extends AppCompatActivity {

    private ActivityMonthNotesBinding binding;
    private DiaryViewModel viewModel;
    private DiaryNoteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMonthNotesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(DiaryViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupObservers();
        loadData();
    }

    private void setupToolbar() {
        String searchType = getIntent().getStringExtra(Constants.EXTRA_SEARCH_TYPE);

        if (Constants.SEARCH_TYPE_MONTH.equals(searchType)) {
            int month = getIntent().getIntExtra(Constants.EXTRA_MONTH, 1);
            int year = getIntent().getIntExtra(Constants.EXTRA_YEAR, 2026);
            String[] months = getResources().getStringArray(
                    com.personaldiary.R.array.months);
            String title = months[month - 1] + " " + year;
            binding.toolbar.setTitle(title);
        } else if (Constants.SEARCH_TYPE_TAG.equals(searchType)) {
            String tag = getIntent().getStringExtra(Constants.EXTRA_TAG);
            binding.toolbar.setTitle("Tag: " + (tag != null ? tag : ""));
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new DiaryNoteAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getNotesLiveData().observe(this, notes -> {
            if (notes != null && !notes.isEmpty()) {
                adapter.setNotes(notes);
                binding.recyclerView.setVisibility(View.VISIBLE);
                binding.tvEmpty.setVisibility(View.GONE);
            } else {
                binding.recyclerView.setVisibility(View.GONE);
                binding.tvEmpty.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getErrorLiveData().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.getLoadingLiveData().observe(this, loading ->
                binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));
    }

    private void loadData() {
        String searchType = getIntent().getStringExtra(Constants.EXTRA_SEARCH_TYPE);

        if (Constants.SEARCH_TYPE_MONTH.equals(searchType)) {
            int month = getIntent().getIntExtra(Constants.EXTRA_MONTH, 1);
            int year = getIntent().getIntExtra(Constants.EXTRA_YEAR, 2026);
            viewModel.searchByMonth(year, month);
        } else if (Constants.SEARCH_TYPE_TAG.equals(searchType)) {
            String tag = getIntent().getStringExtra(Constants.EXTRA_TAG);
            if (tag != null) {
                viewModel.searchByTag(tag);
            }
        }
    }
}

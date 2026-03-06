package com.personaldiary.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.personaldiary.databinding.ItemDiaryNoteBinding;
import com.personaldiary.models.DiaryNote;
import com.personaldiary.utils.Constants;
import com.personaldiary.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class DiaryNoteAdapter extends RecyclerView.Adapter<DiaryNoteAdapter.NoteViewHolder> {

    private List<DiaryNote> notes = new ArrayList<>();

    public void setNotes(List<DiaryNote> notes) {
        this.notes = notes != null ? notes : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDiaryNoteBinding binding = ItemDiaryNoteBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new NoteViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.bind(notes.get(position));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {

        private final ItemDiaryNoteBinding binding;
        private final AttachmentPreviewAdapter attachmentPreviewAdapter;

        NoteViewHolder(@NonNull ItemDiaryNoteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            attachmentPreviewAdapter = new AttachmentPreviewAdapter();
            this.binding.rvAttachments.setLayoutManager(
                    new LinearLayoutManager(binding.getRoot().getContext(),
                            LinearLayoutManager.HORIZONTAL, false));
            this.binding.rvAttachments.setAdapter(attachmentPreviewAdapter);
        }

        void bind(DiaryNote note) {
            binding.tvDate.setText(DateUtils.formatShortDate(note.getDate()));
            binding.tvContent.setText(note.getContent());

            String tag = note.getTag();
            if (tag != null && !tag.isEmpty()) {
                binding.chipTag.setText(tag);
                binding.chipTag.setVisibility(android.view.View.VISIBLE);
                applyTagStyle(tag);
            } else {
                binding.chipTag.setVisibility(android.view.View.GONE);
            }

            if (note.getUpdatedAt() != null) {
                binding.tvTimestamp.setText(
                        "Updated: " + DateUtils.formatTimestamp(note.getUpdatedAt()));
                binding.tvTimestamp.setVisibility(android.view.View.VISIBLE);
            } else if (note.getCreatedAt() != null) {
                binding.tvTimestamp.setText(
                        "Created: " + DateUtils.formatTimestamp(note.getCreatedAt()));
                binding.tvTimestamp.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.tvTimestamp.setVisibility(android.view.View.GONE);
            }

            if (note.hasAttachments()) {
                int count = note.getAttachments().size();
                binding.tvAttachmentCount.setText(count + " file" + (count > 1 ? "s" : ""));
                binding.tvAttachmentCount.setVisibility(android.view.View.VISIBLE);
                binding.rvAttachments.setVisibility(android.view.View.VISIBLE);
                attachmentPreviewAdapter.setAttachments(note.getAttachments());
            } else {
                binding.tvAttachmentCount.setVisibility(android.view.View.GONE);
                binding.rvAttachments.setVisibility(android.view.View.GONE);
                attachmentPreviewAdapter.setAttachments(null);
            }
        }

        private void applyTagStyle(String tag) {
            int colorRes;
            switch (tag) {
                case Constants.TAG_SPECIAL:
                    colorRes = com.personaldiary.R.color.tag_special;
                    break;
                case Constants.TAG_IMPORTANT:
                    colorRes = com.personaldiary.R.color.tag_important;
                    break;
                case Constants.TAG_BAD_NEWS:
                    colorRes = com.personaldiary.R.color.tag_bad_news;
                    break;
                default:
                    colorRes = com.personaldiary.R.color.tag_normal;
                    break;
            }
            binding.chipTag.setChipBackgroundColorResource(colorRes);
        }
    }
}

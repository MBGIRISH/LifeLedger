package com.personaldiary.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.personaldiary.R;
import com.personaldiary.databinding.ItemAttachmentPreviewBinding;
import com.personaldiary.models.Attachment;
import com.personaldiary.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class AttachmentPreviewAdapter extends RecyclerView.Adapter<AttachmentPreviewAdapter.AttachmentViewHolder> {

    private final List<Attachment> attachments = new ArrayList<>();

    public void setAttachments(List<Attachment> newList) {
        attachments.clear();
        if (newList != null) attachments.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AttachmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAttachmentPreviewBinding binding = ItemAttachmentPreviewBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AttachmentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AttachmentViewHolder holder, int position) {
        holder.bind(attachments.get(position));
    }

    @Override
    public int getItemCount() {
        return attachments.size();
    }

    static class AttachmentViewHolder extends RecyclerView.ViewHolder {

        private final ItemAttachmentPreviewBinding binding;

        AttachmentViewHolder(@NonNull ItemAttachmentPreviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Attachment attachment) {
            binding.tvName.setText(attachment.getName());

            int placeholder = getPlaceholderRes(attachment.getType());

            if (Constants.ATTACH_TYPE_IMAGE.equals(attachment.getType())
                    || Constants.ATTACH_TYPE_VIDEO.equals(attachment.getType())) {
                binding.ivPreview.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                Glide.with(binding.ivPreview)
                        .load(attachment.getUrl())
                        .centerCrop()
                        .placeholder(placeholder)
                        .error(placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(binding.ivPreview);
            } else {
                binding.ivPreview.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
                binding.ivPreview.setImageResource(placeholder);
            }

            binding.getRoot().setOnClickListener(v -> openUrl(v, attachment.getUrl()));
        }

        private static int getPlaceholderRes(String type) {
            if (Constants.ATTACH_TYPE_IMAGE.equals(type)) return R.drawable.ic_image;
            if (Constants.ATTACH_TYPE_VIDEO.equals(type)) return R.drawable.ic_video;
            if (Constants.ATTACH_TYPE_AUDIO.equals(type)) return R.drawable.ic_audio;
            return R.drawable.ic_document;
        }

        private static void openUrl(View view, String url) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                view.getContext().startActivity(intent);
            } catch (Exception ignored) {
            }
        }
    }
}


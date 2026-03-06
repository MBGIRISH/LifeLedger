package com.personaldiary.models;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class DiaryNote {

    private String content;
    private String tag;
    private String date;
    private List<Attachment> attachments;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public DiaryNote() {}

    public DiaryNote(String content, String tag, String date,
                     List<Attachment> attachments,
                     Timestamp createdAt, Timestamp updatedAt) {
        this.content = content;
        this.tag = tag;
        this.date = date;
        this.attachments = attachments;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public List<Attachment> getAttachments() { return attachments; }
    public void setAttachments(List<Attachment> attachments) { this.attachments = attachments; }

    public boolean hasAttachments() {
        return attachments != null && !attachments.isEmpty();
    }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}

package com.personaldiary.utils;

public final class Constants {

    private Constants() {}

    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_DIARY_NOTES = "diary_notes";
    public static final String SUBCOLLECTION_NOTES = "notes";

    public static final String TAG_NORMAL = "NORMAL";
    public static final String TAG_SPECIAL = "SPECIAL";
    public static final String TAG_IMPORTANT = "IMPORTANT";
    public static final String TAG_BAD_NEWS = "BAD_NEWS";

    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_TAG = "tag";
    public static final String FIELD_DATE = "date";
    public static final String FIELD_CREATED_AT = "createdAt";
    public static final String FIELD_UPDATED_AT = "updatedAt";
    public static final String FIELD_ATTACHMENTS = "attachments";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_EMAIL = "email";

    public static final String ATTACH_TYPE_IMAGE = "image";
    public static final String ATTACH_TYPE_DOCUMENT = "document";
    public static final String ATTACH_TYPE_AUDIO = "audio";
    public static final String ATTACH_TYPE_VIDEO = "video";

    public static final String EXTRA_SEARCH_TYPE = "search_type";
    public static final String EXTRA_MONTH = "month";
    public static final String EXTRA_YEAR = "year";
    public static final String EXTRA_TAG = "tag";

    public static final String SEARCH_TYPE_MONTH = "month";
    public static final String SEARCH_TYPE_TAG = "tag";
}

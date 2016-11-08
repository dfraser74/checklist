package com.philschatz.checklist;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.Instant;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.ChronoField;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class ToDoItem implements Serializable {

    /*
    createdAt
    completedAt
    updatedAt
    remindAt (optional)
     */

    private String mIdentifier;
    private String mTitle;
    // These are public so Firebase serializes them easily
    public String createdAt;
    public String remindAt;
    public String completedAt;


    public static String getNow() {
        return DateTimeFormatter.ISO_INSTANT.format(Instant.now());
    }

    public ToDoItem() {
        mIdentifier = UUID.randomUUID().toString();
        createdAt = getNow();
    }

    public ToDoItem(JSONObject jsonObject) throws JSONException {
        throw new RuntimeException("BUG: This constructor is no longer supported");
    }

    public JSONObject toJSON() throws JSONException {
        throw new RuntimeException("BUG: This serializer is no longer supported");
    }

    public void toggleCompletedAt() {
        completedAt = (completedAt == null) ? getNow() : null;
    }

    public boolean hasReminder() { return remindAt != null; }
    public boolean isComplete() { return completedAt != null; }
    public void clearReminder() { remindAt = null; }

    // Accessor methods are down here

    public String getIdentifier() {
        return mIdentifier;
    }

    public void setIdentifier(String identifier) {
        mIdentifier = identifier;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public long createdAt() {
        return fromString(createdAt);
    }
    public long completedAt() {
        return fromString(completedAt);
    }
    public long remindAt() {
        return fromString(remindAt);
    }

    public void createdAtSet(long at) {
        createdAt = fromLong(at);
    }
    public void completedAtSet(long at) {
        completedAt = fromLong(at);
    }
    public void remindAtSet(long at) {
        remindAt = fromLong(at);
    }


//    public Date legacyGetCompletedAt() {
//        return fromStringToDate(completedAt);
//    }
//    public Date legacyGetRemindAt() {
//        return fromStringToDate(remindAt);
//    }
//    public void legacySetCreatedAt(Date at) {
//        createdAt = fromDateToString(at);
//    }
//    public void legacySetCompletedAt(Date at) {
//        completedAt = fromDateToString(at);
//    }
//    public void legacySetRemindAt(Date at) {
//        remindAt = fromDateToString(at);
//    }
//
//
//    private Date fromStringToDate(String at) {
//        if (at == null) {
//            return null;
//        }
//        return new Date(fromString(at));
//    }
//
//    private String fromDateToString(Date at) {
//        if (at == null) {
//            return null;
//        } else {
//            return fromLong(at.getTime());
//        }
//    }


    private static long fromString(String at) {
        if (at != null) {
            return DateTimeFormatter.ISO_INSTANT.parse(at).getLong(ChronoField.INSTANT_SECONDS) * 1000;
        }
        // TODO: Turn this into a RuntimeException? Maybe to check that code checks `has###()` first.
        return 0L;
    }
    public static String fromLong(long at) {
        if (at == 0) {
            return null;
        } else {
            return DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(at));
        }
    }
}


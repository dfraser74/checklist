package com.philschatz.checklist;

import com.amulyakhare.textdrawable.util.ColorGenerator;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.Instant;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.ChronoField;

import java.io.Serializable;
import java.util.UUID;

public class ToDoList implements Serializable {

    /*
    createdAt
    completedAt
    updatedAt
    remindAt (optional)
     */

    public String title;
    // These are public so Firebase serializes them easily
    public String createdAt;
    public String remindAt;
    public String completedAt;
    public boolean isArchived;


    public ToDoList() {
        createdAt = ToDoItem.getNow();
    }

    public String getTitle() { return title; }

    public void setTitle(String t) { title = t; }

    public void isArchivedSet(boolean archived) {
        isArchived = archived;
    }

    public int getColor() {
        // Use the following as the default if color is null
        String firstLetter = getTitle().substring(0, 1);
        // Use the first letter as the hash for the color
        return ColorGenerator.MATERIAL.getColor(firstLetter);
    }

}


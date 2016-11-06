package com.philschatz.checklist;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class ToDoItem implements Serializable{

    /*
    createdAt
    completedAt
    updatedAt
    remindAt (optional)
     */

    private String mTitle;
    private Date mCreatedAt;
    private Date mRemindAt;
    private Date mCompletedAt;
    private String mIdentifier;
    private static final String TODOTEXT = "todotext";
    private static final String TODODATE = "tododate";
    private static final String TODOIDENTIFIER = "todoidentifier";


    public ToDoItem(){
        mIdentifier = UUID.randomUUID().toString();
        mCreatedAt = new Date();
    }

    public ToDoItem(JSONObject jsonObject) throws JSONException{
        mTitle = jsonObject.getString(TODOTEXT);
        mIdentifier = jsonObject.getString(TODOIDENTIFIER);

        if(jsonObject.has(TODODATE)){
            mRemindAt = new Date(jsonObject.getLong(TODODATE));
        }
    }

    public JSONObject toJSON() throws JSONException{
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TODOTEXT, mTitle);
        if(mRemindAt !=null){
            jsonObject.put(TODODATE, mRemindAt.getTime());
        }
        jsonObject.put(TODOIDENTIFIER, mIdentifier);

        return jsonObject;
    }



    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public Date getRemindAt() {
        return mRemindAt;
    }

    public void setRemindAt(Date at) {
        this.mRemindAt = at;
    }

    public Date getCompletedAt() {
        return mCompletedAt;
    }

    public void setCompletedAt(Date at) {
        this.mCompletedAt = at;
    }

    public Date getCreatedAt() {
        return mCreatedAt;
    }

    public void setCreatedAt(Date at) {
        this.mCreatedAt = at;
    }


    public String getIdentifier(){
        return mIdentifier;
    }

    public void setIdentifier(String identifier){
        mIdentifier = identifier;
    }

}


package com.example.samuel.wordbank.data;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Word {
    private String userId;
    private String name;
    private String translate;
    private String meaning;
    private String key;
    private int status;
    private long createdDate;

    public Word(String userId, String name, int status, long createdDate, String translate,
                String meaning, String key) {
        this.userId = userId;
        this.name = name;
        this.status = status;
        this.createdDate = createdDate;
        this.meaning = meaning;
        this.translate = translate;
        this.key = key;
    }

    public Word() {
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("meaning", meaning);
        result.put("translate", translate);
        result.put("status", status);
        result.put("userId", userId);
        result.put("key", key);
        result.put("createdDate", createdDate);

        return  result;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTranslate() {
        return translate;
    }

    public void setTranslate(String translate) {
        this.translate = translate;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}

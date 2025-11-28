package com.example.famigo_android.data.tasks;

public class Task {

    private String id;
    private String family_id;
    private String title;
    private String description;
    private String deadline;
    private String status;
    private int points_value;
    private String category;

    public String getId() {
        return id;
    }

    public String getFamily_id() {
        return family_id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDeadline() {
        return deadline;
    }

    public String getStatus() {
        return status;
    }

    public int getPoints_value() {
        return points_value;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

}

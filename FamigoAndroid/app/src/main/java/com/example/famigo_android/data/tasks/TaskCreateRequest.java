package com.example.famigo_android.data.tasks;

public class TaskCreateRequest {

    private String title;
    private String description;
    private String deadline;     // ISO string: 2025-11-25T00:00:00.000Z
    private int points_value;

    public TaskCreateRequest(String title, String description, String deadline, int points_value) {
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.points_value = points_value;
    }

    // getters if you ever need them (optional)
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDeadline() { return deadline; }
    public int getPoints_value() { return points_value; }
}

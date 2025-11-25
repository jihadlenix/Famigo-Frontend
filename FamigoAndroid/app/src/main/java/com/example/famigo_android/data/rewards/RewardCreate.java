package com.example.famigo_android.data.rewards;

public class RewardCreate {
    public String title;
    public String description;
    public int cost_points;

    public RewardCreate(String title, String description, int costPoints) {
        this.title = title;
        this.description = description;
        this.cost_points = costPoints;
    }
}

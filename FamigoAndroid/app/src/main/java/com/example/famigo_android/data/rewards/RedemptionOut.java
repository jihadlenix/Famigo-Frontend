package com.example.famigo_android.data.rewards;

import java.util.Date;

public class RedemptionOut {
    public String id;
    public String reward_id;
    public String requested_by_member_id;
    public String approved_by_member_id;
    public String status;
    public Date created_at;
    public Date updated_at;
    public Date redeemed_at;
    public String reward_title;  // Reward title for display
}


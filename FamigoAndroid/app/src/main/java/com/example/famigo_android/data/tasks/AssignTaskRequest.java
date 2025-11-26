package com.example.famigo_android.data.tasks;

public class AssignTaskRequest {

    // We assign by FamilyMember.id (member_id), not username
    private String member_id;

    public AssignTaskRequest(String member_id) {
        this.member_id = member_id;
    }

    public String getMember_id() {
        return member_id;
    }
}

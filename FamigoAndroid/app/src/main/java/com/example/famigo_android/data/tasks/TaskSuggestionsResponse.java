package com.example.famigo_android.data.tasks;

import java.util.List;

public class TaskSuggestionsResponse {
    public String task_id;
    public String task_title;
    public String category;
    public List<AssignmentSuggestion> suggestions;

    public static class AssignmentSuggestion {
        public String member_id;
        public String display_name;
        public String full_name;
        public String role;
        public Integer age;
        public Double score;
        public String reason;
    }
}


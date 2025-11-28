package com.example.famigo_android.data.tasks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.famigo_android.R;
import com.example.famigo_android.ui.tasks.TasksDashboardActivity;
import com.example.famigo_android.ui.utils.FamigoToast;
import com.example.famigo_android.data.network.ApiClient;
import com.example.famigo_android.data.tasks.TaskApi;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks;
    private String mode;
    private String token;

    private TasksDashboardActivity dashboardActivity;

    public TaskAdapter(List<Task> tasks, String mode, String token,
                       TasksDashboardActivity dashboardActivity) {
        this.tasks = tasks;
        this.mode = mode;
        this.token = token;
        this.dashboardActivity = dashboardActivity;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_card, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {

        Task task = tasks.get(position);

        holder.title.setText(task.getTitle());

        // Format date
        String formatted = task.getDeadline();
        try {
            SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            SimpleDateFormat out = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
            formatted = out.format(iso.parse(task.getDeadline()));
        } catch (Exception ignored) {}

        holder.deadline.setText(formatted);
        holder.points.setText(task.getPoints_value() + " pts");

        // Display category
        String category = task.getCategory();
        if (category != null && !category.isEmpty()) {
            holder.category.setText(category.toUpperCase());
            holder.category.setVisibility(View.VISIBLE);
        } else {
            holder.category.setVisibility(View.GONE);
        }

        if ("MY_TASKS".equals(mode)) {
            holder.assignedLabel.setText("Assigned: You");
        } else {
            holder.assignedLabel.setText("Family Task");
        }

        boolean isDone = task.getStatus() != null &&
                task.getStatus().equalsIgnoreCase("DONE");

        holder.checkbox.setOnCheckedChangeListener(null);
        holder.checkbox.setChecked(isDone);
        holder.checkbox.setEnabled(!isDone);

        // On complete
        holder.checkbox.setOnCheckedChangeListener((btn, checked) -> {

            if (!checked) return;

            TaskApi service = ApiClient.getTasksApi(); // ← UPDATED

            service.completeTask(token, task.getId())
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {

                            if (!response.isSuccessful()) {
                                String errorMsg = "Failed to complete task";
                                if (response.code() == 404) {
                                    errorMsg = "This task is not assigned to you";
                                } else if (response.code() == 403) {
                                    errorMsg = "You cannot complete this task";
                                }
                                FamigoToast.error(holder.itemView.getContext(), errorMsg);
                                holder.checkbox.setChecked(false);
                                return;
                            }

                            task.setStatus("DONE");
                            holder.checkbox.setEnabled(false);

                            // ADD COINS IN UI
                            dashboardActivity.addCoins(task.getPoints_value());

                            FamigoToast.success(holder.itemView.getContext(), "Task completed!");
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            FamigoToast.error(holder.itemView.getContext(),
                                    "Error: " + (t.getMessage() != null ? t.getMessage() : "Unknown error"));
                            holder.checkbox.setChecked(false);
                        }
                    });
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {

        CheckBox checkbox;
        TextView title, deadline, points, assignedLabel, category;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);

            checkbox = itemView.findViewById(R.id.checkbox_done);
            title = itemView.findViewById(R.id.text_task_title);
            deadline = itemView.findViewById(R.id.text_task_deadline);
            points = itemView.findViewById(R.id.text_task_points);
            assignedLabel = itemView.findViewById(R.id.text_task_assigned);
            category = itemView.findViewById(R.id.text_task_category);
        }
    }
}

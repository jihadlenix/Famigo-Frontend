package com.example.famigo_android.ui.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.famigo_android.R;
import com.example.famigo_android.data.network.ApiClient;
import com.example.famigo_android.data.tasks.PointsResponse;
import com.example.famigo_android.data.tasks.Task;
import com.example.famigo_android.data.tasks.TaskApi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskCalendarActivity extends AppCompatActivity {

    private TextView textCoinsValue;
    private CalendarView calendarView;

    private LinearLayout containerMyTasks, containerFamilyTasks;

    private List<Task> allMyTasks = new ArrayList<>();
    private List<Task> allFamilyTasks = new ArrayList<>();

    private String selectedDate = ""; // yyyy-MM-dd

    String token =
            "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI4ZThmY2JhYi00YTQzLTRmZGItYTA0Zi02MWI4ODU5ODZiZjMiLCJleHAiOjE3NjQxNzM1NDh9._eVMON9kXE3xo0t3pdshXXDObXni-8t5qhhi0Rp06BA";

    String familyId = "1fe7a2a4-bf5c-4221-b7ab-73feab72b519";

    private TaskApi taskApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_calendar);

        taskApi = ApiClient.getTasksApi();

        textCoinsValue = findViewById(R.id.text_coins_value);
        containerMyTasks = findViewById(R.id.container_my_tasks);
        containerFamilyTasks = findViewById(R.id.container_family_tasks);
        calendarView = findViewById(R.id.calendarView);

        // Default selected date = today
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        selectedDate = sdf.format(new Date());

        loadCoins();
        loadTasks();

        ImageButton buttonAdd = findViewById(R.id.button_add_task);
        buttonAdd.setOnClickListener(v ->
                startActivity(new Intent(TaskCalendarActivity.this, AddTaskActivity.class))
        );

        ImageView calendarToggle = findViewById(R.id.icon_calendar_toggle);
        calendarToggle.setOnClickListener(v -> finish());

        calendarView.setOnDateChangeListener((view, year, month, day) -> {
            selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day);
            filterTasks();
        });

        findViewById(R.id.nav_home).setOnClickListener(v -> {
            startActivity(new Intent(TaskCalendarActivity.this, TasksDashboardActivity.class));
            finish();
        });
    }


    // ---------------------------------------------------------
    // LOAD COINS
    // ---------------------------------------------------------
    private void loadCoins() {
        taskApi.getMyPoints(token)
                .enqueue(new Callback<PointsResponse>() {
                    @Override
                    public void onResponse(Call<PointsResponse> call, Response<PointsResponse> response) {
                        if (response.isSuccessful() && response.body() != null)
                            textCoinsValue.setText(String.valueOf(response.body().getTotalPoints()));
                    }

                    @Override
                    public void onFailure(Call<PointsResponse> call, Throwable t) {}
                });
    }


    // ---------------------------------------------------------
    // LOAD TASKS
    // ---------------------------------------------------------
    private void loadTasks() {

        // My tasks
        taskApi.getMyTasks(token).enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful() && response.body() != null)
                    allMyTasks = response.body();
                filterTasks();
            }

            @Override public void onFailure(Call<List<Task>> call, Throwable t) {}
        });

        // Family tasks
        taskApi.getFamilyTasks(token, familyId).enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful() && response.body() != null)
                    allFamilyTasks = response.body();
                filterTasks();
            }

            @Override public void onFailure(Call<List<Task>> call, Throwable t) {}
        });
    }


    // ---------------------------------------------------------
    // FILTER TASKS BY SELECTED DATE
    // ---------------------------------------------------------
    private void filterTasks() {

        containerMyTasks.removeAllViews();
        containerFamilyTasks.removeAllViews();

        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        SimpleDateFormat justDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        // My tasks
        for (Task t : allMyTasks) {
            try {
                if (justDate.format(iso.parse(t.getDeadline())).equals(selectedDate))
                    addCard(containerMyTasks, t);
            } catch (Exception ignored) {}
        }

        // Family tasks
        for (Task t : allFamilyTasks) {
            try {
                if (justDate.format(iso.parse(t.getDeadline())).equals(selectedDate))
                    addCard(containerFamilyTasks, t);
            } catch (Exception ignored) {}
        }
    }


    // ---------------------------------------------------------
    // ADD A SINGLE TASK CARD
    // ---------------------------------------------------------
    private void addCard(LinearLayout parent, Task task) {

        View card = LayoutInflater.from(this)
                .inflate(R.layout.item_task_calendar_card, parent, false);

        TextView title = card.findViewById(R.id.text_task_title);
        TextView points = card.findViewById(R.id.text_task_points);
        CheckBox checkbox = card.findViewById(R.id.checkbox_done);

        title.setText(task.getTitle());
        points.setText(task.getPoints_value() + " pts");

        boolean done = "DONE".equalsIgnoreCase(task.getStatus());
        checkbox.setChecked(done);
        checkbox.setEnabled(!done);


        // ✓ Complete task when checkbox clicked
        checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (!isChecked) return;

            taskApi.completeTask(token, task.getId())
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {

                            if (!response.isSuccessful()) {
                                Toast.makeText(TaskCalendarActivity.this,
                                        "Failed to complete task",
                                        Toast.LENGTH_SHORT).show();
                                checkbox.setChecked(false);
                                return;
                            }

                            task.setStatus("DONE");
                            checkbox.setEnabled(false);

                            // Add coins on UI
                            int current = Integer.parseInt(textCoinsValue.getText().toString());
                            int updated = current + task.getPoints_value();
                            textCoinsValue.setText(String.valueOf(updated));

                            Toast.makeText(TaskCalendarActivity.this,
                                    "Task completed! +" + task.getPoints_value() + " pts",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(TaskCalendarActivity.this,
                                    "Error: " + t.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            checkbox.setChecked(false);
                        }
                    });
        });

        parent.addView(card);
    }
}

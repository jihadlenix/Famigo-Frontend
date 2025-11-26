package com.example.famigo_android.ui.tasks;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.famigo_android.R;
import com.example.famigo_android.data.network.ApiClient;
import com.example.famigo_android.data.tasks.TaskApi;
import com.example.famigo_android.data.tasks.Task;
import com.example.famigo_android.data.tasks.AssignTaskRequest;
import com.example.famigo_android.data.tasks.TaskCreateRequest;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTaskActivity extends AppCompatActivity {

    private EditText inputTitle, inputDescription, inputAssigned, inputPoints;
    private TextView textDeadline;
    private Button submitButton;

    private String selectedISODate = null;

    String token =
            "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI4ZThmY2JhYi00YTQzLTRmZGItYTA0Zi02MWI4ODU5ODZiZjMiLCJleHAiOjE3NjQxNzk1ODh9.gtDLYgD3QQSTXj_rz6TOBBm2XD8oS0Wa2AHiII0P-r8";

    String familyId = "1fe7a2a4-bf5c-4221-b7ab-73feab72b519";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        inputTitle       = findViewById(R.id.input_title);
        inputDescription = findViewById(R.id.input_description);
        inputAssigned    = findViewById(R.id.input_assigned);
        inputPoints      = findViewById(R.id.input_points);
        textDeadline     = findViewById(R.id.text_deadline);
        submitButton     = findViewById(R.id.button_submit_task);

        // ========= DEADLINE PICKER =========
        textDeadline.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            new DatePickerDialog(
                    AddTaskActivity.this,
                    (picker, y, m, d) -> {
                        String visual = y + "-" +
                                String.format("%02d", (m + 1)) + "-" +
                                String.format("%02d", d);

                        textDeadline.setText(visual);
                        selectedISODate = visual + "T00:00:00.000Z";
                    },
                    year, month, day
            ).show();
        });

        // ========= SUBMIT TASK =========
        submitButton.setOnClickListener(v -> submitTask());

        // Back Arrow
        ImageButton backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(v -> finish());
    }

    // ============================================================
    // CREATE TASK → ASSIGN TASK
    // ============================================================
    private void submitTask() {

        String title       = inputTitle.getText().toString().trim();
        String description = inputDescription.getText().toString().trim();
        String assignedTo  = inputAssigned.getText().toString().trim();
        String pointsStr   = inputPoints.getText().toString().trim();

        if (title.isEmpty() || pointsStr.isEmpty() || selectedISODate == null) {
            Toast.makeText(this, "Please fill all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        int points = Integer.parseInt(pointsStr);

        TaskCreateRequest request = new TaskCreateRequest(
                title,
                description,
                selectedISODate,
                points
        );

        // 🌟 NEW API CLIENT USAGE
        TaskApi service = ApiClient.getTasksApi();

        // -----------------------------
        // 1) CREATE TASK
        // -----------------------------
        service.createFamilyTask(token, familyId, request)
                .enqueue(new Callback<Task>() {
                    @Override
                    public void onResponse(Call<Task> call, Response<Task> response) {

                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(AddTaskActivity.this,
                                    "Failed to create task.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String taskId = response.body().getId();

                        // -----------------------------
                        // 2) ASSIGN TASK
                        // -----------------------------
                        AssignTaskRequest assignReq = new AssignTaskRequest(assignedTo);

                        service.assignTask(token, taskId, assignReq)
                                .enqueue(new Callback<Void>() {
                                    @Override
                                    public void onResponse(Call<Void> call, Response<Void> response) {

                                        if (!response.isSuccessful()) {
                                            Toast.makeText(AddTaskActivity.this,
                                                    "Task created but NOT assigned.",
                                                    Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        Toast.makeText(AddTaskActivity.this,
                                                "Task added successfully!",
                                                Toast.LENGTH_SHORT).show();

                                        finish();
                                    }

                                    @Override
                                    public void onFailure(Call<Void> call, Throwable t) {
                                        Toast.makeText(AddTaskActivity.this,
                                                "Assign failed: " + t.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }

                    @Override
                    public void onFailure(Call<Task> call, Throwable t) {
                        Toast.makeText(AddTaskActivity.this,
                                "Error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

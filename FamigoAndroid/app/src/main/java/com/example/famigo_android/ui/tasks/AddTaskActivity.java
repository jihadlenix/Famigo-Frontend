package com.example.famigo_android.ui.tasks;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.famigo_android.R;
import com.example.famigo_android.data.auth.TokenStore;
import com.example.famigo_android.data.network.ApiClient;
import com.example.famigo_android.data.tasks.AssignTaskRequest;
import com.example.famigo_android.data.tasks.Task;
import com.example.famigo_android.data.tasks.TaskApi;
import com.example.famigo_android.data.tasks.TaskCreateRequest;
import com.example.famigo_android.data.user.FamilyDto;
import com.example.famigo_android.data.user.MeOut;
import com.example.famigo_android.data.user.MemberDto;
import com.example.famigo_android.data.user.UserRepository;
import com.example.famigo_android.ui.auth.ProfileActivity;
import com.example.famigo_android.ui.rewards.StoreActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTaskActivity extends AppCompatActivity {

    private EditText inputTitle;
    private EditText inputDescription;
    private EditText inputPoints;
    private TextView textDeadline;
    private Spinner spinnerAssigned;
    private Button submitButton;

    private String selectedISODate = null;

    private String token;
    private String familyId;

    private TaskApi taskApi;
    private UserRepository userRepo;

    // Spinner: index -> FamilyMember.id
    private final List<String> memberIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        taskApi = ApiClient.getTasksApi();
        userRepo = new UserRepository(this);

        TokenStore ts = new TokenStore(this);
        token = "Bearer " + ts.getAccessToken();

        familyId = getIntent().getStringExtra("FAMILY_ID");
        if (familyId == null) {
            familyId = ts.getFamilyId();
        }
        if (familyId == null) {
            Toast.makeText(this, "Family ID missing", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Views
        inputTitle       = findViewById(R.id.input_title);
        inputDescription = findViewById(R.id.input_description);
        inputPoints      = findViewById(R.id.input_points);
        textDeadline     = findViewById(R.id.text_deadline);
        spinnerAssigned  = findViewById(R.id.spinner_assigned);
        submitButton     = findViewById(R.id.button_submit_task);

        // Back button
        ImageButton backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(v -> finish());

        // Deadline picker (text and icon)
        ImageView deadlineIcon = findViewById(R.id.icon_deadline);
        textDeadline.setOnClickListener(v -> showDatePicker());
        deadlineIcon.setOnClickListener(v -> showDatePicker());

        // Submit
        submitButton.setOnClickListener(v -> submitTask());

        // Bottom nav
        setupBottomNav();

        // Load family members into spinner
        loadMembers();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year  = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day   = calendar.get(Calendar.DAY_OF_MONTH);

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
    }

    private void setupBottomNav() {
        ImageButton navHome     = findViewById(R.id.nav_home);
        ImageButton navMessages = findViewById(R.id.nav_messages);
        ImageButton navProfile  = findViewById(R.id.nav_profile);

        navHome.setOnClickListener(v -> {
            Intent i = new Intent(AddTaskActivity.this, TasksDashboardActivity.class);
            i.putExtra("FAMILY_ID", familyId);
            startActivity(i);
            finish();
        });

        navMessages.setOnClickListener(v -> {
            Intent i = new Intent(AddTaskActivity.this, StoreActivity.class);
            i.putExtra("FAMILY_ID", familyId);
            startActivity(i);
            finish();
        });

        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(AddTaskActivity.this, ProfileActivity.class));
            finish();
        });
    }

    // -------------------------------------------------------------
    // Load members for the current family into Spinner
    // -------------------------------------------------------------
    private void loadMembers() {
        userRepo.getMe().enqueue(new Callback<MeOut>() {
            @Override
            public void onResponse(Call<MeOut> call, Response<MeOut> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(AddTaskActivity.this,
                            "Failed to load members", Toast.LENGTH_SHORT).show();
                    return;
                }

                MeOut me = response.body();
                String myUserId = me.id;

                FamilyDto target = null;
                if (me.families != null) {
                    for (FamilyDto f : me.families) {
                        if (familyId.equals(f.id)) {
                            target = f;
                            break;
                        }
                    }
                }

                if (target == null || target.members == null || target.members.isEmpty()) {
                    Toast.makeText(AddTaskActivity.this,
                            "No members in this family", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<String> labels = new ArrayList<>();
                memberIds.clear();

                for (MemberDto m : target.members) {
                    String label;

                    if (m.display_name != null && !m.display_name.isEmpty()) {
                        label = m.display_name;
                    } else if (m.role != null) {
                        if ("PARENT".equalsIgnoreCase(m.role)) {
                            label = "Parent";
                        } else if ("CHILD".equalsIgnoreCase(m.role)) {
                            label = "Child";
                        } else {
                            label = m.role;
                        }
                    } else {
                        label = "Member";
                    }

                    if (m.user_id != null && m.user_id.equals(myUserId)) {
                        label = label + " (you)";
                    }

                    labels.add(label);
                    memberIds.add(m.id); // FamilyMember.id
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        AddTaskActivity.this,
                        android.R.layout.simple_spinner_item,
                        labels
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerAssigned.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<MeOut> call, Throwable t) {
                Toast.makeText(AddTaskActivity.this,
                        "Error loading members: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // -------------------------------------------------------------
    // Create task + assign to selected member
    // -------------------------------------------------------------
    private void submitTask() {

        String title       = inputTitle.getText().toString().trim();
        String description = inputDescription.getText().toString().trim();
        String pointsStr   = inputPoints.getText().toString().trim();

        if (title.isEmpty() || pointsStr.isEmpty() || selectedISODate == null) {
            Toast.makeText(this, "Please fill all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedIndex = spinnerAssigned.getSelectedItemPosition();
        if (selectedIndex < 0 || selectedIndex >= memberIds.size()) {
            Toast.makeText(this, "Please choose who will do the task.", Toast.LENGTH_SHORT).show();
            return;
        }

        int points;
        try {
            points = Integer.parseInt(pointsStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Points must be a number.", Toast.LENGTH_SHORT).show();
            return;
        }

        String assigneeMemberId = memberIds.get(selectedIndex);

        TaskCreateRequest request = new TaskCreateRequest(
                title,
                description,
                selectedISODate,
                points
        );

        // 1) Create task
        taskApi.createFamilyTask(token, familyId, request)
                .enqueue(new Callback<Task>() {
                    @Override
                    public void onResponse(Call<Task> call, Response<Task> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(AddTaskActivity.this,
                                    "Failed to create task.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String taskId = response.body().getId();

                        // 2) Assign task to selected family member
                        AssignTaskRequest assignReq = new AssignTaskRequest(assigneeMemberId);

                        taskApi.assignTask(token, taskId, assignReq)
                                .enqueue(new Callback<Void>() {
                                    @Override
                                    public void onResponse(Call<Void> call,
                                                           Response<Void> response) {
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

package com.example.famigo_android.ui.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.famigo_android.R;
import com.example.famigo_android.data.auth.TokenStore;
import com.example.famigo_android.data.network.ApiClient;
import com.example.famigo_android.data.tasks.PointsResponse;
import com.example.famigo_android.data.tasks.Task;
import com.example.famigo_android.data.tasks.TaskAdapter;
import com.example.famigo_android.data.tasks.TaskApi;
import com.example.famigo_android.data.user.MeOut;
import com.example.famigo_android.data.user.UserRepository;
import com.example.famigo_android.data.family.FamilyOut;
import com.example.famigo_android.data.family.FamilyMemberOut;
import com.example.famigo_android.data.family.FamilyRepository;
import com.example.famigo_android.ui.NavigationHelper;
import com.example.famigo_android.ui.utils.FamigoToast;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TasksDashboardActivity extends AppCompatActivity {

    RecyclerView recyclerMyTasks, recyclerFamilyTasks;
    TaskAdapter myTasksAdapter, familyTasksAdapter;

    TextView textCoinsValue;
    int totalCoins = 0;

    TokenStore tokenStore;

    private String token;
    private String familyId;
    private String currentUserId;
    private boolean isCurrentUserParent = false;

    private TaskApi taskApi;
    private UserRepository userRepo;
    private FamilyRepository familyRepo;
    
    private View emptyMyTasks, emptyFamilyTasks;
    private com.google.android.material.floatingactionbutton.FloatingActionButton addTaskButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks_dashboard);

        // Set status bar color to green
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getColor(R.color.famigo_green_dark));
        }

        taskApi = ApiClient.getTasksApi();
        userRepo = new UserRepository(this);
        familyRepo = new FamilyRepository(this);

        tokenStore = new TokenStore(this);
        token = "Bearer " + tokenStore.getAccessToken();
        familyId = getIntent().getStringExtra("FAMILY_ID");

        if (familyId == null) {
            familyId = tokenStore.getFamilyId();
        }
        tokenStore.saveFamilyId(familyId);

        if (familyId == null) {
            FamigoToast.error(this, "Family ID missing!");
            finish();
            return;
        }

        // Setup unified bottom navigation
        NavigationHelper.setupBottomNavigation(this, NavigationHelper.Tab.TASKS);

        addTaskButton = findViewById(R.id.button_add_task);
        ImageButton calendarButton = findViewById(R.id.button_calendar);
        textCoinsValue = findViewById(R.id.text_coins_value);

        addTaskButton.setOnClickListener(v -> {
            Intent i = new Intent(this, AddTaskActivity.class);
            i.putExtra("FAMILY_ID", familyId);
            startActivity(i);
        });

        calendarButton.setOnClickListener(v -> {
            Intent i = new Intent(this, TaskCalendarActivity.class);
            i.putExtra("FAMILY_ID", familyId);
            startActivity(i);
        });

        recyclerMyTasks = findViewById(R.id.recycler_my_tasks);
        recyclerFamilyTasks = findViewById(R.id.recycler_family_tasks);
        emptyMyTasks = findViewById(R.id.empty_my_tasks);
        emptyFamilyTasks = findViewById(R.id.empty_family_tasks);

        recyclerMyTasks.setLayoutManager(new LinearLayoutManager(this));
        recyclerFamilyTasks.setLayoutManager(new LinearLayoutManager(this));

        // Load current user to check role, then load tasks
        loadCurrentUser();
        fetchCoins();
    }

    private void loadCurrentUser() {
        userRepo.getMe().enqueue(new Callback<MeOut>() {
            @Override
            public void onResponse(Call<MeOut> call, Response<MeOut> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUserId = response.body().id;
                    checkUserRole();
                } else {
                    // Still load tasks, just won't be able to check parent status
                    loadTasks();
                }
            }

            @Override
            public void onFailure(Call<MeOut> call, Throwable t) {
                // Still load tasks, just won't be able to check parent status
                loadTasks();
            }
        });
    }

    private void checkUserRole() {
        familyRepo.getFamilyById(familyId).enqueue(new Callback<FamilyOut>() {
            @Override
            public void onResponse(Call<FamilyOut> call, Response<FamilyOut> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FamilyOut family = response.body();
                    // Check if current user is a parent in this family
                    if (family.members != null && currentUserId != null) {
                        for (FamilyMemberOut member : family.members) {
                            if (currentUserId.equals(member.user_id) && "PARENT".equalsIgnoreCase(member.role)) {
                                isCurrentUserParent = true;
                                break;
                            }
                        }
                    }
                    // Show/hide FAB based on role - only parents can add tasks
                    if (addTaskButton != null) {
                        addTaskButton.setVisibility(isCurrentUserParent ? View.VISIBLE : View.GONE);
                    }
                }
                // Load tasks regardless
                loadTasks();
            }

            @Override
            public void onFailure(Call<FamilyOut> call, Throwable t) {
                // Load tasks even if role check fails
                loadTasks();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchCoins();
        loadTasks();
    }

    private void loadTasks() {

        taskApi.getMyTasks(token).enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful()) {
                    List<Task> tasks = response.body();
                    if (tasks == null || tasks.isEmpty()) {
                        emptyMyTasks.setVisibility(View.VISIBLE);
                        recyclerMyTasks.setVisibility(View.GONE);
                    } else {
                        emptyMyTasks.setVisibility(View.GONE);
                        recyclerMyTasks.setVisibility(View.VISIBLE);
                    myTasksAdapter = new TaskAdapter(
                                tasks,
                            "MY_TASKS",
                            token,
                            TasksDashboardActivity.this
                    );
                    recyclerMyTasks.setAdapter(myTasksAdapter);
                    }
                }
            }

            @Override public void onFailure(Call<List<Task>> call, Throwable t) {}
        });

        taskApi.getFamilyTasks(token, familyId)
                .enqueue(new Callback<List<Task>>() {
                    @Override
                    public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                        if (response.isSuccessful()) {
                            List<Task> tasks = response.body();
                            if (tasks == null || tasks.isEmpty()) {
                                emptyFamilyTasks.setVisibility(View.VISIBLE);
                                recyclerFamilyTasks.setVisibility(View.GONE);
                            } else {
                                emptyFamilyTasks.setVisibility(View.GONE);
                                recyclerFamilyTasks.setVisibility(View.VISIBLE);
                            familyTasksAdapter = new TaskAdapter(
                                        tasks,
                                    "FAMILY_TASKS",
                                    token,
                                    TasksDashboardActivity.this
                            );
                            recyclerFamilyTasks.setAdapter(familyTasksAdapter);
                            }
                        }
                    }

                    @Override public void onFailure(Call<List<Task>> call, Throwable t) {}
                });
    }

    private void fetchCoins() {
        taskApi.getMyPoints(token).enqueue(new Callback<PointsResponse>() {
            @Override
            public void onResponse(Call<PointsResponse> call, Response<PointsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    totalCoins = response.body().getTotalPoints();
                    textCoinsValue.setText(String.valueOf(totalCoins));
                }
            }

            @Override public void onFailure(Call<PointsResponse> call, Throwable t) {}
        });
    }

    public void addCoins(int pts) {
        totalCoins += pts;
        textCoinsValue.setText(String.valueOf(totalCoins));
    }
}

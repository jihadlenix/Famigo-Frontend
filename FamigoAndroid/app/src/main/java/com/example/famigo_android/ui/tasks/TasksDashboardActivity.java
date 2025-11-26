package com.example.famigo_android.ui.tasks;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.famigo_android.ui.auth.ProfileActivity;
import com.example.famigo_android.ui.rewards.StoreActivity;

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

    private TaskApi taskApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks_dashboard);

        taskApi = ApiClient.getTasksApi();

        tokenStore = new TokenStore(this);
        token = "Bearer " + tokenStore.getAccessToken();
        familyId = getIntent().getStringExtra("FAMILY_ID");

        if (familyId == null) {
            familyId = tokenStore.getFamilyId();
        }
        tokenStore.saveFamilyId(familyId);

        if (familyId == null) {
            Toast.makeText(this, "Family ID missing!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ImageButton navHome = findViewById(R.id.nav_home);
        ImageButton navMessages = findViewById(R.id.nav_messages);
        ImageButton navProfile = findViewById(R.id.nav_profile);

        navHome.setColorFilter(getColor(R.color.text_primary));
        navMessages.setColorFilter(getColor(R.color.text_secondary));
        navProfile.setColorFilter(getColor(R.color.text_secondary));

        navMessages.setOnClickListener(v -> {
            Intent i = new Intent(TasksDashboardActivity.this, StoreActivity.class);
            i.putExtra("FAMILY_ID", familyId);
            startActivity(i);
        });

        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(TasksDashboardActivity.this, ProfileActivity.class));
        });

        ImageButton addTaskButton = findViewById(R.id.button_add_task);
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

        recyclerMyTasks.setLayoutManager(new LinearLayoutManager(this));
        recyclerFamilyTasks.setLayoutManager(new LinearLayoutManager(this));

        fetchCoins();
        loadTasks();
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
                    myTasksAdapter = new TaskAdapter(
                            response.body(),
                            "MY_TASKS",
                            token,
                            TasksDashboardActivity.this
                    );
                    recyclerMyTasks.setAdapter(myTasksAdapter);
                }
            }

            @Override public void onFailure(Call<List<Task>> call, Throwable t) {}
        });

        taskApi.getFamilyTasks(token, familyId)
                .enqueue(new Callback<List<Task>>() {
                    @Override
                    public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                        if (response.isSuccessful()) {
                            familyTasksAdapter = new TaskAdapter(
                                    response.body(),
                                    "FAMILY_TASKS",
                                    token,
                                    TasksDashboardActivity.this
                            );
                            recyclerFamilyTasks.setAdapter(familyTasksAdapter);
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

package com.example.famigo_android.data.tasks;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface TaskApi {

    // ➤ Get tasks assigned to the current user
    @GET("tasks/me/tasks")
    Call<List<Task>> getMyTasks(
            @Header("Authorization") String token
    );

    // ➤ Get family tasks
    @GET("tasks/families/{family_id}/tasks")
    Call<List<Task>> getFamilyTasks(
            @Header("Authorization") String token,
            @Path("family_id") String familyId
    );

    // ➤ Create a task inside a family
    @POST("tasks/families/{family_id}/tasks")
    Call<Task> createFamilyTask(
            @Header("Authorization") String token,
            @Path("family_id") String familyId,
            @Body TaskCreateRequest body
    );

    // ➤ Assign task to a family member (by member_id)
    @POST("tasks/tasks/{task_id}/assign")
    Call<Void> assignTask(
            @Header("Authorization") String token,
            @Path("task_id") String taskId,
            @Body AssignTaskRequest body
    );

    // ➤ Mark task as complete for the current user
    @POST("tasks/tasks/{task_id}/complete")
    Call<Void> completeTask(
            @Header("Authorization") String token,
            @Path("task_id") String taskId
    );

    // ➤ Get total points of current user
    @GET("tasks/me/points")
    Call<PointsResponse> getMyPoints(
            @Header("Authorization") String token
    );
}

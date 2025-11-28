package com.example.famigo_android.ui.auth;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.famigo_android.R;
import com.example.famigo_android.data.auth.TokenStore;
import com.example.famigo_android.data.user.FamilyDto;
import com.example.famigo_android.data.user.MeOut;
import com.example.famigo_android.data.user.MemberDto;
import com.example.famigo_android.data.network.ApiClient;
import com.example.famigo_android.data.user.UserRepository;
import com.example.famigo_android.data.user.UserUpdate;
import com.example.famigo_android.ui.NavigationHelper;
import com.example.famigo_android.ui.family.FamilyDetailsActivity;
import com.example.famigo_android.ui.auth.MainActivity;
import com.example.famigo_android.ui.utils.FamigoToast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private TextView fullNameTv;
    private TextView usernameTv;
    private TextView emailTv;
    private TextView walletTv;
    private TextView bioTv;
    private TextView avatarTv;
    private ImageView profilePicture;
    private View emptyFamilies;
    private RecyclerView familiesRecyclerView;
    private FamilyAdapter familyAdapter;

    private UserRepository userRepo;
    private MeOut currentMe;
    private File photoFile;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Set status bar color to green
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getColor(R.color.famigo_green_dark));
        }

        // Setup unified bottom navigation
        NavigationHelper.setupBottomNavigation(this, NavigationHelper.Tab.PROFILE);

        // ---- profile UI views ----
        fullNameTv = findViewById(R.id.profileFullName);
        usernameTv = findViewById(R.id.profileUsername);
        emailTv = findViewById(R.id.profileEmail);
        walletTv = findViewById(R.id.profileWallet);
        bioTv = findViewById(R.id.profileBio);
        avatarTv = findViewById(R.id.profileAvatar);
        profilePicture = findViewById(R.id.profilePicture);
        emptyFamilies = findViewById(R.id.emptyFamilies);
        familiesRecyclerView = findViewById(R.id.familiesRecyclerView);
        
        // Setup families RecyclerView
        familiesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        familyAdapter = new FamilyAdapter(null, this::onFamilyClick);
        familiesRecyclerView.setAdapter(familyAdapter);

        // Edit buttons
        ImageButton editFullNameBtn = findViewById(R.id.editFullNameButton);
        ImageButton editUsernameBtn = findViewById(R.id.editUsernameButton);
        ImageButton editBioBtn = findViewById(R.id.editBioButton);
        View editPictureBtn = findViewById(R.id.editPictureButton);

        if (editFullNameBtn != null) {
            editFullNameBtn.setOnClickListener(v -> showEditFullNameDialog());
        }
        if (editUsernameBtn != null) {
            editUsernameBtn.setOnClickListener(v -> showEditUsernameDialog());
        }
        if (editBioBtn != null) {
            editBioBtn.setOnClickListener(v -> showEditBioDialog());
        }
        if (editPictureBtn != null) {
            editPictureBtn.setOnClickListener(v -> showImageSourceDialog());
        }

        // Logout button
        android.widget.Button logoutBtn = findViewById(R.id.logoutButton);
        if (logoutBtn != null) {
            logoutBtn.setOnClickListener(v -> handleLogout());
        }

        // Setup activity result launchers
        setupImageLaunchers();

        // ---- call /users/me ----
        userRepo = new UserRepository(this);
        loadMe();
    }

    private void setupImageLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && photoFile != null) {
                        uploadImage(photoFile);
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            try {
                                File tempFile = createTempFileFromUri(imageUri);
                                uploadImage(tempFile);
                            } catch (IOException e) {
                                FamigoToast.error(this, "Error reading image: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
                            }
                        }
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMe();
    }

    private void loadMe() {
        userRepo.getMe().enqueue(new Callback<MeOut>() {
            @Override
            public void onResponse(Call<MeOut> call, Response<MeOut> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    FamigoToast.error(ProfileActivity.this, "Failed to load profile");
                    return;
                }

                currentMe = response.body();
                bindMe(currentMe);
            }

            @Override
            public void onFailure(Call<MeOut> call, Throwable t) {
                FamigoToast.error(ProfileActivity.this, t.getMessage() != null ? t.getMessage() : "Failed to load profile");
            }
        });
    }

    private void bindMe(MeOut me) {
        // Full name
        if (me.full_name != null && !me.full_name.isEmpty()) {
            fullNameTv.setText(me.full_name);
        } else {
            fullNameTv.setText("Not set");
        }

        // Username
        if (me.username != null && !me.username.isEmpty()) {
            usernameTv.setText("@" + me.username);
        } else {
            usernameTv.setText("Not set");
        }

        // Email
        emailTv.setText(me.email != null ? me.email : "No email");

        // Wallet
        if (me.wallet != null) {
            walletTv.setText(me.wallet.balance + " points");
        } else {
            walletTv.setText("0 points");
        }

        // Bio
        if (me.bio != null && !me.bio.isEmpty()) {
            bioTv.setText(me.bio);
            bioTv.setTextColor(getColor(R.color.text_primary));
        } else {
            bioTv.setText("No bio set");
            bioTv.setTextColor(getColor(R.color.text_secondary));
        }

        // Profile picture or avatar initial
        if (me.profile_pic != null && !me.profile_pic.isEmpty()) {
            // Show profile picture - load image in background
            // profile_pic is stored as "uploads/filename.jpg" in backend
            String baseUrl = ApiClient.getBaseUrl();
            String imageUrl = baseUrl + "static/" + me.profile_pic;
            loadProfileImage(imageUrl);
        } else {
            // Show avatar initial
            profilePicture.setVisibility(View.GONE);
            avatarTv.setVisibility(View.VISIBLE);
            String initial = "U";
            if (me.full_name != null && !me.full_name.isEmpty()) {
                initial = me.full_name.substring(0, 1).toUpperCase();
            } else if (me.username != null && !me.username.isEmpty()) {
                initial = me.username.substring(0, 1).toUpperCase();
            }
            avatarTv.setText(initial);
        }

        // Families
        renderFamilies(me.families);
    }

    private void renderFamilies(List<FamilyDto> families) {
        if (families == null || families.isEmpty()) {
            emptyFamilies.setVisibility(View.VISIBLE);
            familiesRecyclerView.setVisibility(View.GONE);
            return;
        }

        emptyFamilies.setVisibility(View.GONE);
        familiesRecyclerView.setVisibility(View.VISIBLE);
        familyAdapter = new FamilyAdapter(families, this::onFamilyClick);
        familiesRecyclerView.setAdapter(familyAdapter);
    }
    
    private void onFamilyClick(FamilyDto family) {
        Intent intent = new Intent(this, FamilyDetailsActivity.class);
        intent.putExtra("FAMILY_ID", family.id);
        startActivity(intent);
    }

    private void showEditFullNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Full Name");

        EditText input = new EditText(this);
        input.setHint("Enter your full name");
        if (currentMe != null && currentMe.full_name != null) {
            input.setText(currentMe.full_name);
        }
        input.setPadding(32, 24, 32, 24);

        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (newName.isEmpty()) {
                FamigoToast.warning(this, "Name cannot be empty");
                return;
            }
            updateProfile("full_name", newName);
        });

        builder.setNegativeButton("Cancel", (d, w) -> d.dismiss());
        builder.show();
    }

    private void showEditUsernameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Username");

        EditText input = new EditText(this);
        input.setHint("Enter username");
        if (currentMe != null && currentMe.username != null) {
            input.setText(currentMe.username);
        }
        input.setPadding(32, 24, 32, 24);

        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newUsername = input.getText().toString().trim();
            if (newUsername.isEmpty()) {
                FamigoToast.warning(this, "Username cannot be empty");
                return;
            }
            updateProfile("username", newUsername);
        });

        builder.setNegativeButton("Cancel", (d, w) -> d.dismiss());
        builder.show();
    }

    private void showEditBioDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Bio");

        EditText input = new EditText(this);
        input.setHint("Tell us about yourself");
        input.setMinLines(3);
        input.setMaxLines(5);
        if (currentMe != null && currentMe.bio != null) {
            input.setText(currentMe.bio);
        }
        input.setPadding(32, 24, 32, 24);

        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newBio = input.getText().toString().trim();
            updateProfile("bio", newBio);
        });

        builder.setNegativeButton("Cancel", (d, w) -> d.dismiss());
        builder.show();
    }

    private void showImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Profile Picture");
        
        String[] options = {"Camera", "Gallery"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                openCamera();
            } else {
                openGallery();
            }
        });
        
        builder.setNegativeButton("Cancel", (d, w) -> d.dismiss());
        builder.show();
    }

    private void openCamera() {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                photoFile = createImageFile();
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            "com.example.famigo_android.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    cameraLauncher.launch(takePictureIntent);
                }
            }
        } catch (IOException ex) {
            FamigoToast.error(this, "Error creating image file");
        }
    }

    private void openGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(pickPhoto);
    }

    private File createImageFile() throws IOException {
        String imageFileName = "JPEG_" + System.currentTimeMillis() + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private File createTempFileFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File tempFile = File.createTempFile("profile_", ".jpg", getCacheDir());
        FileOutputStream outputStream = new FileOutputStream(tempFile);
        
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        
        inputStream.close();
        outputStream.close();
        return tempFile;
    }

    private void loadProfileImage(String imageUrl) {
        // Load image in background thread
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(imageUrl);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                input.close();
                
                runOnUiThread(() -> {
                    if (bitmap != null) {
                        profilePicture.setImageBitmap(bitmap);
                        profilePicture.setVisibility(View.VISIBLE);
                        avatarTv.setVisibility(View.GONE);
                    } else {
                        showAvatarInitial();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(this::showAvatarInitial);
            }
        }).start();
    }

    private void showAvatarInitial() {
        profilePicture.setVisibility(View.GONE);
        avatarTv.setVisibility(View.VISIBLE);
        String initial = "U";
        if (currentMe != null) {
            if (currentMe.full_name != null && !currentMe.full_name.isEmpty()) {
                initial = currentMe.full_name.substring(0, 1).toUpperCase();
            } else if (currentMe.username != null && !currentMe.username.isEmpty()) {
                initial = currentMe.username.substring(0, 1).toUpperCase();
            }
        }
        avatarTv.setText(initial);
    }

    private void uploadImage(File imageFile) {
        userRepo.uploadProfilePicture(imageFile).enqueue(new Callback<MeOut>() {
            @Override
            public void onResponse(Call<MeOut> call, Response<MeOut> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentMe = response.body();
                    bindMe(currentMe);
                    FamigoToast.success(ProfileActivity.this, "Profile picture updated!");
                } else {
                    FamigoToast.error(ProfileActivity.this, "Failed to upload image");
                }
            }

            @Override
            public void onFailure(Call<MeOut> call, Throwable t) {
                FamigoToast.error(ProfileActivity.this, "Error: " + (t.getMessage() != null ? t.getMessage() : "Unknown error"));
                }
        });
    }

    private void updateProfile(String field, String value) {
        UserUpdate update = new UserUpdate();

        switch (field) {
            case "full_name":
                update.full_name = value;
                break;
            case "username":
                update.username = value;
                break;
            case "bio":
                update.bio = value;
                break;
            case "profile_pic":
                update.profile_pic = value;
                break;
        }

        userRepo.updateMe(update).enqueue(new Callback<MeOut>() {
            @Override
            public void onResponse(Call<MeOut> call, Response<MeOut> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentMe = response.body();
                    bindMe(currentMe);
                    FamigoToast.success(ProfileActivity.this, "Profile updated!");
                } else {
                    FamigoToast.error(ProfileActivity.this, "Failed to update profile");
            }
        }

            @Override
            public void onFailure(Call<MeOut> call, Throwable t) {
                FamigoToast.error(ProfileActivity.this, "Error: " + (t.getMessage() != null ? t.getMessage() : "Unknown error"));
            }
        });
    }

    private void handleLogout() {
        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    // Clear tokens
                    TokenStore tokenStore = new TokenStore(this);
                    tokenStore.clear();
                    
                    // Navigate to login screen
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}

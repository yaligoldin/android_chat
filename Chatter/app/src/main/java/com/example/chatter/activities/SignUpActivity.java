package com.example.chatter.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatter.databinding.ActivitySignUpBinding;
import com.example.chatter.utilities.Constants;
import com.example.chatter.utilities.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;
    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }

    private void setListeners() {
        binding.textSignIn.setOnClickListener(v -> onBackPressed());
        binding.buttonSignUp.setOnClickListener(v -> {
            if (isValidSignUpDetails()) {
                signUp();
            }
        });
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void signUp() {
        loading(true);

        // Get the entered user details
        String username = binding.inputUsername.getText().toString();
        String displayName = binding.inputDisplayName.getText().toString();
        String password = binding.inputPassword.getText().toString();

        // Create a new thread for network operations
        new Thread(() -> {
            try {
                // Create a JSON object for the new user
                JSONObject newUser = new JSONObject();
                newUser.put("username", username);
                newUser.put("password", password);
                newUser.put("displayName", displayName);
                newUser.put("profilePic", encodedImage);

                // Open a connection to the server
                String serverAddress = getServerAddress();
                String apiEndpoint = serverAddress + "api/Users";
                URL url = new URL(apiEndpoint);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Set the request method to POST
                connection.setRequestMethod("POST");

                // Set the content type header
                connection.setRequestProperty("Content-Type", "application/json");

                // Enable output for sending data
                connection.setDoOutput(true);

                // Write the JSON data to the request body
                connection.getOutputStream().write(newUser.toString().getBytes());

                // Get the response code
                int responseCode = connection.getResponseCode();

                // Process the response
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Registration successful
                    runOnUiThread(() -> {
                        preferenceManager.putString(Constants.KEY_USERNAME, username);
                        preferenceManager.putString(Constants.KEY_DISPLAY, displayName);
                        showToast("Registration successful");
                        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    });
                } else {
                    // Registration failed
                    runOnUiThread(() -> {
                        showToast("Username already exists");
                        loading(false);
                    });
                }

                // Disconnect from the server
                connection.disconnect();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                String errorMessage = e.getMessage();
                runOnUiThread(() -> {
                    showToast("The server address is invalid. Make sure it ends with '/'");
                    loading(false);
                });
            }
        }).start();
    }

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private String getServerAddress() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("ServerAddress", "");
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private Boolean isValidSignUpDetails() {
        if (encodedImage == null) {
            showToast("Select profile Image");
            return false;
        } else if (binding.inputUsername.getText().toString().trim().isEmpty()) {
            showToast("Enter username");
            return false;
        } else if (binding.inputDisplayName.getText().toString().trim().isEmpty()) {
            showToast("Enter display name");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter password");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().length() < 6) {
            showToast("Password must be at least 6 characters");
            return false;
        } else if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Verify your password");
            return false;
        } else if (!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())) {
            showToast("Passwords don't match");
            return false;
        } else {
            return true;
        }
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignUp.setVisibility(View.VISIBLE);
        }
    }
}
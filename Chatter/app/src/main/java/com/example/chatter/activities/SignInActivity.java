package com.example.chatter.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatter.databinding.ActivitySignInBinding;
import com.example.chatter.utilities.Constants;
import com.example.chatter.utilities.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners() {
        binding.textCreateNewAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
        binding.buttonSignIn.setOnClickListener(v -> {
            if (isValidSignInDetails()) {
                signIn();
            }
        });
        binding.fabSettings.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class)));
    }
    private String getServerAddress() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("ServerAddress", "");
    }
    private void signIn() {
        loading(true);
        String username = binding.inputUsername.getText().toString();
        String password = binding.inputPassword.getText().toString();

        // Create a new thread for network operations
        new Thread(() -> {
            try {
                // Create a JSON object with the login credentials
                JSONObject credentials = new JSONObject();
                credentials.put("username", username);
                credentials.put("password", password);

                // Open a connection to the server
                String serverAddress = getServerAddress();
                String apiEndpoint = serverAddress + "api/Tokens";
                URL url = new URL(apiEndpoint);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Set the request method to POST
                connection.setRequestMethod("POST");

                // Set the content type header
                connection.setRequestProperty("Content-Type", "application/json");

                // Enable output for sending data
                connection.setDoOutput(true);

                // Write the JSON data to the request body
                connection.getOutputStream().write(credentials.toString().getBytes());

                // Get the response code
                int responseCode = connection.getResponseCode();

                // Process the response
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Login successful
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    inputStream.close();

                    // Parse the JSON response
                    String token = response.toString();


                    // Fetch user information using the token
                    if (token != null) {
                        String userEndpoint = serverAddress + "api/Users/" + username;
                        URL userUrl = new URL(userEndpoint);
                        HttpURLConnection userConnection = (HttpURLConnection) userUrl.openConnection();
                        userConnection.setRequestMethod("GET");
                        userConnection.setRequestProperty("Authorization", "Bearer " + token);

                        int userResponseCode = userConnection.getResponseCode();

                        if (userResponseCode == HttpURLConnection.HTTP_OK) {

                            InputStream userInputStream = userConnection.getInputStream();
                            BufferedReader userReader = new BufferedReader(new InputStreamReader(userInputStream));
                            StringBuilder userResponse = new StringBuilder();
                            String userLine;
                            while ((userLine = userReader.readLine()) != null) {
                                userResponse.append(userLine);
                            }
                            userReader.close();
                            userInputStream.close();

                            // Parse the user information response
                            JSONObject userJsonResponse = new JSONObject(userResponse.toString());

                            Log.d("Server Response", "User JSON Response: " + userJsonResponse.toString());

                            String displayName = userJsonResponse.optString("displayName");
                            String image = userJsonResponse.optString("profilePic");

                            // Save user information and navigate to MainActivity
                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                            preferenceManager.putString(Constants.KEY_USERNAME, username);
                            preferenceManager.putString(Constants.KEY_DISPLAY, displayName);
                            preferenceManager.putString(Constants.KEY_IMAGE, image);
                            preferenceManager.putString(Constants.KEY_FCM_TOKEN, token);

                            Log.d("Server Response", "Data received: " + username + " " + displayName + " " + image);

                            runOnUiThread(() -> {
                                showToast("Login successful");
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            });
                        } else {
                            // Failed to fetch user information
                            runOnUiThread(() -> {
                                showToast("Failed to fetch user information");
                                loading(false);
                            });
                        }

                        userConnection.disconnect();
                    } else {
                        // Token is null
                        runOnUiThread(() -> {
                            showToast("Token not received");
                            loading(false);
                        });
                    }
                } else {
                    // Login failed
                    runOnUiThread(() -> {
                        showToast("Wrong username or password");
                        loading(false);
                    });
                }

                // Disconnect from the server
                connection.disconnect();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                String errorMessage = e.getMessage();
                runOnUiThread(() -> {
                    showToast("Error occurred during login: " + errorMessage);
                    loading(false);
                });
            }
        }).start();
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignIn.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidSignInDetails() {
        if (binding.inputUsername.getText().toString().trim().isEmpty()) {
            showToast("Enter username");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter password");
            return false;
        } else {
            return true;
        }
    }
}
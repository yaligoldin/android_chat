package com.example.chatter.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatter.R;
import com.example.chatter.databinding.ActivityUsersBinding;
import com.example.chatter.listeners.ConversionListener;
import com.example.chatter.models.User;
import com.example.chatter.utilities.Constants;
import com.example.chatter.utilities.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UsersActivity extends AppCompatActivity implements ConversionListener {
    private EditText editTextSearch;
    private Button buttonAddChat;
    private ActivityUsersBinding binding;

    private PreferenceManager preferenceManager;
    private OkHttpClient client;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        editTextSearch = findViewById(R.id.editTextSearch);
        buttonAddChat = findViewById(R.id.buttonAddChat);
        preferenceManager = new PreferenceManager(getApplicationContext());
        client = new OkHttpClient();
        executorService = Executors.newSingleThreadExecutor();
        setListeners();
        buttonAddChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextSearch.getText().toString().trim();
                if (!username.isEmpty()) {
                    sendAddChatRequest(username, getApplicationContext());
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                } else {
                    Toast.makeText(UsersActivity.this, "Please enter a username", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
    private String getServerAddress() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("ServerAddress", "");
    }

    private void sendAddChatRequest(String username, final Context context) {
        String fcmToken = preferenceManager.getString(Constants.KEY_FCM_TOKEN);
        OkHttpClient client = new OkHttpClient();

// Create the request body
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("username", username);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, requestBody.toString());

// Create the request
        String serverAddress = getServerAddress();
        String apiEndpoint = serverAddress + "api/Chats";
        Request request = new Request.Builder()
                .url(apiEndpoint)
                .addHeader("authorization", "Bearer " + fcmToken)
                .post(body)
                .build();

// Send the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(UsersActivity.this, "Chat added successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(UsersActivity.this, "Username doesn't exist", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(UsersActivity.this, "Username doesn't exist", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }
    @Override
    public void onConversionClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }

}

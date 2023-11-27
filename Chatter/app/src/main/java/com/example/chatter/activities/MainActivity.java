package com.example.chatter.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatter.R;
import com.example.chatter.adapters.RecentConversationsAdapter;
import com.example.chatter.databinding.ActivityMainBinding;
import com.example.chatter.listeners.ConversionListener;
import com.example.chatter.models.ChatMessage;
import com.example.chatter.models.User;
import com.example.chatter.utilities.Constants;
import com.example.chatter.utilities.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements ConversionListener {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations;

    private RecentConversationsAdapter conversationsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        init();
        loadUserDetails();
        setListeners();
        fetchChatsFromServer(preferenceManager.getString(Constants.KEY_FCM_TOKEN));
    }

    private void setListeners() {
        binding.imageSignOut.setOnClickListener(y -> signOut());
        binding.fabNewChat.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), UsersActivity.class)));
        binding.fabSettings.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class)));
    }

    private void loadUserDetails() {
        binding.textName.setText(preferenceManager.getString(Constants.KEY_DISPLAY));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }

    private void init() {
        conversations = new ArrayList<>();
        conversationsAdapter = new RecentConversationsAdapter(conversations, this);
        binding.conversationsRecyclerView.setAdapter(conversationsAdapter);
    }

    public void onConversionClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }

    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogTheme);
        builder.setTitle("Confirmation")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performLogout();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    private void performLogout() {
        preferenceManager.clear();
        startActivity(new Intent(getApplicationContext(), SignInActivity.class));
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void fetchChatsFromServer(String token) {

        OkHttpClient client = new OkHttpClient();
        String serverAddress = getServerAddress();
        String apiEndpoint = serverAddress + "api/Chats";
        // Create a request builder
        Request.Builder requestBuilder = new Request.Builder()
                .url(apiEndpoint)
                .addHeader("authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json");

        // Build the request
        Request request = requestBuilder.build();

        // Execute the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle network request failure
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.isSuccessful()) {

                    // Parse the response body and extract the chat data
                    String responseBody = response.body().string();
                    try {
                        JSONArray jsonArray = new JSONArray(responseBody);
                        List<ChatMessage> chatMessages = new ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonChat = jsonArray.getJSONObject(i);
                            Log.d("Chats: ", String.valueOf(jsonChat));
                            String id = jsonChat.getString("id");
                            JSONObject userObject = jsonChat.getJSONObject("user");
                            String userId = userObject.getString("_id");
                            String username = userObject.getString("username");
                            String displayName = userObject.getString("displayName");
                            String profilePic = userObject.getString("profilePic");
                            String[] messageData = getLastMessage(id);
                            String lastMessage = messageData[0];
                            Date date = convertStringToDate(messageData[1]);
                            ChatMessage chatMessage = new ChatMessage(id, userId, username, displayName, profilePic, lastMessage, date);

                            chatMessages.add(chatMessage);
                        }

                        // Update the conversations list with the fetched chats
                        conversations.clear();
                        conversations.addAll(chatMessages);

                        Collections.sort(conversations, (obj1, obj2) -> {
                            Date date1 = obj1.getDateObject();
                            Date date2 = obj2.getDateObject();

                            if (date1 == null && date2 == null) {
                                return 0;
                            } else if (date1 == null) {
                                return 1; // obj1 is considered greater
                            } else if (date2 == null) {
                                return -1; // obj2 is considered greater
                            } else {
                                return date2.compareTo(date1); // Sort in descending order
                            }
                        });

                        // Update the conversationsAdapter and notify the adapter about the data change
                        runOnUiThread(() -> {
                            conversationsAdapter.notifyDataSetChanged();
                            binding.conversationsRecyclerView.smoothScrollToPosition(0);
                            binding.conversationsRecyclerView.setVisibility(View.VISIBLE);
                            binding.progressBar.setVisibility(View.GONE);
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Handle non-successful response (e.g., display an error message)
                    final String error = response.message();
                    runOnUiThread(() -> showToast(error));
                }
            }
        });
    }

    private String getServerAddress() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("ServerAddress", "");
    }

    private String[] getLastMessage(String chatId) {
        String[] messageData = new String[2];
        String content = "";
        String date = "";
        String serverAddress = getServerAddress();
        String apiEndpoint = serverAddress + "api/Chats/";
        String url = apiEndpoint + chatId + "/Messages";
        String token = preferenceManager.getString(Constants.KEY_FCM_TOKEN);
        String authorization = "Bearer " + token;

        try {
            URL apiUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("authorization", authorization);
            connection.setRequestProperty("Content-Type", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    response.append(line);
                }
                bufferedReader.close();

                // Parse the response JSON and update the messages list
                JSONArray jsonArray = new JSONArray(response.toString());
                if (jsonArray.length() > 0) {
                    JSONObject lastMessage = jsonArray.getJSONObject(jsonArray.length() - 1);
                    content = lastMessage.getString("content");
                    date = lastMessage.getString("created");
                }
            } else {
                Log.e("MainActivity", "Failed to fetch messages. Response code: " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("MainActivity", "Error fetching messages: " + e.getMessage());
        }
        messageData[0] = content;
        messageData[1] = date;

        return messageData;
    }

    public static Date convertStringToDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null; // Return null if the parsing fails
        }
    }
}
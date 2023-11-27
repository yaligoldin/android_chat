package com.example.chatter.activities;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatter.adapters.ChatAdapter;
import com.example.chatter.databinding.ActivityChatBinding;
import com.example.chatter.models.ChatMessage;
import com.example.chatter.models.User;
import com.example.chatter.utilities.Constants;
import com.example.chatter.utilities.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
public class ChatActivity extends AppCompatActivity {
    private ActivityChatBinding binding;
    private User receiverUser;
    private String receiverID = null;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private String conversationId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadReceiverDetails();
        init();
        listenMessages();
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                getBitmapFromEncodedString(receiverUser.image),
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);
    }

    private String getServerAddress() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("ServerAddress", "");
    }

    private void listenMessages() {
        String serverAddress = getServerAddress();
        String apiEndpoint = serverAddress + "api/Chats/";
        String url = apiEndpoint + receiverID + "/Messages";
        String token = preferenceManager.getString(Constants.KEY_FCM_TOKEN);
        String authorization = "Bearer " + token;
        Log.d("DONALD", url + " and " + authorization);

        try {
            Log.d("TRUMP", "OK2");
            URL apiUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("authorization", authorization);
            connection.setRequestProperty("Content-Type", "application/json");
            Log.d("TRUMP", "OK3");

            int responseCode = connection.getResponseCode();
            Log.d("TRUMP", "OK4");

            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.d("TRUMP", "OK");

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

                // Clear the existing chatMessages list
                chatMessages.clear();

                // Process the jsonArray and update chatMessages accordingly
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject messageJson = jsonArray.getJSONObject(i);
                    Log.d("TRUMP", String.valueOf(messageJson));
                    // Extract the required message data from the JSON object
                    String messageId = messageJson.getString("id");
                    String senderId = messageJson.getJSONObject("sender").getString("id");
                    String senderUsername = messageJson.getJSONObject("sender").getString("username");
                    String content = messageJson.getString("content");
                    String created = messageJson.getString("created");

                    // Create a ChatMessage object with the extracted data
                    ChatMessage chatMessage = new ChatMessage(messageId, senderId, senderUsername, null, null, content, null);

                    // Add the chatMessage to the list
                    chatMessages.add(chatMessage);
                }

                // Sort the chatMessages based on the dateObject

                // Update the UI to display the chatMessages
                chatAdapter.notifyDataSetChanged();
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            } else {
                Log.e("ChatActivity", "Failed to fetch messages. Response code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //
    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    //
    private void loadReceiverDetails() {
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        receiverID = receiverUser.id.toString();
        binding.textName.setText(receiverUser.displayName);
    }

    //
    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
//
    }
}
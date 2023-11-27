package com.example.chatter.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.chatter.R;
import com.example.chatter.databinding.ActivitySettingsBinding;
import com.example.chatter.utilities.Constants;
import com.example.chatter.utilities.PreferenceManager;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;
    private EditText editServerAddress;

    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();

        // Set the initial state of the switch
        boolean isDarkModeEnabled = isDarkModeEnabled();
        binding.switchDarkMode.setChecked(isDarkModeEnabled);
        SharedPreferences sharedPreferences = getSharedPreferences("ThemePrefs", MODE_PRIVATE);

        int savedThemeMode = sharedPreferences.getInt("ThemeMode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedThemeMode);

        editServerAddress = findViewById(R.id.editServerAddress);
        String savedServerAddress = getServerAddress();
        editServerAddress.setText(savedServerAddress);
    }
    private boolean isDarkModeEnabled() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }
    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());

        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                enableDarkMode();
            } else {
                disableDarkMode();
            }
        });
        binding.buttonSave.setOnClickListener(v -> {
            if (!getServerAddress().equals(editServerAddress.getText().toString())) {
                saveServerAddress(editServerAddress.getText().toString());
                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, false);
                // Start SignInActivity
                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                startActivity(intent);
                finish();
            }
            else {
                onBackPressed();
            }
        });

    }
    private String getServerAddress() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("ServerAddress", "");
    }

    private void saveServerAddress(String serverAddress) {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("ServerAddress", serverAddress);
        editor.apply();
    }
    private void enableDarkMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        saveThemePreference(AppCompatDelegate.MODE_NIGHT_YES);
    }

    private void disableDarkMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        saveThemePreference(AppCompatDelegate.MODE_NIGHT_NO);
    }

    private void saveThemePreference(int themeMode) {
        SharedPreferences sharedPreferences = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("ThemeMode", themeMode);
        editor.apply();
    }

}

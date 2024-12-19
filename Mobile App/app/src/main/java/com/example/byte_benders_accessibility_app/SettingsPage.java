package com.example.byte_benders_accessibility_app;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import java.util.Locale;

public class SettingsPage extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private GestureDetectorCompat gestureDetector;
    private TextToSpeech tts;
    private ViewPager2 viewPager;

    private String[] currentSettings; // Store current settings
    private String[][] options;       // Store options for each setting
    private String[] cardContents;    // Store descriptions with current values
    private String[] settingNames;    // Store names of the settings


    private int currentItem = -1;     // The currently selected item in ViewPager
    private int currentOptionIndex = 0; // Track the selected option index
    private boolean isChoosingOption = false; // Whether the user is in selection mode

    private SettingsAdapter adapter; // Adapter for ViewPager2

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings_page);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize gesture detector with custom listener
        gestureDetector = new GestureDetectorCompat(this, new CustomGestureListener());

        // Initialize TextToSpeech
        tts = new TextToSpeech(this, this);

        settingNames = new String[]{
                "Language",
                "Voice",
                "Favorite Location"
        };

        // Default settings
        currentSettings = new String[]{
                "Language: English",
                "Voice: Female",
                "Favorite Location: Current Location"
        };

        // Options for each setting
        options = new String[][]{
                {"English", "Spanish", "French"},
                {"Female", "Male"},
                {"Current Location", "Previous Location"}
        };

        // Initialize ViewPager2 with descriptions and options
        updateCardContents();

        viewPager = findViewById(R.id.viewPager);
        adapter = new SettingsAdapter(this, cardContents);
        viewPager.setAdapter(adapter);
    }

    // Updates card contents dynamically with current settings
    private void updateCardContents() {
        cardContents = new String[]{
                "Language Settings<br>Description: Change the language of the application<br>Options: English, Spanish, French<br>Current Setting: " + currentSettings[0],
                "Change Voice<br>Description: Change the voice of the application<br>Options: Female, Male<br>Current Setting: " + currentSettings[1],
                "Favorite Location<br>Description: Set your favorite location<br>Options: Current Location, Previous Location<br>Current Setting: " + currentSettings[2]
        };

        // Notify the adapter to refresh data
        if (adapter != null) {
            adapter.updateData(cardContents);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isChoosingOption) {
            // Navigate options using volume keys
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                currentOptionIndex = (currentOptionIndex + 1) % options[currentItem].length;
                speak("Option: " + options[currentItem][currentOptionIndex]);
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                currentOptionIndex = (currentOptionIndex - 1 + options[currentItem].length) % options[currentItem].length;
                speak("Option: " + options[currentItem][currentOptionIndex]);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);
            speak("You are on the settings page. Swipe left or right to navigate through settings. Single tap to choose a new setting. Press and hold to hear the details.");
        } else {
            // Handle initialization error
        }
    }

    private void speak(String text) {
        if (tts != null && !tts.isSpeaking()) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private class CustomGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100; // Minimum distance for a swipe
        private static final int SWIPE_VELOCITY_THRESHOLD = 100; // Minimum velocity for a swipe

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            currentItem = viewPager.getCurrentItem();
            if (currentItem >= 0 && currentItem < options.length) {
                if (!isChoosingOption) {
                    isChoosingOption = true;
                    currentOptionIndex = 0; // Reset to first option
                    speak("Choose a new setting for " + settingNames[currentItem] + ". Use volume keys to change options, and tap again to confirm.");
                } else {
                    currentSettings[currentItem] = options[currentItem][currentOptionIndex];
                    speak("Setting updated to " + currentSettings[currentItem]);
                    isChoosingOption = false;
                    updateCardContents();
                }
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            currentItem = viewPager.getCurrentItem();
            if (currentItem >= 0 && currentItem < cardContents.length) {
                speak(cardContents[currentItem].replace("<br>", "\n"));
            }
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();

            // Check for swipe up
            if (Math.abs(diffY) > Math.abs(diffX) && diffY < -SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                onSwipeUp();
                return true;
            }
            return false;
        }

        private void onSwipeUp() {
            speak("Navigating back to the home page.");
            finish(); // Close the current activity
        }
    }

}

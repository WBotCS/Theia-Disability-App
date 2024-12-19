package com.example.byte_benders_accessibility_app;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;
import android.speech.tts.UtteranceProgressListener;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class NavigationPage extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private GestureDetectorCompat gestureDetector;
    private TextToSpeech tts;
    private ViewPager2 viewPager;

    private TextView subtitleTextView;

    private boolean isVolumeDownPressed = false;
    private boolean isPowerButtonPressed = false;

    private List<Building> buildings = Arrays.asList(
            new Building("Spark 203", "Distance: 200 feet ", "Estimate Walking time: 2 minutes "),
            new Building("Spark 301", "Distance: 250 feet ", "Estimate Walking time: 3 minutes","shorter route with stairs", "longer route with elevator"),
            new Building("Spark 1st floor Bathroom", "Distance: 150 feet ", "Estimate Walking time: 1 minutes")
    );

    // -1: no selection, 0: first route, 1: second route
    private int selectedRoute = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_navigation_page);

        // Initialize subtitleTextView
        subtitleTextView = findViewById(R.id.subtitleTextView);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        gestureDetector = new GestureDetectorCompat(this, new CustomGestureListener());

        // Initialize TTS
        tts = new TextToSpeech(this, this);

        // Set up ViewPager2
        viewPager = findViewById(R.id.viewPager);
        if (viewPager != null) {
            viewPager.setAdapter(new CardAdapter(this, buildings));
        } else {

        }

        // Set up Start Route button
        Button startRouteButton = findViewById(R.id.startRouteButton);
        startRouteButton.setOnClickListener(v -> {
            int position = viewPager.getCurrentItem();
            Building building = buildings.get(position);
            if (building.getName().equals("Spark 1st floor Bathroom")) {
                String directions = "Starting route to Spark 1st floor Bathroom from Spark main entrance. " +
                        "Go straight 15 feet. " +
                        "Turn left and proceed 10 feet. "+
                        "Turn right and proceed 25 feet. "+
                        "Turn left and proceed 20 feet. "+
                        "Turn right and proceed 10 feet. "+
                        "Turn right and proceed 8 feet. "+
                        "You have arrived at Spark 1st floor Bathroom. ";
                speakWithPauses(directions);
            } else if (building.getName().equals("Spark 203")) {
                String directions = "Starting route to Spark room 203 from Spark main entrance. " +
                        "Walk straight for 12 feet, caution there is a stair case ahead. " +
                        "Continue walking up the 6 steps of stairs. " +
                        "On your left is another stair case, proceed up the 11 steps of stairs. " +
                        "Take a right and walk 25 feet straight. " +
                        "Take a left and walk 20 feet straight.  " +
                        "You have reached your destination. Spark Room 203 is on your right. ";
                speakWithPauses(directions);
            } else if (building.getShorterRoute() != null && building.getLongerRoute() != null) {
                selectedRoute = -1; // Reset selection
                speak("Which route would you like to take to " + building.getName() +
                        "? Two routes found. The first route will be quicker but requires multiple stair cases. " +
                        "The second route will take longer but uses the elevator. " +
                        "Use the volume rocker to choose. Increase volume for the first route and decrease volume for the second route.");
            } else {
                speak("Start route to " + building.getName());
            }
        });
    }

    private void showSubtitle(String text) {
        runOnUiThread(() -> {
            subtitleTextView.setText(text);
            subtitleTextView.setVisibility(View.VISIBLE);
        });
    }

    private void hideSubtitle() {
        runOnUiThread(() -> subtitleTextView.setVisibility(View.GONE));
    }


    private void interruptAndGuideBack() {
        // Stop ongoing TTS
        if (tts != null && tts.isSpeaking()) {
            tts.stop();
        }

        // Start guiding the user back to the starting point
        String backDirections = "Stopping current directions. Guiding you back to Spark main entrance. " +
                "Turn around and walk straight for 10 feet. " +
                "Continue straight for another 15 feet. " +
                "Turn left and proceed for 10 feet. " +
                "You have arrived back at Spark main entrance.";
        speakWithPauses(backDirections);
    }

    // Helper method to speak text with pauses
    public void speakWithPauses(String text) {
        if (tts != null) {
            // Split the text into sentences
            String[] sentences = text.split("\\.\\s+");

            // Set the UtteranceProgressListener before speaking
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    // Show subtitle for the current sentence
                    runOnUiThread(() -> showSubtitle(utteranceId));
                }

                @Override
                public void onDone(String utteranceId) {
                    // Hide subtitle after the sentence is spoken
                    runOnUiThread(() -> hideSubtitle());
                }

                @Override
                public void onError(String utteranceId) {
                    // Handle errors if necessary
                }
            });

            // Queue each sentence with a unique utterance ID
            for (int i = 0; i < sentences.length; i++) {
                String sentence = sentences[i].trim();
                String utteranceId = sentence; // Use the sentence itself as the utterance ID for simplicity
                tts.speak(sentence, TextToSpeech.QUEUE_ADD, null, utteranceId);

                // Add a silent pause after each sentence
                tts.playSilentUtterance(1000, TextToSpeech.QUEUE_ADD, null);
            }
        }
    }




    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            isVolumeDownPressed = true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            isPowerButtonPressed = true;
        }
        // Check if both buttons are pressed simultaneously
        if (isVolumeDownPressed && isPowerButtonPressed) {
            interruptAndGuideBack();
            return true; // Consume the event
        }
        if (selectedRoute == -1) {
            int position = viewPager.getCurrentItem();
            Building building = buildings.get(position);
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                selectedRoute = 0;
                if (building.getName().equals("Spark 301")) {
                    String directions = "Starting route to Spark 301. Using the first option." +
                            "Walk straight for 12 feet, caution there is a stair case ahead. " +
                            "Continue walking up the 6 steps of stairs. " +
                            "On your left is another stair case, proceed up the 11 steps of stairs. " +
                            "Proceed forward 5 feet until you reach another stair case. " +
                            "Proceed up the 12 steps of stairs. " +
                            "Take a left and proceed 8 feet. " +
                            "Take a left and proceed 25 feet. " +
                            "You have reached Spark 301. The entrance should be on your right. ";
                    speakWithPauses(directions);
                } else {
                    speak("Route 1 is chosen, starting route to " + building.getName());
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                selectedRoute = 1;
                if (building.getName().equals("Spark 301")) {
                    String directions = "Starting route to Spark 301. Using the second option." +
                            "Go straight 15 feet. " +
                            "Turn left and proceed 10 feet. "+
                            "Turn right and proceed 25 feet. "+
                            "Go straight 18 feet. "+
                            "Elevators should be on your right. "+
                            "Take elevator to 3rd floor. " +
                            "Exit the elevator and take a left. "+
                            "Proceed 15 feet. "+
                            "You have reached Spark 301. It is straight ahead. ";
                    speakWithPauses(directions);
                } else {
                    speak("Route 2 is chosen, starting route to " + building.getName());
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    // --- TextToSpeech.OnInitListener implementation ---

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            isVolumeDownPressed = false;
        } else if (keyCode == KeyEvent.KEYCODE_POWER) {
            isPowerButtonPressed = false;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Handle language not supported (e.g., show a message)
            } else {
                speak("You are at the navigation page. Swipe left or right to explore recommended locations. "
                        + "Your current location is Spark Main Entrance. " + "To go back to the Home Page. " +
                        "Swipe down from the top of the screen. " + "To start a route to a location, tap on an area on the bottom right of the screen.");
            }
        } else {
            // Handle initialization failed (e.g., show a message)
        }
    }

    // --- Helper method to speak text ---

    public void speak(String text) {
        if (tts != null && !tts.isSpeaking()) {
            String utteranceId = "UTTERANCE_SINGLE";
            showSubtitle(text); // Show the subtitle
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);

            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    // Show subtitle during speaking
                }

                @Override
                public void onDone(String utteranceId) {
                    runOnUiThread(() -> hideSubtitle()); // Hide subtitle after speaking
                }

                @Override
                public void onError(String utteranceId) {

                }
            });
        }
    }



    // --- Release TTS resources ---

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private class CustomGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            int position = viewPager.getCurrentItem();
            Building building = buildings.get(position);
            speak(building.getName() + building.getDistance() + building.getWalkingTime());
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffY) > Math.abs(diffX)) {
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeDown();
                    }
                }
            }
            return true;
        }
    }

    private void onSwipeDown() {
        // Navigate back to the homepage
        finish();
    }


}
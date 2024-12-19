package com.example.byte_benders_accessibility_app;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;
public class SecondActivity extends AppCompatActivity implements GestureDetector.OnGestureListener, TextToSpeech.OnInitListener {

    private GestureDetectorCompat gestureDetector;
    private TextToSpeech tts;
    private TextView subtitleTextView;

    private static final String DEFAULT_TEXT = "Hi, How Can I Help You?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_second);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        gestureDetector = new GestureDetectorCompat(this, this);

        // Initialize TTS
        tts = new TextToSpeech(this, this);

        // Initialize subtitle TextView
        subtitleTextView = findViewById(R.id.textView3);

        // Set default text
        subtitleTextView.setText(DEFAULT_TEXT);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    // --- TextToSpeech.OnInitListener implementation ---
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                subtitleTextView.setText("TTS Language not supported.");
            } else {
                String welcomeMessage = "You are at the home page now. " +
                        "Swipe up from the bottom of the screen to navigate to the navigation page. " +
                        "Swipe down from the top of the screen to go to the settings page. " +
                        "Microphone indicator is located at the bottom left of the screen.";
                speak(welcomeMessage);
            }
        } else {
            subtitleTextView.setText("TTS Initialization failed.");
        }
    }

    // --- Helper method to speak text ---
    private void speak(String text) {
        // Define a maximum length for each chunk
        final int MAX_LENGTH = 40;

        // Split the text into sentences
        String[] sentences = text.split("\\.\\s+");

        // Set up TTS Utterance Progress Listener
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                runOnUiThread(() -> subtitleTextView.setText(utteranceId)); // Show the current chunk
            }

            @Override
            public void onDone(String utteranceId) {
                runOnUiThread(() -> subtitleTextView.setText(DEFAULT_TEXT)); // Reset to default text after the last chunk
            }

            @Override
            public void onError(String utteranceId) {
                runOnUiThread(() -> subtitleTextView.setText("Error speaking the text."));
            }
        });

        // Process each sentence and break into chunks without splitting words
        for (String sentence : sentences) {
            if (sentence.length() > MAX_LENGTH) {
                // Split the sentence into chunks, ensuring words are not split
                int start = 0;
                while (start < sentence.length()) {
                    int end = Math.min(start + MAX_LENGTH, sentence.length());

                    // Ensure we do not split a word
                    if (end < sentence.length() && !Character.isWhitespace(sentence.charAt(end))) {
                        end = sentence.lastIndexOf(" ", end);
                        if (end <= start) { // Handle edge case where no spaces are found
                            end = Math.min(start + MAX_LENGTH, sentence.length());
                        }
                    }

                    String chunk = sentence.substring(start, end).trim();
                    tts.speak(chunk, TextToSpeech.QUEUE_ADD, null, chunk); // Use chunk as the utterance ID
                    start = end;
                }
            } else {
                tts.speak(sentence, TextToSpeech.QUEUE_ADD, null, sentence); // Use the sentence itself as the utterance ID
            }
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

    // --- Gesture Handling ---
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float diffY = e2.getY() - e1.getY();

        if (diffY < -100 && Math.abs(velocityY) > 100) { // Swipe up
            Intent intent = new Intent(SecondActivity.this, NavigationPage.class);
            startActivity(intent);
            return true;
        } else if (diffY > 100 && Math.abs(velocityY) > 100) { // Swipe down
            Intent intent = new Intent(SecondActivity.this, SettingsPage.class);
            startActivity(intent);
            return true;
        }
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // Not used
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // Not used
    }
}

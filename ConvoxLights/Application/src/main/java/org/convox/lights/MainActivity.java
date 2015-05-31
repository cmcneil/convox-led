/*
 * carson.mcneil@gmail.com
 */

package org.convox.lights;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.chiralcode.colorpicker.ColorPicker;

import org.convox.common.logger.Log;
import org.convox.common.logger.LogFragment;
import org.convox.common.logger.LogWrapper;
import org.convox.common.logger.MessageOnlyLogFilter;

import org.convox.lights.R;

import java.util.Arrays;

/**
 * App for controlling a Convox Light Server!
 */
public class MainActivity extends FragmentActivity {

    public static final String TAG = "Convox Lights";

    // Reference to the fragment showing events, so we can clear it with a button
    // as necessary.
    private LogFragment mLogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Read the preferences from the preference file.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);


        setContentView(R.layout.sample_main);

        // Initialize text fragment that displays intro text.
        SimpleTextFragment introFragment = (SimpleTextFragment)
                    getSupportFragmentManager().findFragmentById(R.id.intro_fragment);
        introFragment.setText(R.string.welcome_message);
        introFragment.getTextView().setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f);

        // Initialize the logging framework.
        initializeLogging();
        initColorListener();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ping_action:
                Log.i("CONVOX_LED", "Pinged the server!");
                ConvoxLEDUtils.pushLedPacket(228, 77, 96);
            // Clear the log view fragment.
            case R.id.clear_action:
              mLogFragment.getLogView().setText("Stufff");
              return true;
        }
        return false;
    }

    public void startSettings(MenuItem item) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }


    /** Create a chain of targets that will receive log data */
    public void initializeLogging() {

        // Using Log, front-end to the logging chain, emulates
        // android.util.log method signatures.

        // Wraps Android's native log framework
        LogWrapper logWrapper = new LogWrapper();
        Log.setLogNode(logWrapper);

        // A filter that strips out everything except the message text.
        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);

        // On screen logging via a fragment with a TextView.
        mLogFragment =
                (LogFragment) getSupportFragmentManager().findFragmentById(R.id.log_fragment);
        msgFilter.setNext(mLogFragment.getLogView());
    }

    public void initColorListener() {
        ColorPicker colorPicker = (ColorPicker) findViewById(R.id.color_picker);
        colorPicker.setColorSelectedListener(new ColorPicker.OnColorSelectedListener() {
            @Override
            public void onColorSelected(float[] color) {
                Log.i("CONVOX_LED", "Selected color: " + Arrays.toString(color));
                if (color.length >= 3) {
                    ConvoxLEDUtils.fillLights(color[0], color[1], color[2]);
                }
            }
        });
    }
}

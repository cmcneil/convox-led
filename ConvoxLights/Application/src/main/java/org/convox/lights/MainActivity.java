/*
 * carson.mcneil@gmail.com
 */

package org.convox.lights;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.chiralcode.colorpicker.ColorPicker;

/**
 * App for controlling a Convox Light Server!
 */
public class MainActivity extends FragmentActivity {

    public static final String TAG = "CONVOX_LIGHTS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Read the preferences from the preference file.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.main);

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
            case R.id.clear_action:
              return true;
        }
        return false;
    }

    public void startSettings(MenuItem item) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void initColorListener() {
        ColorPicker colorPicker = (ColorPicker) findViewById(R.id.color_picker);
        colorPicker.setColorSelectedListener(new ColorPicker.OnColorSelectedListener() {
            @Override
            public void onColorSelected(float[] color) {
                if (color.length >= 3) {
                    ConvoxLEDUtils.fillLights(color[0], color[1], color[2]);
                }
            }
        });
    }
}

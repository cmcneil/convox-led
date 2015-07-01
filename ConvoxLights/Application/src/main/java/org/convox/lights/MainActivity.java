/*
 * carson.mcneil@gmail.com
 */

package org.convox.lights;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.chiralcode.colorpicker.ColorPicker;
import com.jmedeisis.draglinearlayout.DragLinearLayout;

/**
 * App for controlling a Convox Light Server!
 */
public class MainActivity extends FragmentActivity {

    public static final String TAG = "CONVOX_LIGHTS";
    public String test = "test";
    private DragLinearLayout colorTray;
    private ColorDot mActiveColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Read the preferences from the preference file.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.main);

        initColorListeners();
        initColorTray();
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

    // Set up the tray so that all the children are draggable.
    private void initColorTray() {
        colorTray = (DragLinearLayout) findViewById(R.id.color_tray);
        // set all children draggable except the first (the header)
        for(int i = 0; i < colorTray.getChildCount(); i++){
            View child = colorTray.getChildAt(i);
            colorTray.setViewDraggable(child, child); // the child is its own drag handle
        }
    }

    private void initColorListeners() {
        ColorPicker colorPicker = (ColorPicker) findViewById(R.id.color_picker);
        colorPicker.setColorSelectedListener(new ColorPicker.OnColorSelectedListener() {
            @Override
            public void onColorSelected(float[] color) {
                if (color.length >= 3) {
//                    ConvoxLEDUtils.fillLights(color[0], color[1], color[2]);
                    if (mActiveColor != null) {
                        mActiveColor.setColor(color);
                    }
                    sendNewConfig();
                }
            }
        });

        colorPicker.setColorAddedListener(new ColorPicker.OnColorAddedListener() {
            @Override
            public void onColorAdded(float[] color) {
                // Construct a new color dot.
                ColorDot newColor = new ColorDot(MainActivity.this);
                newColor.setLayoutParams(
                        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.MATCH_PARENT));
                newColor.setColor(color);
                colorTray.addDragView(newColor, newColor);
                if (mActiveColor != null) {
                    mActiveColor.setNotSelected();
                }
                mActiveColor = newColor;
//                }
                newColor.setOnSelectedListener(new ColorDot.OnSelectedListener() {
                    @Override
                    public void onSelected(ColorDot selectedDot) {
                        if (mActiveColor != selectedDot) {
                            mActiveColor.setNotSelected();
                            mActiveColor = selectedDot;
                            mActiveColor.setIsSelected();
                        }
                    }
                });
                mActiveColor.setIsSelected();
            }
        });
    }

    private void sendNewConfig() {
        float[][] colors = new float[colorTray.getChildCount()][3];
        for(int i = 0; i < colorTray.getChildCount(); i++){
            ColorDot child = (ColorDot) colorTray.getChildAt(i);
            colors[i] = child.getColor();
        }
        ConvoxLEDUtils.sendConfig(colors);
    }
}

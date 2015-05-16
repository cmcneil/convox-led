/*
 * carson.mcneil@gmail.com
 */

package org.convox.lights;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import org.convox.lights.ConvoxLEDUtils;
import org.convox.lights.SimpleTextFragment;

import org.convox.common.logger.Log;
import org.convox.common.logger.LogFragment;
import org.convox.common.logger.LogWrapper;
import org.convox.common.logger.MessageOnlyLogFilter;

import org.convox.lights.R;

/**
 * App for controlling a Convox Light Server!
 */
public class MainActivity extends FragmentActivity {

    public static final String TAG = "Network Connect";

    // Reference to the fragment showing events, so we can clear it with a button
    // as necessary.
    private LogFragment mLogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_main);

        // Initialize text fragment that displays intro text.
        SimpleTextFragment introFragment = (SimpleTextFragment)
                    getSupportFragmentManager().findFragmentById(R.id.intro_fragment);
        introFragment.setText(R.string.welcome_message);
        introFragment.getTextView().setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f);

        // Initialize the logging framework.
        initializeLogging();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // When the user clicks FETCH, fetch the first 500 characters of
            // raw HTML from www.google.com.
            case R.id.fetch_action:
                Log.i("CONVOX_LED", "hit the fetch button");
                //writeRawContent("http://www.google.com");
                return true;
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
}

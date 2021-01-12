package com.garlic.websockettest;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This saves the setting in the sharded preferences, but this would be better done with the defaultSharedPreferences
 */
public class SettingsActivity extends AppCompatActivity {

    // TODO: this should be in a central dependency file
    public final static String PORT = "PORT";
    public final static String HOST = "HOST";
    public final static String PAUSE_CONNECTION = "PAUSE_CONNECTION";

    private String port;
    private String host;
    private boolean pauseConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null) {
            // If not saved changes were made (e.g. smartphone switches to landscape mode)
            port = savedInstanceState.getString(PORT);
            host = savedInstanceState.getString(HOST);
            pauseConnection = savedInstanceState.getBoolean(PAUSE_CONNECTION);
        }else{
            //
            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            port = sharedPref.getString(PORT, "8765");
            host = sharedPref.getString(HOST, "192.168.2.100");
            pauseConnection = sharedPref.getBoolean(PAUSE_CONNECTION, true);
        }

        initSettings();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        updateSettings();

        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(PORT, port);
        savedInstanceState.putString(HOST, host);
        savedInstanceState.putBoolean(PAUSE_CONNECTION, pauseConnection);
    }

    /**
     * Saves the settings in the shared preferences
     *
     * @param view
     */
    public void saveSettings(View view) {
        updateSettings();

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(PORT, port);
        editor.putString(HOST, host);
        editor.putBoolean(PAUSE_CONNECTION, pauseConnection);
        editor.apply();

        String pauseConnectionString = pauseConnection ? "paused" : "active";

        CharSequence text = "Settings saved (ws://"+host+":"+port+" - "+ pauseConnectionString;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this, text, duration);
        toast.show();
        finish();
    }

    /**
     * Initialises the setting TextViews elements with the current values
     */
    public void initSettings(){
        TextView portTextView = (TextView) findViewById(R.id.editTextPort);
        portTextView.setText(port);

        TextView hostTextView = (TextView) findViewById(R.id.editTextHost);
        hostTextView.setText(host);

        Switch pauseConnectionSwitch = (Switch) findViewById(R.id.pause_connection_switch);
        pauseConnectionSwitch.setChecked(pauseConnection);
    }

    private void updateSettings() {
        TextView portTextView = (TextView) findViewById(R.id.editTextPort);
        port = portTextView.getText().toString();

        TextView hostTextView = (TextView) findViewById(R.id.editTextHost);
        host = hostTextView.getText().toString();

        Switch pauseConnectionSwitch = (Switch) findViewById(R.id.pause_connection_switch);
        pauseConnection = pauseConnectionSwitch.isChecked();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            default:

                return super.onOptionsItemSelected(item);
        }
    }
}
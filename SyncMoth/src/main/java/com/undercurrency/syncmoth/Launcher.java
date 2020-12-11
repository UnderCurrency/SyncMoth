package com.undercurrency.syncmoth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class Launcher extends AppCompatActivity {

    /**
     * If {@link #SPLASH_DISPLAY_LENGTH} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */

    private final int SPLASH_DISPLAY_LENGTH = 6000;
    private TextView txvVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_launcher);
        txvVersion = findViewById(R.id.txv_version);
        txvVersion.setText(BuildConfig.VERSION_NAME);
        new Handler().postDelayed(() -> {
            Intent intent_main = new Intent(Launcher.this, MainActivity.class);
            startActivity(intent_main);
            finish();
        }, SPLASH_DISPLAY_LENGTH);
    }
}
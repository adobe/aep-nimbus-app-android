package com.adobe.marketing.mobile.sampleapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String LOG_TAG = "MenuActivity";

    Button btnCore;
    Button btnEdge;
    Button btnEdgeIdentity;
    Button btnConsent;
    Button btnAssurance;
    Button btnMessaging;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Get the URI from the data elements of the applications intent com.adobe.marketing.mobile.sampleapp.MainActivity
        final Intent intent = getIntent();
        final Uri data = intent.getData();

        if (data != null) {
            Log.d(LOG_TAG, "Connected successfully to Assurance with the URI : " + data.toString());
        }

        btnCore = findViewById(R.id.btn_core);
        btnEdge = findViewById(R.id.btn_edge);
        btnAssurance = findViewById(R.id.btn_assurance);
        btnEdgeIdentity = findViewById(R.id.btn_edgeidentity);
        btnConsent = findViewById(R.id.btn_consent);
        btnMessaging = findViewById(R.id.btn_messaging);

        btnCore.setOnClickListener(this);
        btnEdge.setOnClickListener(this);
        btnAssurance.setOnClickListener(this);
        btnEdgeIdentity.setOnClickListener(this);
        btnConsent.setOnClickListener(this);
        btnMessaging.setOnClickListener(this);

        askNotificationPermission();
    }

    private void openMainActivity(int tab) {
        Intent intent = new Intent(MenuActivity.this, MainActivity.class);
        intent.putExtra(AppConstants.INTENT_TAB_KEY,  tab);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_core: openMainActivity(0);
                break;
            case R.id.btn_edge: openMainActivity(1);
                break;
            case R.id.btn_edgeidentity: openMainActivity(2);
                break;
            case R.id.btn_consent: openMainActivity(3);
                break;
            case R.id.btn_assurance: openMainActivity(4);
                break;
            case R.id.btn_messaging: openMainActivity(5);
                break;
            default:
                break;
        }
    }

    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
                Log.d(LOG_TAG, "Notification permission granted");
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                Log.d(LOG_TAG, "Notification Permission: Not granted");
                showNotificationPermissionRationale();
            } else {
                // Directly ask for the permission
                Log.d(LOG_TAG, "Requesting notification permission");
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            Log.d(LOG_TAG, "Notification permission granted");
            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // FCM SDK (and your app) can post notifications.
                    Log.d(LOG_TAG, "Notification permission granted");
                    Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    if (Build.VERSION.SDK_INT >= 33) {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                            showNotificationPermissionRationale();
                        } else {
                            Log.d(LOG_TAG, "Grant notification permission from settings");
                            Toast.makeText(this, "Grant notification permission from settings", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });


    private void showNotificationPermissionRationale() {
        new AlertDialog.Builder(this)
                .setTitle("Grant notification permission")
                .setMessage("Notification permission is required to show notifications")
                .setPositiveButton("Ok", (dialog, which) -> {
                    if (Build.VERSION.SDK_INT >= 33) {
                        requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}

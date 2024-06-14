package com.adobe.marketing.mobile.sampleapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.adobe.marketing.mobile.AdobeCallback;
import com.adobe.marketing.mobile.Messaging;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.edge.identity.Identity;
import com.adobe.marketing.mobile.services.ServiceProvider;

public class MessageTab extends Fragment {
    TextView tvECID;
    TextView tvAppSurface;
    EditText editTextCustomAction;
    Button buttonCustomIam;
    Button buttonRefreshIam;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messaging_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvECID = view.findViewById(R.id.tv_lbl_ecidLabel);
        tvAppSurface = view.findViewById(R.id.tvAppSurface);
        editTextCustomAction = view.findViewById(R.id.editTextCustomAction);
        buttonCustomIam = view.findViewById(R.id.buttonTriggerCustomIAM);
        buttonRefreshIam = view.findViewById(R.id.buttonRefreshInAppMessages);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Activity activity = getActivity();

        if (activity == null) return;

        Identity.getExperienceCloudId(new AdobeCallback<String>() {
            @Override
            public void call(final String ecid) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String text = String.format("Messaging SDK setup is complete with ECID - %s. \n\nFor more details please take a look at the documentation in the github repository.", ecid);
                        tvECID.setText(text);
                    }
                });
            }
        });

        // setup app surface text view
        tvAppSurface.setText(String.format("Retrieved in-app messages for app surface: mobileapp://%s", BuildConfig.APPLICATION_ID));

        // setup button click listeners
        buttonCustomIam.setOnClickListener(v -> {
            final String customAction = editTextCustomAction.getText().toString();
            MobileCore.trackAction(customAction, null);
        });

        buttonRefreshIam.setOnClickListener(v -> {
            Messaging.refreshInAppMessages();
            Toast.makeText(ServiceProvider.getInstance().getAppContextService().getApplicationContext(), "Refreshing in-app messages.", Toast.LENGTH_SHORT);
        });
    }
}
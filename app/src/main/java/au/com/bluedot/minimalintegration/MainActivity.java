package au.com.bluedot.minimalintegration;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import au.com.bluedot.point.net.engine.BDError;
import au.com.bluedot.point.net.engine.ServiceManager;
import au.com.bluedot.point.net.engine.TempoService;

/**
 * @author Bluedot Innovation
 * Copyright (c) 2025 Bluedot Innovation. All rights reserved.
 */
public class MainActivity extends Activity implements View.OnClickListener{

    EditText etProjectId;
    EditText etDestinationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etProjectId = findViewById(R.id.etProjectId);
        etDestinationId = findViewById(R.id.etDestinationId);
    }

    @Override protected void onStart() {
        super.onStart();
        Button init = findViewById(R.id.bInit);
        init.setEnabled(
                !ServiceManager.getInstance(getApplicationContext()).isBluedotServiceInitialized());
    }

    @Override
    public void onClick(View v) {
        int ID = v.getId();
        MainApplication mainApplication = (MainApplication) getApplicationContext();
        switch (ID) {
            case R.id.bInit:
                mainApplication.initPointSDK(etProjectId.getText().toString());
                break;

            case R.id.bReset:
                mainApplication.reset();
                break;

            case R.id.bStartGeoT:
                mainApplication.startGeoTrigger();
                break;

            case R.id.bStartBGGeoT:
                mainApplication.startGeoTrigger(true);
                break;

            case R.id.bStopGeoT:
                mainApplication.stopGeoTrigger();
                break;

            case R.id.bStartTempo:
                mainApplication.startTempo(etDestinationId.getText().toString());
                break;

            case R.id.bStopTempo:
                BDError bdError = TempoService.stop(getApplicationContext());
                String text = "Tempo stop";
                if (bdError != null) {
                    text = text + bdError.getReason();
                } else {
                    text = text + "Success";
                }
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
                break;


            case R.id.bSwitchProject:
                String switchProjectId = etProjectId.getText().toString();
                if(!switchProjectId.isEmpty()) {
                    mainApplication.switchProject(switchProjectId);
                } else {
                    Toast.makeText(this, "Please enter a project Id", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.bCurrentProjectId:
                String projectId = etProjectId.getText().toString();
                if(!projectId.isEmpty()) {
                    boolean isCurrent = ServiceManager.getInstance(getApplicationContext()).isCurrentProjectId(projectId);
                    Log.i("MinApp", "Current project id is "+ isCurrent);
                    Toast.makeText(this, "Is " + projectId + " current? " + isCurrent, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Please enter a project Id", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
    }

}

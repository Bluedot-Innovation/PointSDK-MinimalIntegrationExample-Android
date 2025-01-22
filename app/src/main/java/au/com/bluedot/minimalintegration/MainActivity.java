package au.com.bluedot.minimalintegration;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import au.com.bluedot.point.net.engine.BDError;
import au.com.bluedot.point.net.engine.ServiceManager;
import au.com.bluedot.point.net.engine.TempoService;

/*
 * @author Bluedot Innovation
 * Copyright (c) 2018 Bluedot Innovation. All rights reserved.
 */
public class MainActivity extends Activity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                mainApplication.initPointSDK();
                break;

            case R.id.bReset:
                mainApplication.reset();
                break;

            case R.id.bStartGeoT:
                mainApplication.startGeoTrigger();
                break;

            case R.id.bStopGeoT:
                mainApplication.stopGeoTrigger();
                break;

            case R.id.bStartTempo:
                mainApplication.startTempo();
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

            case R.id.bBrainChatAI:
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                this.startActivity(intent);
                break;
            default:
                break;
        }
    }

}

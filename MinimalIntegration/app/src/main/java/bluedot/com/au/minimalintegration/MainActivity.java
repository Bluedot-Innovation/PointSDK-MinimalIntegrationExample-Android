package bluedot.com.au.minimalintegration;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/*
 * @author Bluedot Innovation
 * Copyright (c) 2018 Bluedot Innovation. All rights reserved.
 */
public class MainActivity extends Activity implements View.OnClickListener{

    Button bFinishActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
    }

    private void initUI() {

        bFinishActivity = (Button) findViewById(R.id.bFinishActivity);

        bFinishActivity.setOnClickListener(this);
    }

    private void finishActivity(){
        finish();
    }

    @Override
    public void onClick(View v) {
        int ID = v.getId();
        switch (ID){
            case R.id.bFinishActivity:
                finishActivity();
                break;

        }
    }

}

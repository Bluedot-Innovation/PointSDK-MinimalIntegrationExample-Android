package bluedot.com.au.minimalintegration;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

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

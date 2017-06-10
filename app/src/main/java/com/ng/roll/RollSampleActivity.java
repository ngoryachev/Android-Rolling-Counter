package com.ng.roll;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Random;

public class RollSampleActivity extends FragmentActivity {

    private RollingCounterView rollingCounterView;
    private boolean autoRollDirection = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.roll_sample_activity);
        rollingCounterView = (RollingCounterView) findViewById(R.id.rolling_counter_view);
        final EditText customValueEditText = (EditText) findViewById(R.id.roll_sample_activity_custom_value_edit_text);
        View customValueButton = findViewById(R.id.roll_sample_activity_custom_value_button_set);
        View randomValueButton = findViewById(R.id.roll_sample_activity_random_value_button_set);
        View autoRollForwardButton = findViewById(R.id.roll_sample_activity_auto_roll_button_forward);
        View autoRollBackwardButton = findViewById(R.id.roll_sample_activity_auto_roll_button_backward);
        View autoRollStopButton = findViewById(R.id.roll_sample_activity_auto_roll_button_stop);

        final Random random = new Random();

        customValueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = customValueEditText.getText().toString();
                customValueEditText.setText(null);
                try{
                    stopAutoRoll();
                    int val = Integer.parseInt(value);
                    rollingCounterView.setCounterValue(val, false);
                } catch (Exception e) {
                    Toast.makeText(RollSampleActivity.this, "Bad input", Toast.LENGTH_SHORT).show();
                }
            }
        });

        randomValueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAutoRoll();
                rollingCounterView.setCounterValue(random.nextInt((int) Math.pow(10, rollingCounterView.getDigitCount())), false);
            }
        });

        autoRollForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAutoRoll();
                startAutoRoll(true);
            }
        });

        autoRollBackwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAutoRoll();
                startAutoRoll(false);
            }
        });

        autoRollStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAutoRoll();
            }
        });
    }

    private Runnable autoRoll = new Runnable() {
        @Override
        public void run() {
            rollingCounterView.changeCounterBy(autoRollDirection ? +1 : -1);
            startAutoRoll(autoRollDirection);
        }
    };

    private void stopAutoRoll() {
        rollingCounterView.removeCallbacks(autoRoll);
    }

    private void startAutoRoll(boolean forward) {
        autoRollDirection = forward;
        rollingCounterView.postDelayed(autoRoll, 1000);
    }

}
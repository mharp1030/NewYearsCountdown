package com.moeharp.newyearscountdown;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener
{
    private Button btnSetCount, btnStart;
    private CardView cardSet, cardStart;
    private EditText txtCountdown, txtMsg;
    private Spinner spinner;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize widgets
        btnSetCount = findViewById(R.id.btnSetCount);
        btnStart = findViewById(R.id.btnStart);
        cardSet = findViewById(R.id.cardSet);
        cardStart = findViewById(R.id.cardStart);
        txtCountdown = findViewById(R.id.txtCountdown);
        txtMsg = findViewById(R.id.txtMsg);
        spinner = findViewById(R.id.spnHighestNot);

        prefs = getSharedPreferences("timer", MODE_PRIVATE);

        // Hide card view object
        cardStart.setVisibility(View.INVISIBLE);

        // Set listeners
        btnSetCount.setOnClickListener(this);
        btnStart.setOnClickListener(this);

        // When keyboard "enter" button is clicked, it allows the button to be clicked as well
        txtCountdown.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                        || (actionId == EditorInfo.IME_ACTION_DONE))
                {
                    btnSetCount.performClick();
                }
                return false;
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if(!String.valueOf(txtCountdown.getText().toString()).isEmpty())
            btnSetCount.performClick();
    }

    private void SaveValues()
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("countdown", Integer.parseInt(txtCountdown.getText().toString()));
        editor.putInt("Interval", Integer.parseInt(spinner.getSelectedItem().toString()));
        editor.putString("message", txtMsg.getText().toString());
        editor.apply();
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.btnSetCount:
                int countdownIntervals;
                List<String> intervals = new ArrayList<>();
                ArrayAdapter<String> adapter;
                if(!String.valueOf(txtCountdown.getText().toString()).isEmpty())
                {
                    countdownIntervals = Integer.parseInt(txtCountdown.getText().toString());
                    if(countdownIntervals % 5 == 0 && countdownIntervals <= 120)
                    {
                        // Adds items to spinner
                        if(countdownIntervals >= 90)
                            intervals.add(Integer.toString(90));
                        if(countdownIntervals >= 60)
                            intervals.add(Integer.toString(60));
                        if(countdownIntervals >= 30)
                            intervals.add(Integer.toString(30));
                        if(countdownIntervals >= 20)
                            intervals.add(Integer.toString(20));
                        if(countdownIntervals >= 10)
                            intervals.add(Integer.toString(10));
                        intervals.add(Integer.toString(5));
                        intervals.add(Integer.toString(1));

                        adapter = new ArrayAdapter<>(getApplicationContext(),
                                android.R.layout.simple_spinner_dropdown_item, intervals);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(adapter);

                        // Makes second card visible to continue countdown process
                        cardStart.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        cardStart.setVisibility(View.INVISIBLE);
                        Toast.makeText(MainActivity.this,
                                "Error: Value Must be Multiple of 5 and Less Than 120 Seconds.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    cardStart.setVisibility(View.INVISIBLE);
                    Toast.makeText(MainActivity.this,
                            "Error: Countdown Textbox Can't Empty.",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnStart:
                if(!txtMsg.getText().toString().isEmpty())
                {
                    Toast.makeText(MainActivity.this,
                            "Countdown Has Been Started!",
                            Toast.LENGTH_SHORT).show();

                    SaveValues();
                    startService(new Intent(this, NotificationService.class));
                }
                else
                {
                    Toast.makeText(MainActivity.this,
                            "Error: Message Textbox Can't Empty.",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

}

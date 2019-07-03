package com.pxq.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import com.pxq.myapplication.widget.TextSeekBar;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextSeekBar seekBar = findViewById(R.id.seek_bar);
        seekBar.setText("test");
    }



}

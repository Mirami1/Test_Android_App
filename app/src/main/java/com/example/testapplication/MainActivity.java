package com.example.testapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener {
    Button button1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button1= findViewById(R.id.button_1);
        button1.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        Toast.makeText(MainActivity.this,"WE ARE FUCKED",Toast.LENGTH_SHORT).show();
        button1.setText("YELTSIN VODKA");
    }
}
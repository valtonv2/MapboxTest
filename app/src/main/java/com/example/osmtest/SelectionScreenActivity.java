package com.example.osmtest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SelectionScreenActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection_screen);
    }


    public void goBack(View view) {

        Intent goBackToMap = new Intent(this, MainActivity.class);
        startActivity(goBackToMap);


    }

}

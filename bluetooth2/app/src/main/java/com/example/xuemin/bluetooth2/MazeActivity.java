package com.example.xuemin.bluetooth2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;

public class MazeActivity extends AppCompatActivity {
    android.support.v7.widget.Toolbar toolbar;
    ImageButton upButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maze);
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        //over write the tool bar title
        // toolbar.setTitle("Robot 23");
        setSupportActionBar(toolbar);

        upButton = (ImageButton) findViewById(R.id.upBtn);
    }

    //for importing the menu into the main page
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String msg =" ";
        switch(item.getItemId()){
            case R.id.delete:
                msg="deleted";
                break;
            case R.id.map:
                break;
        }
        return super.onOptionsItemSelected(item);
    }



}

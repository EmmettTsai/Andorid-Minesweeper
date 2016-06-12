package com.emmett.minesweeper;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;

public class MainActivity extends AppCompatActivity {

    private final String KEY_WINESWEEPER = "KEY_WINESWEEPER";
    private GridView grid_view;
    private Minesweeper minesweeper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("emmett", "onCreate");
        setContentView(R.layout.activity_main);
        grid_view = (GridView) findViewById(R.id.grid_view);
        if (savedInstanceState != null) {
            minesweeper = savedInstanceState.getParcelable(KEY_WINESWEEPER);
            if (minesweeper != null) {
                minesweeper.init(this, grid_view);
            }
        } else {
            createNewGame(Minesweeper.Level.EASY);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_easy:
                createNewGame(Minesweeper.Level.EASY);
                return true;
            case R.id.action_normal:
                createNewGame(Minesweeper.Level.NORMAL);
                return true;
            case R.id.action_hard:
                createNewGame(Minesweeper.Level.HARD);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("emmett", "onSaveInstanceState");
        outState.putParcelable(KEY_WINESWEEPER, minesweeper);
    }

    private void createNewGame(Minesweeper.Level level) {
        minesweeper = new Minesweeper(this, grid_view, level);
    }
}

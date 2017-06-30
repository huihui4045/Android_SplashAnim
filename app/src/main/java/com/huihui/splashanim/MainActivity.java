package com.huihui.splashanim;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {

    private FrameLayout mMainView;
    private SplashView splashView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainView = new FrameLayout(this);
        ContentView contentView = new ContentView(this);
        mMainView.addView(contentView);
        splashView = new SplashView(this);
        mMainView.addView(splashView);

        setContentView(mMainView);
    }
}

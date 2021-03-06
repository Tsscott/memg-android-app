package com.xyz.me_mg;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by timscott on 15/04/2018.
 *
 * Splash Screen with Me-MG Logo
 *
 * 5 seconds
 *
 * Uses example code:
 * https://www.coderefer.com/android-splash-screen-example-tutorial/
 *
 */

public class SplashActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        Thread timerThread = new Thread(){
            public void run(){
                try{
                    sleep(5000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }finally{
                    Intent intent = new Intent(SplashActivity.this,MainActivity.class);
                    startActivity(intent);
                }
            }
        };
        timerThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }



}

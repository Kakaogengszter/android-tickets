package com.example.androidshop;

import android.os.AsyncTask;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Random;

public class RandomAsyncTask extends AsyncTask<Void,Void,String> {

    private WeakReference<TextView> mTextView;

    public RandomAsyncTask(TextView tw){
        mTextView = new WeakReference<>(tw);
    }

    @Override
    protected String doInBackground(Void... voids) {
        Random r = new Random();
        int number = r.nextInt(11);
        int ms = number * 300;

        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        return "Bejelentkezés vendégként " + ms + " ms után";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        mTextView.get().setText(s);
    }
}

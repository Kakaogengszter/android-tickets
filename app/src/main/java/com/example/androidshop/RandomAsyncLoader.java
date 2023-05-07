package com.example.androidshop;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import java.util.Random;

public class RandomAsyncLoader extends AsyncTaskLoader<String>{

    public RandomAsyncLoader(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();

        forceLoad();
    }

    @Nullable
    @Override
    public String loadInBackground() {

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
}

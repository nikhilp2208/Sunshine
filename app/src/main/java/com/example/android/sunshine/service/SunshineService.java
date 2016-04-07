package com.example.android.sunshine.service;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by nikhil.p on 07/04/16.
 */
public class SunshineService extends IntentService {

    public SunshineService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}

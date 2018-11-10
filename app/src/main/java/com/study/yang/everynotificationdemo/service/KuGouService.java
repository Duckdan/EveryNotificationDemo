package com.study.yang.everynotificationdemo.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class KuGouService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public class MyBinder extends Binder implements GuGouBinder {

        @Override
        public void player() {

        }

        @Override
        public void pause() {

        }

        @Override
        public void previous() {

        }

        @Override
        public void next() {

        }
    }

}

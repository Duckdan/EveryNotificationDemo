package com.study.yang.everynotificationdemo;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.study.yang.everynotificationdemo.broadcast.HeadsetReceiver;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private KuGouBroadCastReceiver kuGouBroadCastReceiver;
    private NotificationManager notificationManager;
    private UCBroadCastReceiver ucBroadCastReceiver;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (currentProgress > 100) {
                handler.removeMessages(1);
                return;
            }

            if (ucCurrentProgress > 100) {
                handler.removeMessages(2);
                isDownload = !isDownload;
                return;
            } else if (ucCurrentProgress == 100) {
                isDownload = !isDownload;
            }

            switch (msg.what) {
                case 1:
                    currentProgress++;
                    notifyDefineProgress();
                    break;
                case 2:
                    ucCurrentProgress++;
                    ucCurrentSecondProgress++;
                    ucDownloadProgress();
                    break;
            }

        }
    };
    private AudioManager mAudioManager;
    private ComponentName mComponent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //获得AudioManager对象
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        //构造一个ComponentName，指向MediaoButtonReceiver类
        mComponent = new ComponentName(getPackageName(), HeadsetReceiver.class.getName());

        //注册一个MediaButtonReceiver广播监听
        mAudioManager.registerMediaButtonEventReceiver(mComponent);


        findViewById(R.id.tv_first).setOnClickListener(this);
        findViewById(R.id.tv_second).setOnClickListener(this);
        findViewById(R.id.tv_third).setOnClickListener(this);
        findViewById(R.id.tv_forth).setOnClickListener(this);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "chat";
            String channelName = "聊天消息";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            createNotificationChannel(channelId, channelName, importance);

            channelId = "subscribe";
            channelName = "订阅消息";
            importance = NotificationManager.IMPORTANCE_DEFAULT;
            createNotificationChannel(channelId, channelName, importance);

            channelId = "music";
            channelName = "酷狗音乐";
            importance = NotificationManager.IMPORTANCE_DEFAULT;
            createNotificationChannel(channelId, channelName, importance);

            channelId = "progress";
            channelName = "notify自带进度条";
            importance = NotificationManager.IMPORTANCE_DEFAULT;
            createNotificationChannel(channelId, channelName, importance);

            channelId = "uc_progress";
            channelName = "UC下载进度条";
            importance = NotificationManager.IMPORTANCE_DEFAULT;
            createNotificationChannel(channelId, channelName, importance);
        }

        IntentFilter kuGouFilter = new IntentFilter();
        kuGouFilter.addAction("com.kugou.action.pre");
        kuGouFilter.addAction("com.kugou.action.next");
        kuGouFilter.addAction("com.kugou.action.close");
        kuGouBroadCastReceiver = new KuGouBroadCastReceiver();
        registerReceiver(kuGouBroadCastReceiver, kuGouFilter);

        IntentFilter ucFilter = new IntentFilter();
        ucFilter.addAction("com.uc.action.pause.play");
        ucBroadCastReceiver = new UCBroadCastReceiver();
        registerReceiver(ucBroadCastReceiver, ucFilter);
    }


    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        /**
         * Android8.0启动震动，记得添加震动权限
         */
        channel.enableVibration(true);
        /**
         * 先等2秒，再震动2秒，然后就不震动了，所以这个long数组的元素要是偶数个因为奇数个最后一个没用
         */
        channel.setVibrationPattern(new long[]{2000, 2000, 2000});
        /**
         * 设置通知到来时手机关屏时指示灯的提示颜色
         */
        channel.enableLights(true);
        channel.setLightColor(Color.YELLOW);
        NotificationManager notificationManager = (NotificationManager) getSystemService(
                NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_first:
                firstNotification();
                break;
            case R.id.tv_second:
                kuGouNotification();
                break;
            case R.id.tv_third:
                notifyDefineProgress();
                break;
            case R.id.tv_forth:
                ucDownloadProgress();
                break;
        }
    }


    /**
     * 第一个样式通知栏：
     */
    private void firstNotification() {
        //创建Builder的时候传入“chat”
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "chat");
        Intent intent = new Intent(this, AActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 100, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notify = builder.
                //Android8.0上面LargeIcon没有效果
                        setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_directions_railway)).
                        setSmallIcon(R.drawable.ic_local_car).
                        setTicker("通知来了Ticker").  //话说这个有上升动画
                setContentInfo("ContentInfo--通知文本").//这个在通知中显示不出来
                setContentText("ContentText--通知文本").
                        setContentTitle("ContentTitle--通知文本").
                        setContentIntent(pendingIntent).
                        setColor(Color.YELLOW).
                //setVibrate(new long[]{2000, 2000, 2000, 2000}).//Android8.0不起作用
                //setLights(Color.parseColor("#0000FF"), 5, 5).//Android8.0不起作用
                        build();
        notify.flags = Notification.FLAG_NO_CLEAR;//此标记为该同志将不会清楚
        notificationManager.notify(1, notify);

    }

    private String[] musicPictures = {"https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=1269331898,2561345491&fm=26&gp=0.jpg",
            "https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=3513011019,3508358566&fm=26&gp=0.jpg",
            "https://ss0.bdstatic.com/70cFuHSh_Q1YnxGkpoWK1HF6hhy/it/u=1376494895,619814693&fm=15&gp=0.jpg",
            "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=1981155124,791539881&fm=26&gp=0.jpg",
            "https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=2307580165,777665758&fm=26&gp=0.jpg"};
    private int currentIndex = 0;
    private Notification kuGouNotify = null;
    private RemoteViews kuGouContentView = null;

    /**
     * 酷狗通知栏---自定义通知栏
     */
    private void kuGouNotification() {
        if (kuGouNotify == null) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "music");
            kuGouContentView = new RemoteViews(getPackageName(), R.layout.notify_kugou_layout);

            Intent preIntent = new Intent();
            preIntent.setAction("com.kugou.action.pre");
            PendingIntent prePendingIntent = PendingIntent.getBroadcast(this, 100, preIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            kuGouContentView.setOnClickPendingIntent(R.id.iv_previous, prePendingIntent);

            Intent nextIntent = new Intent();
            nextIntent.setAction("com.kugou.action.next");
            PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, 200, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            kuGouContentView.setOnClickPendingIntent(R.id.iv_next, nextPendingIntent);

            Intent closeIntent = new Intent();
            closeIntent.setAction("com.kugou.action.close");
            PendingIntent closePendingIntent = PendingIntent.getBroadcast(this, 300, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            kuGouContentView.setOnClickPendingIntent(R.id.iv_close, closePendingIntent);

            Intent intent = new Intent(this, BActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 100, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            kuGouNotify = builder.
                    setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_music)).
                    setSmallIcon(R.drawable.ic_music).
                    setOngoing(true).//设置当前通知栏是否不间断的运行,添加这个之后生成的自定义布局的通知才生效
                    setCustomBigContentView(kuGouContentView). //设置折叠的通知栏
                    setContentIntent(pendingIntent).
                    build();
        }

        Glide.with(this).asBitmap().load(Uri.parse(musicPictures[currentIndex])).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                kuGouContentView.setImageViewBitmap(R.id.iv_music, resource);
                notificationManager.notify(2, kuGouNotify);  //放到此处进行发出通知，使得图片加载成功之后弹出通知
            }
        });
//        notify.bigContentView = contentView;   //该方法在API28已经过时

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(kuGouBroadCastReceiver);
        unregisterReceiver(ucBroadCastReceiver);
        mAudioManager.unregisterMediaButtonEventReceiver(mComponent);
    }

    private class KuGouBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();
            Log.e("intentAction", intentAction);

            if ("com.kugou.action.pre".equals(intentAction)) {
                currentIndex = currentIndex == 0 ? 4 : currentIndex - 1;
            } else if ("com.kugou.action.next".equals(intentAction)) {
                currentIndex = currentIndex == 4 ? 0 : currentIndex + 1;
            } else if ("com.kugou.action.close".equals(intentAction)) {
                notificationManager.cancel(2);
                return;
            }

            kuGouNotification();
        }
    }


    private int currentProgress = 1;
    private NotificationCompat.Builder notifyBuilder = null;  //Builder
    private Notification notifyDefine = null;

    /**
     * 通知栏自带进度条
     */
    private void notifyDefineProgress() {
        if (notifyBuilder == null) {
            notifyBuilder = new NotificationCompat.Builder(this, "progress");
            Intent intent = new Intent(this, CActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 100, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            notifyDefine = notifyBuilder.
                    setSmallIcon(R.drawable.ic_file_download).
                    setTicker("通知来了Ticker").  //话说这个有上升动画
                    setContentText("ContentText--通知文本").
                    setContentTitle("ContentTitle--通知文本").
                    setContentIntent(pendingIntent).
                    //第三个参数为true时代表是间断的进度条，为false的时候就是正常进度条
                            setProgress(100, currentProgress, false).
                            build();
            notifyDefine.flags = Notification.FLAG_NO_CLEAR;
        }
        notifyDefine = notifyBuilder.setProgress(100, currentProgress, false).build();
        notificationManager.notify(3, notifyDefine);
        handler.sendEmptyMessageDelayed(1, 1000);
    }

    private boolean isDownload = true;
    private Notification ucNotification = null;
    private RemoteViews ucContentView = null;
    private int ucCurrentProgress = 1;
    private int ucCurrentSecondProgress = 5;

    private void ucDownloadProgress() {
        if (ucNotification == null) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "music");
            ucContentView = new RemoteViews(getPackageName(), R.layout.notify_uc_layout);
            ucContentView.setImageViewResource(R.id.iv_pause_play, isDownload ? R.drawable.ic_pause_circle : R.drawable.ic_play_circle);
            //给ProgressBar设置进度
            ucContentView.setProgressBar(R.id.pb, 100, ucCurrentProgress, false);
            //更新ProgressBar第二层进度条
            ucContentView.setInt(R.id.pb, "setSecondaryProgress", ucCurrentSecondProgress);


            Intent preIntent = new Intent();
            preIntent.setAction("com.uc.action.pause.play");
            PendingIntent prePendingIntent = PendingIntent.getBroadcast(this, 100, preIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            ucContentView.setOnClickPendingIntent(R.id.iv_pause_play, prePendingIntent);


            Intent intent = new Intent(this, BActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 100, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            ucNotification = builder.
                    setSmallIcon(R.drawable.ic_toys_black).
                    setOngoing(true).//设置当前通知栏是否不间断的运行,添加这个之后生成的自定义布局的通知才生效
                    setCustomBigContentView(ucContentView). //设置折叠的通知栏
                    setContentIntent(pendingIntent).
                    build();
        }


        ucContentView.setImageViewResource(R.id.iv_pause_play, isDownload ? R.drawable.ic_pause_circle : R.drawable.ic_play_circle);
        ucContentView.setProgressBar(R.id.pb, 100, ucCurrentProgress, false);
        ucContentView.setInt(R.id.pb, "setSecondaryProgress", ucCurrentSecondProgress);
        if (isDownload) {
            ucContentView.setImageViewResource(R.id.iv_icon, R.drawable.uc_rotate);
            handler.sendEmptyMessageDelayed(2, 1000);
        } else { //false时给iv_icon设置不旋转，必须放在notify方法之前否则该图片设置将会不生效
            ucContentView.setImageViewResource(R.id.iv_icon, R.drawable.ic_toys_black);
        }
        notificationManager.notify(4, ucNotification);  //放到此处进行发出通知，使得图片加载成功之后弹出通知
    }

    private class UCBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();

            if ("com.uc.action.pause.play".equals(intentAction)) {
                isDownload = !isDownload;
            }

            if (ucCurrentProgress > 100) {
                ucCurrentProgress = 1;
                ucCurrentSecondProgress = 5;
            }

            if (!isDownload) {
                //false时给iv_icon设置不旋转
                ucContentView.setImageViewResource(R.id.iv_icon, R.drawable.ic_toys_black);
                handler.removeMessages(2);
            }

            ucDownloadProgress();
        }
    }


}

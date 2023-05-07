package com.example.androidshop;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class NotificationHandler {
    private static final String CHANNEL_ID = "shop_notification_channel";
    private final int NOTIFICATION_ID = 0;
    private NotificationManager mNotManager;
    private Context mContext;

    public NotificationHandler(Context context){
        this.mContext = context;
        this.mNotManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        createChannel();
    }

    private void createChannel(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            return;
        }

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,"Shop notification",NotificationManager.IMPORTANCE_DEFAULT);

        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLightColor(Color.RED);
        channel.setDescription("Notification from shop app");
        this.mNotManager.createNotificationChannel(channel);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void send(String message){
        Intent intent = new Intent(mContext,ShopListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext,0,intent,PendingIntent.FLAG_MUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext,CHANNEL_ID)
                .setContentTitle("Shop app")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_shopping_cart)
                .setContentIntent(pendingIntent);

        this.mNotManager.notify(NOTIFICATION_ID,builder.build());
    }

    public void cancel(){
        this.mNotManager.cancel(NOTIFICATION_ID);
    }

}

package com.example.testapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String CHANNEL_ID = "123";
    NotificationManager notificationManager;
    NotificationChannel notificationChannel;
    NotificationCompat.Builder builder;
    long itemId = 12345678910L;
    int notificationId = ((Long)itemId).intValue();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //проверить, есть ли такой датчик, иначе закрываем приложение
        SensorManager manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE); //служба по сенсорам
        if (manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) { // если датчика нет, ошибка, закрытие приложения
            //создание окна
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error"); //заголовок
            builder.setMessage("Акселерометр не обнаружен. Приложение будет закрыто."); //сообщение
            builder.setCancelable(false); //нельзя закрыть клавишей Back
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { //сразу обработчик
                public void onClick(DialogInterface dialog, int item) { //обязаны реализовать
                    MainActivity.this.finish();
                }
            });
            //отображение
            builder.show();
        }
        //проверить, запущена ли служба
        //в зависимости от результата сделать доступными/недоступными кнопки
        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(100);
        boolean flag = false; //служба не запущена

        for (ActivityManager.RunningServiceInfo s : rs) {
            if(("" + s.service).equals("ComponentInfo{samples.com.accservicelauncher/samples.com.accservicelauncher.AccService}"))
            {
                flag = true;
                break;
            }

        }

        if (flag) {
            ((Button) findViewById(R.id.btnStart)).setEnabled(false);
            ((Button) findViewById(R.id.btnStop)).setEnabled(true);
        } else { //не запущена
            ((Button) findViewById(R.id.btnStart)).setEnabled(true);
            ((Button) findViewById(R.id.btnStop)).setEnabled(false);
        }

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationChannel = new NotificationChannel(CHANNEL_ID,"My channel",NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setDescription("Service notifications");
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(false);
        notificationManager.createNotificationChannel(notificationChannel);

        Intent resultIntent = new Intent(this,MainActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //resultIntent.setAction(Intent.ACTION_MAIN);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this,0,resultIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

         builder= new NotificationCompat.Builder(this,CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Сервис")
                        .setContentText("Сервис запущен")
                        .setContentIntent(resultPendingIntent)
                        .setAutoCancel(true);

    }




    public void graph(View view){
        //выбор файла для графика
        Intent intent = new Intent(MainActivity.this, FileChooser.class);
        startActivityForResult(intent, 1);
    }


    @Override
    public void onClick(View v) {
        Toast.makeText(MainActivity.this, "WE ARE FUCKED", Toast.LENGTH_SHORT).show();

    }

    public void servStart(View view) {
        ((Button) findViewById(R.id.btnStart)).setEnabled(false); //защита от повторного нажатия start во время записи
        ((Button) findViewById(R.id.btnStop)).setEnabled(true);
        startService(new Intent(MainActivity.this, AccSevice.class)); //текущий класс, вызываемый класс
        notificationManager.notify(123, builder.build());

    }
    public void servStop(View view) {
        ((Button) findViewById(R.id.btnStart)).setEnabled(true);
        ((Button) findViewById(R.id.btnStop)).setEnabled(false);
        stopService(new Intent(MainActivity.this, AccSevice.class));
    }
}
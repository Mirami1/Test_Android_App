package com.example.testapplication;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    public final static int REQUEST_CODE_PERMISSION_READ_CONTACTS = 1;
    private static final String CHANNEL_ID = "123";
    NotificationManager notificationManager;
    NotificationChannel notificationChannel;
    NotificationCompat.Builder builder;
    long itemId = 12345678910L;
    int notificationId = ((Long) itemId).intValue();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_PERMISSION_READ_CONTACTS);
        }
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
            if (("" + s.service).equals("ComponentInfo{samples.com.accservicelauncher/samples.com.accservicelauncher.AccService}")) {
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
        notificationChannel = new NotificationChannel(CHANNEL_ID, "My channel", NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setDescription("Service notifications");
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(false);
        notificationManager.createNotificationChannel(notificationChannel);

        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //resultIntent.setAction(Intent.ACTION_MAIN);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Сервис")
                .setContentText("Сервис запущен")
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true);

    }


    public void graph(View view) {
        //выбор файла для графика
        Intent intent = new Intent(MainActivity.this, FileChooser.class);
        startActivityForResult(intent, 1);
    }


    public void servStart(View view) {
        Snackbar.make(view, "Service created", Snackbar.LENGTH_LONG).show();
        ((Button) findViewById(R.id.btnStart)).setEnabled(false); //защита от повторного нажатия start во время записи
        ((Button) findViewById(R.id.btnStop)).setEnabled(true);
        startService(new Intent(MainActivity.this, AccSevice.class)); //текущий класс, вызываемый класс
        notificationManager.notify(123, builder.build());

    }

    public void servStop(View view) {
        Snackbar.make(view, "Service stopped", Snackbar.LENGTH_LONG).show();
        ((Button) findViewById(R.id.btnStart)).setEnabled(true);
        ((Button) findViewById(R.id.btnStop)).setEnabled(false);
        stopService(new Intent(MainActivity.this, AccSevice.class));
        notificationManager.cancel(123);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { //для результата из FileChooser
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) // если файл не выбран
            return;

        String name = data.getStringExtra("name");
        Toast.makeText(this, name, Toast.LENGTH_SHORT).show();

        //график
        Intent intent2 = new Intent(MainActivity.this, GraphActivity.class);
        intent2.putExtra("name", name);
        startActivity(intent2);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_READ_CONTACTS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Error"); //заголовок
                    builder.setMessage("Вы не дали разрешение на запись файлов данных. Приложение будет закрыто."); //сообщение
                    builder.setCancelable(false); //нельзя закрыть клавишей Back
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { //сразу обработчик
                        public void onClick(DialogInterface dialog, int item) { //обязаны реализовать
                            MainActivity.this.finish();
                        }
                    });
                    //отображение
                    builder.show();
                }
                return;
        }

    }
}
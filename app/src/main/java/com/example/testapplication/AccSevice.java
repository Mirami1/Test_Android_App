package com.example.testapplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AccSevice extends Service {
    SensorManager manager;
    Sensor OriSensor;

    public class measure { //одна показание, которое будет записано в файл
        public float x, y, z, cnt;

        measure() {
            x = 0;
            y = 0;
            z = 0;
            cnt = 0;
        }

        public void clear() {
            m.x = 0;
            m.y = 0;
            m.z = 0;
            m.cnt = 0;

        }
    }

    measure m;
    boolean flag = true; //флаг работы задачи task

    private SensorEventListener listener = new SensorEventListener() { // обьект слушатель
        @Override
        public void onSensorChanged(SensorEvent event) {//усредняем значения, полученные между двумя снятиями показаний - поможет избежать сильных разбросов
            synchronized (m) {
                m.x = event.values[0];
                m.y = event.values[1];
                m.z = event.values[2];
                m.cnt++;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    public AccSevice() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, "Service created", Toast.LENGTH_SHORT).show();
        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        OriSensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        manager.registerListener(listener, OriSensor, SensorManager.SENSOR_DELAY_FASTEST); //подписка на показания
        m = new measure();

        //подготовка к записи файла - проверка наличия директории
        File dir = new File(Environment.getExternalStorageDirectory(), "AccServiceData");
        if (!dir.exists())
            dir.mkdir();
        task();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        manager.unregisterListener(listener); //отписаться от получения показаний
        flag = false; //останавливаем task

    }

    void task() { //снимает показания через равные промежутки времени в отдельном потоке и записывает их в файл
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String name = new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss").format(new Date());

                    File file = new File(Environment.getExternalStorageDirectory() + "/AccServiceData/", name);
                    FileWriter fwr = new FileWriter(file);

                    if (m.cnt == 0)
                        try {
                            //успеть получить измерения => cnt!=0
                            TimeUnit.MILLISECONDS.sleep(500); //тысячные
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    while (flag) {
                        synchronized (m) {
                            fwr.write("" + (m.x) / (m.cnt) + " " + (m.y) / (m.cnt) + " " + (m.z) / (m.cnt) + "\n");
                            m.clear();
                        }
                        try {
                            TimeUnit.MILLISECONDS.sleep(250); //тысячные
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                    fwr.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }


}

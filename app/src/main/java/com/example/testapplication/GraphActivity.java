package com.example.testapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.widget.LinearLayout;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.Scanner;

public class GraphActivity extends Activity {
    private String fileName;
    private GraphicalView Chart; //view
    private XYMultipleSeriesDataset Dataset = new XYMultipleSeriesDataset(); //общий набор
    private XYMultipleSeriesRenderer Renderer = new XYMultipleSeriesRenderer(); //общий рендер

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph2);

        Intent intent = getIntent(); //intent вызывающей activity
        fileName = intent.getStringExtra("name");

        //частные наборы
        XYSeries X = new XYSeries("\nУскорение по оси X");
        XYSeries Y = new XYSeries("\nУскорение по оси Y");
        XYSeries Z = new XYSeries("\nУскорение по оси Z");

        readData(X,Y,Z);//заполняем
        Chart = makeGView(X,Y,Z,Dataset,Renderer);

        Renderer.setChartTitle("График ускорений");
        Renderer.setXTitle("t, с");
        Renderer.setYTitle("a, м/с^2");

        LinearLayout layout = (LinearLayout) findViewById(R.id.gLayout);
        layout.addView(Chart);
    }

    GraphicalView makeGView(XYSeries X, XYSeries Y, XYSeries Z,XYMultipleSeriesDataset Dataset,XYMultipleSeriesRenderer Renderer){
        //частные рендеры
        XYSeriesRenderer xRender = new XYSeriesRenderer();
        XYSeriesRenderer yRender = new XYSeriesRenderer();
        XYSeriesRenderer zRender = new XYSeriesRenderer();
        xRender.setColor(Color.RED);
        yRender.setColor(Color.rgb(0,200,0));
        zRender.setColor(Color.BLUE);
        xRender.setLineWidth(2);
        yRender.setLineWidth(2);
        zRender.setLineWidth(2);

        //общие наборы и рендеры
        Dataset.addSeries(0,X);
        Dataset.addSeries(1,Y);
        Dataset.addSeries(2,Z);
        Renderer.addSeriesRenderer(0,xRender);
        Renderer.addSeriesRenderer(1,yRender);
        Renderer.addSeriesRenderer(2,zRender);

        Renderer.setLegendTextSize(20.0f);
        Renderer.setLabelsTextSize(20.0f);
        Renderer.setChartTitleTextSize(30.0f);
        Renderer.setAxisTitleTextSize(20.0f);

        Renderer.setLabelsColor(Color.BLACK);
        Renderer.setXLabelsColor(Color.BLACK);
        Renderer.setMarginsColor(Color.rgb(200,200,200));
        //grid
        Renderer.setShowGridX(true);
        Renderer.setShowGridY(true);
        Renderer.setGridColor(Color.rgb(90,90,90));

        return ChartFactory.getCubeLineChartView(this,Dataset,Renderer,0.1f);
    }

    void readData(XYSeries X, XYSeries Y, XYSeries Z){
        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/AccServiceData/", fileName);

            Scanner s = new Scanner(file);
            s.useLocale(Locale.CHINA);

            double x, y, z; //показания
            double time = 0;
            while (s.hasNextDouble()) {
                X.add(time,x = s.nextDouble());
                Y.add(time,y = s.nextDouble());
                Z.add(time,z = s.nextDouble());
                time+=0.25;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

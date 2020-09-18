package com.example.testapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.Arrays;

public class FileChooser extends AppCompatActivity {
private ListView fileList;
private String[] fileNames;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_chooser);
        File dir = new File(Environment.getExternalStorageDirectory(),"AccServiceData");
        if(!dir.exists())
            dir.mkdir();
        File[] files=dir.listFiles();
        fileNames=new String[files.length];
        for(int i=0;i<files.length;i++)
            fileNames[i]=files[i].getName();
        Arrays.sort(fileNames);

        fileList=(ListView)findViewById(R.id.listView);
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,fileNames);
        fileList.setAdapter(adapter);

        fileList.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra("name",fileNames[position]);
                setResult(RESULT_OK,intent);
                finish();
            }
        });

    }
}
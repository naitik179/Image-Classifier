package com.example.catdogclassifier;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.catdogclassifier.classifier.ImageClassifier;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {


    // request the activity to identify camera and pormission request
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1000;
    private static final int CAMERA_REQUEST_CODE = 10001;


    public ImageView imageView;
    public ListView listview;
    public ImageClassifier imageclassifier;
    public Button takepicture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializelements();
    }

    public void initializelements(){

        imageView = findViewById(R.id.imageView);
        listview = findViewById(R.id.listview);
        takepicture = findViewById(R.id.takepicture);


        // creating instance of tensor image classifier

        try {
            imageclassifier =new ImageClassifier(this);
        } catch (IOException e) {
            Log.e("Image Classifer Error" , "error" , e);
            e.printStackTrace();
        }

        takepicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if camera permission is available or request for the permission
                if(hasPermission()){
                    opencamera();
                }
                else{
                    requestPermission();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == CAMERA_REQUEST_CODE) {
            // getting bitmap of the image
            Bitmap photo = (Bitmap) Objects.requireNonNull(Objects.requireNonNull(data).getExtras()).get("data");
            // displaying this bitmap in imageview
            imageView.setImageBitmap(photo);

            // pass this bitmap to classifier to make prediction
            List<ImageClassifier.Recognition> predicitons = imageclassifier.recognizeImage(
                    photo, 0);

            // creating a list of string to display in list view
            final List<String> predicitonsList = new ArrayList<>();
            for (ImageClassifier.Recognition recog : predicitons) {
                predicitonsList.add(recog.getName() + "  --------->  " + recog.getConfidence());
            }

            // creating an array adapter to display the classification result in list view
            ArrayAdapter<String> predictionsAdapter = new ArrayAdapter<>(
                    this, R.layout.support_simple_spinner_dropdown_item, predicitonsList);
            listview.setAdapter(predictionsAdapter);

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // this is the result of the camera permission result
        if(requestCode == CAMERA_PERMISSION_REQUEST_CODE){
            if(hasAllPermissions(grantResults)){
                opencamera();
            }
            else{
                requestPermission();
            }
        }
    }

    private boolean hasAllPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED)
                return false;
        }
        return true;
    }

     // Method requests for permission if the android version is marshmallow or above

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // whether permission can be requested or on not
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                Toast.makeText(this, "Camera Permission Required", Toast.LENGTH_SHORT).show();
            }
            // request the camera permission permission
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * creates and starts an intent to get a picture from camera
     */
    private void opencamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }


    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }
}

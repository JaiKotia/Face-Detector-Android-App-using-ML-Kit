package com.mrkotia.facedetector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    static int REQUEST_IMAGE_CAPTURE = 1;

    ImageView imageView;

    TextView textView;

    static Bitmap imageBitmap;
    static Bitmap mutableBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        Button button = findViewById(R.id.button);
        imageView = findViewById(R.id.image_view);
        textView = findViewById(R.id.textView);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });



    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");

            REQUEST_IMAGE_CAPTURE=1;

            Log.w("Hello", "We reached start");

            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    Log.w("Hello", "We reached thread");

                    FirebaseVisionFaceDetectorOptions options =
                            new FirebaseVisionFaceDetectorOptions.Builder()
                                    .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
                                    .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                                    .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                                    .setMinFaceSize(0.15f)
                                    .setTrackingEnabled(true)
                                    .build();


                    FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
                    FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                            .getVisionFaceDetector(options);

                    mutableBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);


                    Task<List<FirebaseVisionFace>> result =
                            detector.detectInImage(image)
                                    .addOnSuccessListener(
                                            new OnSuccessListener<List<FirebaseVisionFace>>() {
                                                @Override
                                                public void onSuccess(List<FirebaseVisionFace> faces) {
                                                    // Task completed successfully
                                                    // ...


                                                    Log.w("Hello", "We reached success");

                                                    textView.setText("No :( ");

                                                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.cena));

                                                    for (FirebaseVisionFace face : faces) {

                                                        Rect bounds = face.getBoundingBox();

                                                        Log.w("Hello", "We reached face");


                                                        Log.w("Hello", ""+bounds);

                                                        Canvas canvas=new Canvas(mutableBitmap);

                                                        Paint paint=new Paint();
                                                        paint.setColor(Color.GREEN);
                                                        paint.setStyle(Paint.Style.STROKE);
                                                        canvas.drawRect(bounds, paint);

                                                        imageView.setImageDrawable(null);
                                                        textView.setText(null);
                                                        imageView.setImageBitmap(mutableBitmap);


                                                        float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
                                                        float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees

                                                        // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                                                        // nose available):
                                                        FirebaseVisionFaceLandmark leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR);
                                                        if (leftEar != null) {
                                                            FirebaseVisionPoint leftEarPos = leftEar.getPosition();
                                                        }





                                                        // If classification was enabled:
                                                        if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {

                                                            float smileProb = face.getSmilingProbability();
                                                            Log.w("Hello", "We are smiling" + smileProb);
                                                            String string = "" + smileProb;
                                                            textView.setText(string);
                                                        }
                                                        if (face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                                            float rightEyeOpenProb = face.getRightEyeOpenProbability();
                                                        }

                                                        // If face tracking was enabled:
                                                        if (face.getTrackingId() != FirebaseVisionFace.INVALID_ID) {
                                                            int id = face.getTrackingId();
                                                        }
                                                    }
                                                }
                                            })
                                    .addOnFailureListener(
                                            new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // Task failed with an exception
                                                    // ...

                                                }
                                            });

                }
            });


        }
    }


}

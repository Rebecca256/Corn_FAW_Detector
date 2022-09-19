package com.example.cornfawdetector;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.List;


public class MainActivity extends AppCompatActivity{

    //UI Views
    private ImageView imageView;
    private Button pickImage;
    private Button takePhoto;
    private Button predict;
    private TextView result;
    private TextView confidences;
    //private float threshold = 0.5F;
    private Classifier classifier;
    private int inputSize = 224;
    private String modelPath = "FAW_Detector.tflite";
    private String labelPath = "label.txt";


    Bitmap image;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init UI Views (better to use ViewBinding instead of findViewById)
        imageView = findViewById(R.id.imageView);
        takePhoto = findViewById(R.id.takePhoto);
        pickImage = findViewById(R.id.pickImage);
        result = findViewById(R.id.result);
        predict = findViewById(R.id.predict);

        //handle click, launch intent to take photo
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //intent to take a picture
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraActivityResultLauncher.launch(intent);
            }
        });


        //handle click, launch intent to pick image from gallery
        pickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //intent to pick image from gallery
                Intent intent = new Intent(Intent.ACTION_PICK);
                //set type
                intent.setType("image/*");
                galleryActivityResultLauncher.launch(intent);


            }
        });
        
        try {
            initClassifier();
            /*initViews();*/
        } catch (IOException e) {
            e.printStackTrace();
        }
  

        int imageSize = 224;

        predict.setOnClickListener(new View.OnClickListener() {

                                       @Override
                                       public void onClick(View view) {
                                           Bitmap image = ((BitmapDrawable) ((ImageView) view).getDrawable()).getBitmap();
                                           List<Classifier.Recognition> result = classifier.recognizeImage(image);
                                           Toast.makeText(MainActivity.this, result.get(0).toString(), Toast.LENGTH_SHORT).show();
                                       }
                                   });

        //handle click, to classify images
        /*predict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, true);
                try {

                    FawDetector model = FawDetector.newInstance(getApplicationContext());

                    //Creates inputs for reference.
                    TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, imageSize, imageSize, 3}, DataType.FLOAT32);

                    TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
                    //tensorImage.load(image);
                    //ByteBuffer byteBuffer = tensorImage.getBuffer();
                    //initialize bytebuffer
                    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
                    byteBuffer.order(ByteOrder.nativeOrder());

                    // get 1D array of 224 * 224 pixels in image
                    int [] intValues = new int[imageSize * imageSize];
                    image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

                    // iterate over pixels and extract R, G, and B values. Add to bytebuffer.
                    int pixel = 0;
                    for(int i = 0; i < imageSize; i++){
                        for(int j = 0; j < imageSize; j++){
                            int val = intValues[pixel++]; // RGB
                            byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                            byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                            byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));

                        }
                    }


                    inputFeature0.loadBuffer(byteBuffer);
                //Runs model inference and gets result
                    FawDetector.Outputs outputs = model.process(inputFeature0);
                    TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                    String classPredicted = "";

                    /*if (outputFeature0.getFloatArray()[0] >= threshold) {

                        classPredicted = "Healthy";
                    } else {
                        classPredicted = "Fall Armyworm";
                    }



                    result.setText(classPredicted);*/

                //Releases model resources if no longer used.
               /* model.close();
                } catch (IOException e) {
                // TODO Handle the exception
                 }

            }
        });*/

}

    /*private void initViews() {
        findViewById(R.id.imageView).setOnClickListener(this);
    }*/

    private void initClassifier() throws IOException {
        classifier = new Classifier(getAssets(), modelPath, labelPath, inputSize);
    }


    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //here we will handle the result of our intent
                    if (result.getResultCode() == Activity.RESULT_OK){
                        //image picked
                        //get uri of image
                        Intent data = result.getData();
                        assert data != null;
                        Uri imageUri = data.getData();
                        try {
                            image = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        imageView.setImageURI(imageUri);

                    }
                    else {
                        //cancelled
                        Toast.makeText(MainActivity.this, "Cancelled...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );


    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //here we will handle the result of our intent
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() !=null){
                        //image picked
                        //get bitmap of image
                        Bundle bundle = result.getData().getExtras();
                        Bitmap bitmap = (Bitmap) bundle.get("data");
                        image = bitmap;
                        imageView.setImageBitmap(bitmap);

                    }
                    else {
                        //cancelled
                        Toast.makeText(MainActivity.this, "Cancelled...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );


}
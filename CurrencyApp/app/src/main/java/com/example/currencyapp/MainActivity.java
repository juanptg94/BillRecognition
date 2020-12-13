package com.example.currencyapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.*;

import java.nio.charset.CoderResult;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback;
    int counter=0;
    TTSManager ttsManager= null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA},100 );

        cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.CameraView);
        cameraBridgeViewBase.setCameraPermissionGranted();
        cameraBridgeViewBase.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_ANY);

        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);

        ttsManager=new TTSManager();
        ttsManager.init(this);
        baseLoaderCallback= new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);
                ttsManager=new TTSManager();
                ttsManager.init(getApplicationContext());
                Log.d("Camera conex","Entre al switch");
                switch (status){
                    case BaseLoaderCallback.SUCCESS:
                        cameraBridgeViewBase.enableView();
                        Log.d("Camera conex","Camara activada");


                        break;
                    default:
                        Log.d("Camera conex","No se pudo activar camara");

                        super.onManagerConnected(status);

                        break;
                }
            }
        };
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        Mat frame = inputFrame.rgba();
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);
        Mat grayframe = new Mat();
        Imgproc.cvtColor(frame, grayframe, Imgproc.COLOR_RGB2GRAY);
        Imgproc.equalizeHist(grayframe, grayframe);
        Mat maskinput = new Mat();
        Imgproc.threshold(grayframe, maskinput, 0, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);
        Imgproc.dilate(maskinput,maskinput, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(4, 4)));
        //mgproc.erode(maskinput,maskinput, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)));


        //cuando se necesita enmascarar una imagen. CODIGO
        /*
        Mat hsvframe=new Mat();
        Imgproc.cvtColor(frame,hsvframe,Imgproc.COLOR_BGR2HSV );
        Mat maskinput= new Mat();
        Core.inRange(hsvframe,new Scalar(38,0,194),new Scalar(158,238,255),maskinput);//configuración con cam de mi madre.
        */


        //Log.d("canales","number of channels "+frame.channels()+", maskch: "+maskinput.channels());

        Imgproc.cvtColor(maskinput, maskinput, Imgproc.COLOR_GRAY2RGB, 3);

        Core.bitwise_and(frame, maskinput, maskinput);
        //Scalar medias= Core.mean(maskinput);

        Mat copyframe=frame.clone();
        Imgproc.cvtColor(frame,frame,Imgproc.COLOR_RGBA2RGB);
        Imgproc.cvtColor(frame,frame,Imgproc.COLOR_RGB2HSV);
        List<Mat> channels=new ArrayList<>();
        Core.split(frame,channels);

        Mat red, blue, green;
        red=new Mat();
        blue=new Mat();
        green=new Mat();

        Core.inRange(channels.get(0), new Scalar(0), new Scalar(30), red); // red
        Core.inRange(channels.get(0), new Scalar(30), new Scalar(90), green); // red
        Core.inRange(channels.get(0), new Scalar(90), new Scalar(150), blue); // red
// ... do the same for blue, green, etc only changing the Scalar values

        double image_size = frame.cols()*frame.rows();
        double red_percent = (Core.countNonZero(red)/image_size);
        double green_percent = (Core.countNonZero(green)/image_size);
        double blue_percent = (Core.countNonZero(blue)/image_size);
        DecimalFormat df2 = new DecimalFormat("#.##");
        String reds=df2.format(red_percent).toString();
        String greens=df2.format(green_percent).toString();
        String blues=df2.format(blue_percent).toString();


        Imgproc.putText(copyframe,"Mean: R:"+ reds+", G:"+greens+", B:"+blues,new Point(50,30),3,0.5,new Scalar(255,0,0),2);
        float error=0.04f; //valor de error
        float fred=Float.valueOf(reds);
        float fgreen=Float.valueOf(greens);
        float fblue=Float.valueOf(blues);
        Log.d("vals","VALORRR: "+Float.toString(fred));
        //BILLETE DE 10.000
        if(fred>=0.34-error && fred <=0.34+error && fgreen>=0.16-error && fgreen <=0.16+error && fblue>=0.23-error && fblue <=0.23+error ){//billete de 1000 parte frontal.rgb=0.30,0.11,0.24

            Imgproc.putText(copyframe,"Billete de 10000",new Point(50,330),3,0.5,new Scalar(0,255,0),2);
            ttsManager.initQueue("Billete de diez mil");

        }
        else if(fred>=0.38-error && fred <=0.38+error && fgreen>=0.23-error && fgreen <=0.23+error && fblue>=0.28-error && fblue <=0.28+error){//billete de 1000 parte trasera.rgb=0.32,0.18,0.34
            Imgproc.putText(copyframe,"Billete de 10000 ",new Point(50,330),3,0.5,new Scalar(0,255,0),2);
            ttsManager.initQueue("Billete de diez mil");


        }
        //BILLETE DE 1.000
        else if(fred>=0.46 -error && fred <=0.46 +error && fgreen>=0.16-error && fgreen <=0.16+error && fblue>=0.36-error && fblue <=0.36+error ){//billete de 1000 parte frontal.rgb=0.30,0.11,0.24

            Imgproc.putText(copyframe,"Billete de 1000 ",new Point(50,330),3,0.5,new Scalar(0,255,0),2);
            ttsManager.initQueue("Billete de mil ");

        }
        else if(fred>=0.40-error && fred <=0.40+error && fgreen>=0.28-error && fgreen <=0.28+error && fblue>=0.31-error && fblue <=0.31+error){//billete de 1000 parte trasera.rgb=0.32,0.18,0.34
            Imgproc.putText(copyframe,"Billete de 1000 ",new Point(50,330),3,0.5,new Scalar(0,255,0),2);
            ttsManager.initQueue("Billete de mil ");


        }
        //BILLETE DE 2.000
        else if(fred>=0.48-error && fred <=0.48+error && fgreen>=0.16-error && fgreen <=0.16+error && fblue>=0.30-error && fblue <=0.30+error ){//billete de 1000 parte frontal.rgb=0.30,0.11,0.24

            Imgproc.putText(copyframe,"Billete de 2000 ",new Point(50,330),3,0.5,new Scalar(0,255,0),2);
            ttsManager.initQueue("Billete de dos mil ");

        }
        else if(fred>=0.35-error && fred <=0.35+error && fgreen>=0.20-error && fgreen <=0.20+error && fblue>=0.36-error && fblue <=0.36+error){//billete de 1000 parte trasera.rgb=0.32,0.18,0.34
            Imgproc.putText(copyframe,"Billete de 2000 ",new Point(50,330),3,0.5,new Scalar(0,255,0),2);
            ttsManager.initQueue("Billete de dos mil ");


        }

        //BILLETE DE 5.000
        else if(fred>=0.46-error && fred <=0.46+error && fgreen>=0.20-error && fgreen <=0.20+error && fblue>=0.27-error && fblue <=0.27+error ){//billete de 1000 parte frontal.rgb=0.30,0.11,0.24

            Imgproc.putText(copyframe,"Billete de 5000 ",new Point(50,330),3,0.5,new Scalar(0,255,0),2);
            ttsManager.initQueue("Billete de cinco mil ");

        }
        else if(fred>=0.37-error && fred <=0.37+error && fgreen>=0.23-error && fgreen <=0.23+error && fblue>=0.40-error && fblue <=0.40+error){//billete de 1000 parte trasera.rgb=0.32,0.18,0.34
            Imgproc.putText(copyframe,"Billete de 5000 ",new Point(50,330),3,0.5,new Scalar(0,255,0),2);
            ttsManager.initQueue("Billete de cinco mil ");



        }
        //BILLETE DE 20.000
        else if(fred>=0.31-error && fred <=0.31+error && fgreen>=0.27-error && fgreen <=0.27+error && fblue>=0.50-error && fblue <=0.50+error ){//billete de 1000 parte frontal.rgb=0.30,0.11,0.24

            Imgproc.putText(copyframe,"Billete de 20000 ",new Point(50,330),3,0.5,new Scalar(0,255,0),2);
            ttsManager.initQueue("Billete de veinte mil ");

        }
        else if(fred>=0.34-error && fred <=0.34+error && fgreen>=0.27-error && fgreen <=0.27+error && fblue>=0.34-error && fblue <=0.34+error){//billete de 1000 parte trasera.rgb=0.32,0.18,0.34
            Imgproc.putText(copyframe,"Billete de 20000 ",new Point(50,330),3,0.5,new Scalar(0,255,0),2);
            ttsManager.initQueue("Billete de veinte mil ");




        }
        //BILLETE DE 50.000
        else if(fred>=0.34-error && fred <=0.34+error && fgreen>=0.26-error && fgreen <=0.26+error && fblue>=0.30-error && fblue <=0.30+error ){//billete de 1000 parte frontal.rgb=0.30,0.11,0.24

            Imgproc.putText(copyframe,"Billete de 50000 ",new Point(50,330),3,0.5,new Scalar(0,255,0),2);
            ttsManager.initQueue("Billete de cincuenta mil ");

        }
        else if(fred>=0.30-error && fred <=0.30+error && fgreen>=0.22-error && fgreen <=0.22+error && fblue>=0.43-error && fblue <=0.43+error){//billete de 1000 parte trasera.rgb=0.32,0.18,0.34
            Imgproc.putText(copyframe,"Billete de 50000 ",new Point(50,330),3,0.5,new Scalar(0,255,0),2);
            ttsManager.initQueue("Billete de cincuenta mil ");



        }


        grayframe.release();
        maskinput.release();
        red.release();
        green.release();
        blue.release();
        for(int i=0;i<channels.size();i++){
            channels.get(i).release();


        }
        channels.clear();




        System.gc();

        return copyframe;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!OpenCVLoader.initDebug()){
            Toast.makeText(getApplicationContext(),"There's a problem you :l",Toast.LENGTH_SHORT);
        }
        else{
            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(cameraBridgeViewBase!=null){
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cameraBridgeViewBase!=null){
            cameraBridgeViewBase.disableView();
        }
        ttsManager.shutDown();
    }
}
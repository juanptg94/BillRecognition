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
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.*;

import java.nio.charset.CoderResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback;
    int counter=0;

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

        baseLoaderCallback= new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);
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
        Imgproc.threshold(grayframe, maskinput, 0, 255, Imgproc.THRESH_OTSU | Imgproc.THRESH_BINARY);


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
        List<Mat> channels= new ArrayList<>();
        Core.split(maskinput,channels);
        Scalar sum= new Scalar(0,0,0);
        for(int i=0;i<3;i++){
            sum=Core.sumElems(maskinput);
        }

        double totalsum=sum.val[0]+sum.val[1]+sum.val[2];
        totalsum= (float) totalsum;
        float perred= (float) (sum.val[0]/totalsum * 100);
        float pergreen=(float) (sum.val[1]/totalsum * 100);
        float perblue=(float)(sum.val[2]/totalsum * 100);




        /*Scalar sums=new Scalar(0,0,0);
        sums=Core.sumElems(maskinput);
        double totalsum=sums.val[0]+sums.val[1]+sums.val[2];

        double perred=sums.val[0]/totalsum * 100;
        double pergreen=sums.val[1]/totalsum * 100;
        double perblue=sums.val[2]/totalsum * 100;*/


        /*int color_red_counter = 0;
        int color_green_counter = 0;
        int color_blue_counter=0;


        for (int i=0; i<maskinput.rows(); i++)
        {

            for (int j=0; j<maskinput.cols(); j++)
            {
                //Log.d("dimen","numfil "+maskinput.rows()+",numcol "+maskinput.cols());
                double[] hsv = maskinput.get(i,j);

                for (int k = 0; k < 3; k++) //Runs for the available number of channels
                {
                    Log.d("hsv","valor de get"+Integer.toString(k)+": "+ hsv[k]);
                }
                if(hsv[0]>0 && hsv[0]<30 ||hsv[0]>150 && hsv[0]<180    )//red
                    color_red_counter++;

                if(hsv[0]>=30 && hsv[0]<90 )//green
                    color_green_counter++;

                if(hsv[0]>=90 && hsv[0]<150 )//blue
                    color_blue_counter++;

            }
        }*/
        //Log.d("hsv","valores"+Integer.toString(color_red_counter)+","+Integer.toString(color_green_counter)+","+Integer.toString(color_blue_counter));
        /*double percentagered = 0;
        double percentagegreen = 0;
        double percentageblue = 0;
        if(color_red_counter+color_green_counter+color_blue_counter!=0){
             percentagered = color_red_counter/(color_red_counter+color_green_counter+color_blue_counter)*100;
             percentagegreen = color_green_counter/(color_red_counter+color_green_counter+color_blue_counter)*100;
             percentageblue = color_blue_counter/(color_red_counter+color_green_counter+color_blue_counter)*100;
        }




        */
        Imgproc.putText(maskinput,"Mean: R: "+Double.toString(perred)+", G: "+Double.toString(pergreen)+", B: "+Double.toString(perblue),new Point(50,30),3,1,new Scalar(255,0,0),2);







        frame=maskinput;


        return frame;
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
    }
}
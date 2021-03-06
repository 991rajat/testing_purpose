package com.example.miniproject;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;

import java.io.IOException;
import java.util.List;

import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Arrays;

import kotlin.jvm.internal.Intrinsics;

/*
 *  CameraPreview.java
 */
public class CameraPreview implements SurfaceHolder.Callback, Camera.PreviewCallback, Camera.FaceDetectionListener
{

    private Camera mCamera = null;
    private ImageView MyCameraPreview = null;
    Bitmap bitmap = null;
    private int[] pixels = null;

    private int imageFormat;
    private int PreviewSizeWidth;
    private int PreviewSizeHeight;
    private boolean bProcessing = false;
    private Context context;
    Rect rect;

    Handler mHandler = new Handler(Looper.getMainLooper());
    private byte[] frameData= null;

    public CameraPreview(Context context, ImageView CameraPreview)
    {
        MyCameraPreview = CameraPreview;
        this.context = context;

    }

    @Override
    public void onPreviewFrame(byte[] arg0, Camera arg1)
    {
        Log.d(">>>>>>>>",arg0.toString());
        frameData = arg0;

        // At preview mode, the frame data will push to here.
//        if (imageFormat == ImageFormat.NV21)
//        {
//            //We only accept the NV21(YUV420) format.
//            frameData = arg0;
//            Log.d(">",arg1.getParameters().getPreviewSize().width+"--"+arg1.getParameters().getPreviewSize().height);
//            //bitmap = Bitmap.createBitmap(arg1.wi, PreviewSizeHeight, Bitmap.Config.ARGB_8888);
////            if ( !bProcessing )
////            {
////                FrameData = arg0;
////                mHandler.post(DoImageProcessing);
////            }
//        }
    }

    public void onPause()
    {
        mCamera.stopPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int imagefor, int w, int h)
    {
        Camera.Parameters parameter = mCamera.getParameters();


//        List<Size> sizes = parameter.getSupportedPreviewSizes();
//        Size optimalSize = getOptimalPreviewSize(sizes,context.getResources().getDisplayMetrics().widthPixels, context.getResources().getDisplayMetrics().heightPixels);
//        PreviewSizeWidth =optimalSize.width;
//        PreviewSizeHeight = optimalSize.height;

        parameter.set("orientation","portrait");
        mCamera.setDisplayOrientation(90);

        // Set the camera preview size

        imageFormat = parameter.getPreviewFormat();
        Log.d(">W H",w+"--"+h);

        List<Size> allSizes = parameter.getSupportedPictureSizes();
        Camera.Size size = setOptimalPreviewSize(parameter,w,h); // get top size
        for (int i = 0; i < allSizes.size(); i++) {
            Log.d(">>>",""+allSizes.get(i).width+"--"+allSizes.get(i).height);
        }

        PreviewSizeHeight =768;
        PreviewSizeWidth = 1024;
        Log.d("---------",PreviewSizeHeight+"-"+PreviewSizeWidth);
        bitmap = Bitmap.createBitmap(PreviewSizeWidth, PreviewSizeHeight, Bitmap.Config.ARGB_8888);
        pixels = new int[PreviewSizeWidth * PreviewSizeHeight];
        parameter.setPreviewSize(PreviewSizeWidth, PreviewSizeHeight);

////set max Picture Size
//        parameter.setPreviewSize(size.width, size.height);
//        PreviewSizeWidth =size.width;
//        PreviewSizeHeight = size.height;
//        if (parameter.isAutoExposureLockSupported())
//            parameter.setAutoExposureLock(true);

//                     params.autoExposureLock = true

//        if (parameter.isVideoStabilizationSupported())
//            parameter.setVideoStabilization(true);

//                if (supportFeature(params.supportedAntibanding, antibanding_auto))
//                    params.antibanding = antibanding_auto


        mCamera.setParameters(parameter);
        mCamera.startPreview();
    }
    private Camera.Size setOptimalPreviewSize(Camera.Parameters cameraParameters, int width, int height) {
        List<Camera.Size> previewSizes = cameraParameters.getSupportedPreviewSizes();
        float targetRatio = (float) width / height;
        Camera.Size previewSize = Util.getOptimalPreviewSize((Activity) context, previewSizes, targetRatio);
        //Log.d("PPP",PreviewSizeHeight+"-"+PreviewSizeWidth+"");
        return previewSize;
//        cameraParameters.setPreviewSize(previewSize.width, previewSize.height);
//        PreviewSizeWidth = previewSize.width;
//        PreviewSizeHeight = previewSize.height;

    }


    @Override
    public void surfaceCreated(SurfaceHolder arg0)
    {
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        try
        {
            // If did not set the SurfaceHolder, the preview area will be black.
            mCamera.setPreviewDisplay(arg0);
            mCamera.setPreviewCallback(this);
            mCamera.setFaceDetectionListener(this);
            mCamera.startFaceDetection();
        }
        catch (IOException e)
        {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0)
    {
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    //  public native boolean ImageProcessing(int width, int height, byte[] NV21FrameData, int [] pixels);
//  static
//  {
//     System.loadLibrary("ImageProcessing");
//  }
//
    private Runnable DoImageProcessing = new Runnable()
    {
        public void run()
        {
            Log.d("TAG", "DoImageProcessing():");
            bProcessing = true;
            long arr[]= Native.analiseFrame(frameData,90, PreviewSizeWidth,PreviewSizeHeight,rect);
            Log.d("Land", Arrays.toString(arr));
            Log.d("Frames", "=="+frameData.length+"=="+pixels.length);
            //bitmap= BitmapFactory.decodeByteArray(frameData,0,frameData.length);
            //Log.d(">>>>",bitm.toString());
            bitmap.setPixels(pixels, 0, PreviewSizeWidth, 0, 0, PreviewSizeWidth, PreviewSizeHeight);

//            ByteBuffer buffer = ByteBuffer.wrap(frameData);
//            Log.i("", "Bitmap size = " + bitmap.getByteCount());
//            Log.i("", "Buffer size = " + buffer.capacity());
//            buffer.rewind();
//            bitmap.copyPixelsFromBuffer(buffer);
//            bitmap.setPixels(pixels, 0, PreviewSizeWidth, 0, 0, PreviewSizeWidth, PreviewSizeHeight);
            //MyCameraPreview.setImageBitmap(bitmap);
            //processDrowsiness(arr);
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//            Bitmap bitma = BitmapFactory.decodeByteArray(frameData, 0, frameData.length, options);
            //Log.d(">>",bitmap.toString());
            processBitmap(bitmap,rect,arr);
            bProcessing = false;
        }
    };

    private void processDrowsiness(long[] arr) {
        Log.d(">>",arr.length+"");
        long[] left = new long[12];
        long[] right = new long[12];
        int i,k=0;
        for(i=20;i<32;i++)
            left[k++]=arr[i];
        k=0;
        for(i=32;i<44;i++)
            right[k++]=arr[i];


    }



    private void processBitmap(Bitmap arr, Rect ret,long[] landmarks) {
        Rect r = new Rect();
        r.set(ret);
//        Log.d(">>","wid heigh"+(r.right-r.left)+(r.bottom-r.top));
        //r = mapTo(r,ret.right-ret.left,ret.bottom-ret.top,90);
        //ret.set(r);
        Canvas c = new Canvas(arr);
        Paint p = new Paint();
        p.setColor(Color.rgb(255, 160, 0));
        p.setStrokeWidth(5f);
        p.setStyle(Paint.Style.STROKE);
        c.drawRect(ret, p);
        int size = landmarks.length;
        for(int i=0;i<size;i+=2)
        {
            p.setColor(Color.YELLOW);
            p.setStyle(Paint.Style.FILL);
            c.drawCircle(landmarks[i], landmarks[i+1], 5f, p);
        }
        MyCameraPreview.setImageBitmap(arr);

    }



    @Override
    public void onFaceDetection(Camera.Face[] faces, Camera camera) {
        Log.d("t>>>>>>>>>",""+faces.toString());
        Camera.Face bestface = null;
        int score=30;
        for(Camera.Face face: faces)
        {
            if(face.score>score){
                score=face.score;
                bestface=face;

            }
        }
        if(bestface!=null)
        {   Log.d(">>bestface metrics","bestface"+bestface.rect.top+bestface.rect.bottom+"-"+bestface.rect.left+"-"+bestface.rect.right);
            Log.d(">>display metrics",""+context.getResources().getDisplayMetrics().widthPixels+""+context.getResources().getDisplayMetrics().heightPixels);
            rect = mapTo(bestface.rect,PreviewSizeWidth,PreviewSizeHeight,90);

            if(!bProcessing)
            {
                mHandler.post(DoImageProcessing);
            }
        }



    }
    public final Rect mapTo(@NotNull Rect $this$mapTo, int width, int height, int rotation) {
        Intrinsics.checkParameterIsNotNull($this$mapTo, "$this$mapTo");

        float hw = (float)$this$mapTo.width() / 2.0F;
        float hh = (float)$this$mapTo.height() / 2.0F;
        float cx = (float)$this$mapTo.left + hw;
        float cy = (float)$this$mapTo.top + hh;
        float side = (hh + hw) / 2.0F;
        float left = cx - side;
        float right = cx + side;
        float top = cy - side;
        float bottom = cy + side;
        float l = (left + (float)1000) / 2000.0F;
        float r = (right + (float)1000) / 2000.0F;
        float t = (top + (float)1000) / 2000.0F;
        float b = (bottom + (float)1000) / 2000.0F;
        int w;
        int h;
        float wr;
        float hr;
        int x0;
        int y0;
        switch(rotation) {
            case 0:
                w = width < height ? height : width;
                h = width < height ? width : height;
                $this$mapTo.left = Math.round((float)w - (float)w * r);
                $this$mapTo.right = Math.round((float)w - (float)w * l);
                $this$mapTo.top = Math.round((float)h * t);
                $this$mapTo.bottom = Math.round((float)h * b);
                wr = (float)$this$mapTo.width() * 0.5F;
                hr = (float)$this$mapTo.height() * 0.5F;
                x0 = $this$mapTo.centerX();
                y0 = $this$mapTo.centerY();
                $this$mapTo.left = (int)((float)x0 - hr);
                $this$mapTo.right = (int)((float)x0 + hr);
                $this$mapTo.top = (int)((float)y0 - wr);
                $this$mapTo.bottom = (int)((float)y0 + wr);
                break;
            case 90:
                w = width > height ? height : width;
                h = width > height ? width : height;
                $this$mapTo.left = Math.round((float)w - (float)w * b);
                $this$mapTo.right = Math.round((float)w - (float)w * t);
                $this$mapTo.top = Math.round((float)h - (float)h * r);
                $this$mapTo.bottom = Math.round((float)h - (float)h * l);
                break;
            case 180:
                w = width < height ? height : width;
                h = width < height ? width : height;
                $this$mapTo.left = Math.round((float)w * l);
                $this$mapTo.right = Math.round((float)w * r);
                $this$mapTo.top = Math.round((float)h - (float)h * b);
                $this$mapTo.bottom = Math.round((float)h - (float)h * t);
                wr = (float)$this$mapTo.width() * 0.5F;
                hr = (float)$this$mapTo.height() * 0.5F;
                x0 = $this$mapTo.centerX();
                y0 = $this$mapTo.centerY();
                $this$mapTo.left = (int)((float)x0 - hr);
                $this$mapTo.right = (int)((float)x0 + hr);
                $this$mapTo.top = (int)((float)y0 - wr);
                $this$mapTo.bottom = (int)((float)y0 + wr);
        }

        return $this$mapTo;
    }

}


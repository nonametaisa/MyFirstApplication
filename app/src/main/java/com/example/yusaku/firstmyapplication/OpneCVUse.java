package com.example.yusaku.firstmyapplication;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Yusaku on 2015/11/30.
 */
public class OpneCVUse {


    public Point mCirclCenterPosition ;
    private double mRadius;   //円の半径
    public Mat mCirclMat;



    public Bitmap setCircleMat(Bitmap bitmap) {

        Mat src = getMat(bitmap);
        mCirclMat = new Mat();
        double[] circleData ;
        double circleRaduian;
        Mat circleMat;
        Mat gray = new Mat();
        Point circleCenter = new Point();
        mCirclCenterPosition = new Point();

        Imgproc.GaussianBlur(src, src, new Size(9, 9), 2, 2);
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGB2GRAY);


        Imgproc.HoughCircles(gray, mCirclMat, Imgproc.CV_HOUGH_GRADIENT, 2, gray.rows() / 4, 100, 50, 5, 50);
        circleMat = mCirclMat;

        for (int i = 0; i < circleMat.cols(); i++) {

            circleData = circleMat.get(0, i);
            circleCenter.x = Math.round(circleData[0]);
            circleCenter.y = Math.round(circleData[1]);
            circleRaduian = Math.round(circleData[2]);
            Core.circle(src, circleCenter, (int) circleRaduian, new Scalar(255, 0, 0), 10);
        }

        return getBitmap(src);
    }

    public Bitmap getCirclePosition(int countPush , Bitmap bitmap){
        double[] circleData ;
        double circleRaduian;

        circleData = mCirclMat.get(0,countPush);
        mCirclCenterPosition.x = Math.round(circleData[0]);
        mCirclCenterPosition.y = Math.round(circleData[1]);
        mRadius = Math.round(circleData[2]) + 40;

        Mat m = roi((int)(mCirclCenterPosition.x -(mRadius /2)),
                (int)(mCirclCenterPosition.y - (mRadius /2)),(int)mRadius,(int)mRadius,bitmap);//端を切り取るためmRadius/2

        return getBitmap(m);




    }

    public Mat getMat(Bitmap src) {

        Mat mat =new Mat(src.getWidth() , src.getHeight() , CvType.CV_32FC1);

        Utils.bitmapToMat(src,mat);
        return mat;


    }

    private Bitmap getBitmap(Mat src) {

        Bitmap dst = Bitmap.createBitmap(src.width(), src.height(), Bitmap.Config.ARGB_8888); // 1ピクセル4バイト（、、各8ビット）
        Utils.matToBitmap(src, dst);
        return dst;


    }

    private Mat roi(Point p1,Point p2,Mat  m ){

        if (m.cols() > p1.x){
            p1.x = m.cols();
        }else  if (p1.x < 0){
            p1.x = 0;
        }
        if (m.rows()> p1.y){
            p1.y = m.rows();
        }else if (p1.y < 0 ){
            p1.y = 0;
        }
        if (m.cols() > p2.x){
            p2.x = m.cols();
        }else if (p2.x < 0){
            p2.x = 0;
        }
        if (m.rows() > p2.y){
            p2.y = m.rows();
        }else if (p2.y < 0 ){
            p2.y = 0;
        }

        Rect rect = new Rect(p1, p2);

        return new Mat(m,rect);

    }

    private Mat roi(Point p1,Point p2,Bitmap  bitmap ){


        if (bitmap.getWidth() > p1.x){
            p1.x = bitmap.getWidth();
        }else  if (p1.x < 0){
            p1.x = 0;
        }
        if (bitmap.getHeight() > p1.y){
            p1.y = bitmap.getHeight();
        }else if (p1.y < 0 ){
            p1.y = 0;
        }
        if (bitmap.getWidth() > p2.x){
            p2.x = bitmap.getWidth();
        }else if (p2.x < 0){
            p2.x = 0;
        }
        if (bitmap.getHeight() > p2.y){
            p2.y = bitmap.getHeight();
        }else if (p2.y < 0 ){
            p2.y = 0;
        }

        Rect rect = new Rect(p1, p2);

        return new Mat(getMat(bitmap),rect);

    }

    private Mat roi(int x , int y , int width , int height, Bitmap  bitmap ){

        if ((x + width) > bitmap.getWidth()){
            x = bitmap.getWidth() - (x - bitmap.getWidth());
        }else if (x < 0){
            width = width + x;
            x = 0;
        }

        if ((y + height) > bitmap.getHeight()){
            y = bitmap.getHeight() -(y - bitmap.getHeight());
        }else if (y < 0){
            height = height +y;
            y = 0;
        }

        Rect rect = new Rect(x,y,width,height);

        return new Mat(getMat(bitmap),rect);

    }






}

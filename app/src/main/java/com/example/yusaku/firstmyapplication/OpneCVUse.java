package com.example.yusaku.firstmyapplication;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

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


    //////////////CIRCLE////////////////////////

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
                (int)(mCirclCenterPosition.y - (mRadius /2)),(int)mRadius,(int)mRadius,bitmap);//円の端を切り取るためmRadius/2

        return getBitmap(m);
    }
//////////////CIRCLE////////////////////////
//////////////LINE//////////////////////////

    public float [] afloat;
    public float [] bfloat;
    public int pIntLineCount = 0;
    public int flagLineCount = 0;

    public Bitmap getLineImage(Bitmap bitmap){
        Mat src = getMat(bitmap);
        Mat edge = new Mat();
        Mat lines = new Mat();
        Imgproc.cvtColor(src , edge, Imgproc.COLOR_RGB2GRAY);

        Imgproc.Canny(edge, edge, 80, 100);
        Imgproc.HoughLinesP(edge, lines, 1, Math.PI / 180, 50, 150, 10);
        pIntLineCount =0;
        src =fncDrwLine(lines,src);
        Utils.matToBitmap(src,bitmap);
        bitmap = serchDraw((int)mCirclCenterPosition.x,(int)mCirclCenterPosition.y,bitmap);


        return bitmap;

    }

    private Mat fncDrwLine(Mat lin,Mat img) {
        double[] data;
        Point pt1 = new Point();
        Point pt2 = new Point();

        afloat = new float[150];
        bfloat = new float[150];

        for (int i = 0; i < lin.cols(); i++){
            data = lin.get(0, i);
            pt1.x = data[0];
            pt1.y = data[1];
            pt2.x = data[2];
            pt2.y = data[3];
            Core.line(img, pt1, pt2, new Scalar(255, 0, 0), 5);

            if (pt1.x - pt2.x != 0) {
                //傾き
                afloat[pIntLineCount] = (float) ((pt1.y - pt2.y) / (pt1.x - pt2.x));
                //切片
                bfloat[pIntLineCount] = (float)(pt2.y -afloat[pIntLineCount] *pt2.x);

                pIntLineCount++;

                if (pIntLineCount >= 150){
                    pIntLineCount =0;
                    flagLineCount = 150;
                }
            }


        }
        Log.e("cunt Number is ", String.valueOf(pIntLineCount));
        return img;
    }

    public Bitmap serchDraw(int x ,int y , Bitmap src ){

        int min = 0, max = 0;

        float matchLineMini , matchLineMax;
        float tmp;
        float result ;
        float minResult  = -9999, maxResult = 9999;

        if (flagLineCount != 0){
            for (int i = 0; i <flagLineCount ; i++){
                //     tmp = afloat[i] * x + bfloat[i];
                //     result = y - tmp;
                tmp =  (y -bfloat[i]) / afloat[i];
                result = tmp - x;

                if (result < 0 &&  minResult < result){

                    matchLineMini = tmp;
                    min = i;
                    minResult = result;
                    Log.e("tmpcount =" , String.valueOf(min));

                }else if (result > 0 && result < maxResult){
                    matchLineMax = tmp;
                    max = i;
                    maxResult = result;
                    Log.e("tmpcount =" , String.valueOf(max));
                }

            }

        }else {


            for (int i = 0; i < pIntLineCount; i++) {

                tmp =  (y -bfloat[i]) / afloat[i];
                result = tmp - x;


                if (result < 0 && minResult < result) {

                    matchLineMini = tmp;
                    min = i;
                    minResult = result;
                    Log.e("min count =" , String.valueOf(min));
                } else if (result > 0 && result < maxResult) {
                    matchLineMax = tmp;
                    max = i;
                    maxResult = result;
                    Log.e("max count =", String.valueOf(max));
                }

            }
        }
        Canvas canvas;
        canvas = new Canvas(src);

        Paint paint;
        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(10);
        paint.setAntiAlias(true);

        // キャンバスに直線を描画する
        canvas.drawLine((0 -bfloat[min]) / afloat[min],0,(src.getHeight() - bfloat[min]) / afloat[min],src.getHeight(),paint);
        paint.setColor(Color.YELLOW);
        canvas.drawLine((0 -bfloat[max]) / afloat[max],0,(src.getHeight() - bfloat[max]) / afloat[max],src.getHeight(),paint);
        return src;

    }
///////////////////LINE//////////////////////////
/////////////////////TM//////////////////////////

    private Mat mTMResult ;
    public Point pTMPoint;
  public Bitmap tenplateMatch(Mat conparedImageMat, Mat conparImageMat , int Flag , int ixy, Mat moto){
      Point tmPoint;

      if (mTMResult != null){
          mTMResult = new Mat(conparedImageMat.rows() - conparImageMat.rows() +1 , conparedImageMat.cols() - conparImageMat.cols() -1 , CvType.CV_32FC1);
      }

      Imgproc.matchTemplate(conparedImageMat, conparImageMat, mTMResult, Imgproc.TM_CCOEFF_NORMED);

      Core.MinMaxLocResult minMaxLocResult =Core.minMaxLoc(mTMResult);  //この値を比べる次、一つ前からだいたいの値を範囲として投票が十分でない場合の値ははじくようにしたい

      pTMPoint = minMaxLocResult.maxLoc;
      tmPoint = new Point(conparImageMat.rows() + pTMPoint.x  , conparedImageMat.cols() + pTMPoint.y);

      Core.rectangle(conparedImageMat,pTMPoint , tmPoint , new Scalar(255,0,0));

      return getBitmap(conparedImageMat);


  }
/////////////////////TM//////////////////////////
///////////////////OTHER/////////////////////////

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
///////////////////OTHER/////////////////////////





}

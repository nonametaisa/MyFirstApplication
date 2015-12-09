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

   //     Mat m = roi((int)(mCirclCenterPosition.x -(mRadius /2)),
   //             (int)(mCirclCenterPosition.y - (mRadius /2)),(int)mRadius ,(int)mRadius,bitmap);//円の端を切り取るためmRadius/2
   //     Mat m = roi((int)(mCirclCenterPosition.x -(mRadius /2)),
   //             (int)(mCirclCenterPosition.y - (mRadius /2) ),(int)mRadius  ,(int)mRadius,bitmap);
  //      Mat m = roi((int)(mCirclCenterPosition.x -(mRadius /2) -10),
  //              (int)(mCirclCenterPosition.y - (mRadius /2) -10 ),(int)mRadius - 20  ,(int)mRadius -20, bitmap);
        Mat m = roi((int)(mCirclCenterPosition.x -(mRadius /2) + 5),
                (int)(mCirclCenterPosition.y - (mRadius /2) +5 ),(int)mRadius -10  ,(int)mRadius -10, bitmap);
        return getBitmap(m);
    }


//////////////CIRCLE////////////////////////
//////////////LINE//////////////////////////
//////////////必要と思われるもの//////////////
    public double slopePlusA = 0 , slopeMinus = 0;
    public double interceptPlus = 0 , interceptMinus = 0;
    public Point pLeftLinePoint , pRightLinePoint;
    ///////////////////////////////////////////////////////////
    public float [] afloat;
    public float [] bfloat;
    public int pIntLineCount ;

    private int mCount;


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
        bitmap = serchDraw((int)mCirclCenterPosition.x,(int)mCirclCenterPosition.y,bitmap , lines);


        return bitmap;

    }

    private Mat fncDrwLine(Mat lin,Mat img) {
        double[] data;
        Point pt1 = new Point();
        Point pt2 = new Point();

        afloat = new float[lin.cols()];
        bfloat = new float[lin.cols()];

        for (int i = 0; i < lin.cols(); i++){
            data = lin.get(0, i);
            pt1.x = data[0];
            pt1.y = data[1];
            pt2.x = data[2];
            pt2.y = data[3];
            Core.line(img, pt1, pt2, new Scalar(255, 0, 0), 5);

            if (pt1.x - pt2.x != 0) {
                //傾き
                afloat[i] = (float) ((pt1.y - pt2.y) / (pt1.x - pt2.x));
                //切片
                bfloat[i] = (float)(pt2.y -afloat[pIntLineCount] *pt2.x);



            }else {
                afloat[i] = 0;
                bfloat[i] = (float)(pt2.y -afloat[pIntLineCount] *pt2.x);

            }

            pIntLineCount++;


        }
        mCount = lin.cols();
        Log.e("cunt Number is ", String.valueOf(pIntLineCount));
        return img;
    }

    private Bitmap serchDraw(int x ,int y , Bitmap src  , Mat ln){

        int min = 0, max = 0;

        float matchLineMini , matchLineMax;
        float tmp;
        float result ;
        float minResult  = -9999, maxResult = 9999;


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
        slopePlusA = afloat[min];
        slopeMinus = afloat[max];
        interceptPlus = bfloat[min];
        interceptMinus = bfloat[max];
        /*　コメントアウト１
        circleMatに入っている線分の端を見つけるのが目的だったが
        そもそもレーン端をさしていない動画があったのでコメントアウト中 */
        setpLeftLinePoint(ln, min);
        setpRightLinePoint(ln, max);
        paint.setColor(Color.WHITE);
        canvas.drawCircle((float)pLeftLinePoint.x ,(float) pLeftLinePoint.y , 5 , paint);
        paint.setColor(Color.BLACK);
        canvas.drawCircle((float)pRightLinePoint.x ,(float) pRightLinePoint.y , 5 , paint);
   // */
        afloat = null;
        bfloat = null;
        return src;

    }

    private void setpLeftLinePoint(Mat ln, int min){
        double[] data;
        data = ln.get(0,min);
        pLeftLinePoint = new Point();
        if (data[1] < data[3]){
            pLeftLinePoint.x = data[2];
            pLeftLinePoint.y = data[3];
        }else {
            pLeftLinePoint.x = data[0];
            pLeftLinePoint.y = data[1];
        }

    }

    private void setpRightLinePoint(Mat ln, int max){
        double[] data;
        data = ln.get(0,max);
        pRightLinePoint = new Point();
        if (data[1] < data[3]){
            pRightLinePoint.x = data[2];
            pRightLinePoint.y = data[3];
        }else {
            pRightLinePoint.x = data[0];
            pRightLinePoint.y = data[1];
        }
    }

    public Point getPlusIntersection(){

        double alpha , beta ;
        double x ,y;
        alpha =  -(1 /slopeMinus);
        beta =  (-alpha) * mCirclCenterPosition.x + mCirclCenterPosition.y ;
        x = (beta - interceptMinus) / (slopeMinus - alpha);
        y = slopeMinus * x + interceptMinus;
        return new Point(x,y);

    }


    public Bitmap getPlusIntersection(Bitmap bitmap){

        double alpha , beta ;
        double x ,y;
        Mat m;
        alpha =  -(1 /slopeMinus);
        beta =  (-alpha) * mCirclCenterPosition.x + mCirclCenterPosition.y ;
        x = (beta - interceptMinus) / (slopeMinus - alpha);
        y = slopeMinus * x + interceptMinus;
        m = getMat(bitmap);
        Core.circle(m ,new Point(x,y) , 10 , new Scalar(255,0,0) , 10 );

        return getBitmap(m);

    }
    public Bitmap getMinusIntersection(Bitmap bitmap){

        double alpha , beta ;
        double x ,y;
        Mat m;
        alpha =  -(1 /slopePlusA);
        beta =  (-alpha) * mCirclCenterPosition.x + mCirclCenterPosition.y ;
        x = (beta - interceptPlus) / (slopePlusA - alpha);
        y = slopePlusA * x + interceptPlus;

        m = getMat(bitmap);
        Core.circle(m ,new Point(x,y) , 10 , new Scalar(255,0,0) , 10 );

        return getBitmap(m);


    }
    public Point getMinusIntersection(){

        double alpha , beta ;
        double x ,y;

        alpha =  -(1 /slopePlusA);
        beta =  (-alpha) * mCirclCenterPosition.x + mCirclCenterPosition.y ;
        x = (beta - interceptPlus) / (slopePlusA - alpha);
        y = slopePlusA * x + interceptPlus;

        return new Point(x,y);


    }

    public Point ballPosition(Bitmap bitmap){
        Point minusPoint , plusPoint;
        Point ballXPosition = new Point();
        minusPoint =getMinusIntersection();
        plusPoint = getPlusIntersection();


        return ballXPosition;

    }
    public Bitmap ballPosition(Bitmap bitmap , int i){
        Point minusPoint , plusPoint;
        Point ballXPosition = new Point();
        minusPoint =getMinusIntersection();
        plusPoint = getPlusIntersection();
        bitmap = getMinusIntersection(bitmap);
        bitmap = getPlusIntersection(bitmap);

        return bitmap;

    }
///////////////////LINE//////////////////////////
/////////////////////TM//////////////////////////


    public Point pTMPoint;
    public Core.MinMaxLocResult pMinMaxResult;

  public Bitmap tenplateMatch(Mat conparedImageMat, Mat conparImageMat){
      Point tmPoint;
      Mat tmp;
      Mat mTMResult ;
      int x = (int)(mCirclCenterPosition.x -((conparImageMat.cols()/2) + 75) );
      int y = (int)(mCirclCenterPosition.y - ((conparImageMat.rows()/2) + 100));
      int width = 150 + conparImageMat.cols();
      int height = 100 + conparImageMat.rows();

      tmp =roi(x,y, width, height,conparedImageMat );


      mTMResult = new Mat(tmp.cols() - conparImageMat.cols() +1 , tmp.rows() - conparImageMat.rows() -1 , CvType.CV_32FC1);
      Imgproc.matchTemplate(tmp, conparImageMat, mTMResult, Imgproc.TM_CCOEFF_NORMED);


      //mTMResult = new Mat(conparedImageMat.cols() - conparImageMat.cols() +1 , conparedImageMat.rows() - conparImageMat.rows() -1 , CvType.CV_32FC1);
      //Imgproc.matchTemplate(conparedImageMat, conparImageMat, mTMResult, Imgproc.TM_CCOEFF_NORMED);


      pMinMaxResult =Core.minMaxLoc(mTMResult);  //この値を比べる次、一つ前からだいたいの値を範囲として投票が十分でない場合の値ははじくようにしたい

      pTMPoint = pMinMaxResult.maxLoc;
      pTMPoint = new Point(pTMPoint.x + x , pTMPoint.y + y);
      tmPoint = new Point(conparImageMat.cols() + pTMPoint.x  , conparImageMat.rows() + pTMPoint.y);
      mCirclCenterPosition.x = (pTMPoint.x + tmPoint.x)/2;
      mCirclCenterPosition.y =(pTMPoint.y + tmPoint.y) /2;
              Core.rectangle(conparedImageMat,pTMPoint , tmPoint , new Scalar(255,0,0), 5);
      Core.circle(conparedImageMat,mCirclCenterPosition , 10 ,new Scalar(255,0,0));

      return getBitmap(conparedImageMat);



  }
/////////////////////TM//////////////////////////
///////////////////OTHER/////////////////////////

    private int mFrameMinus = 0;
    private int mFramePlus = 0;

    public Bitmap getMinusBallImage(Bitmap bitmap){
        mFrameMinus -=2;

    //    Mat m = roi((int)(mCirclCenterPosition.x -(mRadius /2)),
     //           (int)(mCirclCenterPosition.y - (mRadius /2) -10),(int)mRadius + mFrameMinus ,(int)mRadius +10 + mFrameMinus,bitmap);
        Mat m = roi((int)(mCirclCenterPosition.x -(mRadius /2) + 5),
                (int)(mCirclCenterPosition.y - (mRadius /2) +5 ),(int)mRadius -10  ,(int)mRadius -10, bitmap);
        return getBitmap(m);
    }

    public Bitmap getPlusBallImage(Bitmap bitmap){
        mFramePlus += 2;
   //     Mat m = roi((int)(mCirclCenterPosition.x -(mRadius /2)),
   //             (int)(mCirclCenterPosition.y - (mRadius /2) -10),(int)mRadius + mFramePlus ,(int)mRadius +10 + mFramePlus,bitmap);
        Mat m = roi((int)(mCirclCenterPosition.x -(mRadius /2) + 5),
                (int)(mCirclCenterPosition.y - (mRadius /2) +5 ),(int)mRadius -10  ,(int)mRadius -10, bitmap);
        return getBitmap(m);
    }

    public Bitmap myHomography(Bitmap bitmap){

        double bitmapArray[] = {
                0,                  0,
                bitmap.getWidth() , bitmap.getHeight()
        };

        return bitmap;

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
    private Mat roi(int x , int y , int width , int height, Mat  bitmap ){
        Log.e("first     " ,"x = " +String.valueOf(x) + " y = " + String.valueOf(y) + " width =" + String.valueOf(width) + "height = " + String.valueOf(height) );
        Log.e("cols = " + String.valueOf(bitmap.cols()) , "rows = " +String.valueOf(bitmap.rows()));
        if (x < 0){
            width = width - x;
            x = 0;
        }

        if ((x + width) > bitmap.cols()){
            x = bitmap.cols() - (x - bitmap.cols());
        }

        if (y < 0){
            height = height -y;
            y = 0;
        }

        if ((y + height) > bitmap.rows()){
            y = bitmap.rows() -(y - bitmap.rows());
        }
        Point  p1= new Point(x , y);
        Point p2 = new Point(x +width,y + height);
        Core.rectangle(bitmap,p1,p2 ,new Scalar(255,0,0) , 5);
        Rect rect = new Rect(x,y,width,height);
        Log.e("Second      " ,"x = " +String.valueOf(x) + " y = " + String.valueOf(y) + " width =" + String.valueOf(width) + "height = " + String.valueOf(height) );

        return new Mat(bitmap,rect);

    }

    /* square to arbitrary quadrangle transformation
   (0,0)  (1,0)   竍�   (Px,Py)   (Qx,Qy)
   (0,1)  (1,1)      (Rx,Ry)         (Sx,Sy)  */
    double[][] createSquareToArbitraryQuadrangleTransform(double Px, double Py, double Qx, double Qy, double Rx, double Ry, double Sx, double Sy)
    {
        double[][] mat = new double[3][3];
        mat[0][0] = (Qx*Py - Qy*Px)*(Sx - Rx) - (Sx*Ry - Sy*Rx)*(Qx - Px);
        mat[1][0] = (Qx*Py - Qy*Px)*(Sy - Ry) - (Sx*Ry - Sy*Rx)*(Qy - Py);
        mat[2][0] = (Sy - Ry)*(Qx - Px) + (Rx - Sx)*(Qy - Py);
        mat[0][1] = (Ry*Px - Rx*Py)*(Sx - Qx) - (Sy*Qx - Sx*Qy)*(Rx - Px);
        mat[1][1] = (Ry*Px - Rx*Py)*(Sy - Qy) - (Sy*Qx - Sx*Qy)*(Ry - Py);
        mat[2][1] = (Sx - Qx)*(Ry - Py) - (Sy - Qy)*(Rx - Px);

        mat[2][2] = Sx*Qy - Sy*Qx + Qx*Ry - Qy*Rx + Rx*Sy - Ry*Sx;
        mat[0][2] = Px * mat[2][2];
        mat[1][2] = Py * mat[2][2];

        return mat;
    }

    /* rectangle to arbitrary quadrangle transformation
        (0,0)  (W,0)   竍�   (Px,Py)   (Qx,Qy)
        (0,H)  (W,H)      (Rx,Ry)         (Sx,Sy)  */
    public double[][] createRectangleToArbitraryQuadrangleTransform(int W, int H, double Px, double Py, double Qx, double Qy, double Rx, double Ry, double Sx, double Sy)
    {

        double[][] mat = createSquareToArbitraryQuadrangleTransform(Px, Py, Qx, Qy, Rx, Ry, Sx, Sy);

        mat[0][0] /= W;
        mat[1][0] /= W;
        mat[2][0] /= W;
        mat[0][1] /= H;
        mat[1][1] /= H;
        mat[2][1] /= H;

        return mat;
    }

    public Mat myMatrix(Bitmap bitmap , Mat mat , double x ,double y){
        double p[] = new double[3];
        Mat mat2 = new Mat(3,1, CvType.CV_32FC1);
        Mat dstMat = new Mat(3,1,CvType.CV_32FC1);

        p[0] = x;
        p[1] = y;
        p[2] = 1;

        mat2.put(0,0,p);
        Core.gemm(mat,mat2,1,mat,0,dstMat);

        return dstMat;
    }
///////////////////OTHER/////////////////////////

}

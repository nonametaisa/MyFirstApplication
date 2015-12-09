package com.example.yusaku.firstmyapplication;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.highgui.Highgui;


public class MainActivity extends ActionBarActivity {
    private int RESULT_PICK_FILENAME = 1;

    private ImageView mMainImageView , mSubImageView , mLaneImageView;
    private Button mSelectButton , mActButton , mFinishButton, mNextButton , mPlusButton , mMinusButton ;
    private Bitmap mSrcBitmap , mDstBitmap , mBallBitmap ;
    private int mFlag = 0;
    private MediaMetadataRetriever mMediaMetadataRetriever;
    private Mat mSrcMat , mDstMat;
    private int mIntSelectCircle=0;
    private int mVideoTime , mTimeToPicture = 1;
    private Context mContext;
    private TextView mTextView;

    private OpneCVUse opencvuse ;
    private Point mPoint1 , mPoint2;

    private double mBallPosition[][];
    private double mLinePosition[][];
    private int mBallCount = 0;
    int videoCapTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMainImageView = (ImageView)findViewById(R.id.myMainImageVew);
        mSubImageView = (ImageView)findViewById(R.id.mySubImageView);
        mLaneImageView = (ImageView)findViewById(R.id.laneImageView);
        mSelectButton = (Button)findViewById(R.id.selectButton);
        mActButton = (Button)findViewById(R.id.actButton);
        mFinishButton = (Button)findViewById(R.id.cancelButton);
        mNextButton = (Button)findViewById(R.id.nextButton);
        mMinusButton = (Button)findViewById(R.id.minusButton);
        mPlusButton = (Button)findViewById(R.id.plusButton);
        mTextView = (TextView)findViewById(R.id.textView);

        mBallPosition = new double[2][20]; //x,y //point
        mLinePosition = new double[4][20]; //left:x left:y,right:x right:y // point




        opencvuse = new OpneCVUse();

        mContext = this;

        mSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pickFilenameFromGallery();
                mFlag = 1;

            }
        });

        mActButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mFlag == 1){

                    Toast.makeText(mContext,String.valueOf(mVideoTime) , Toast.LENGTH_SHORT).show();

                   // setImage(mVideoTime / 500);
                  //  mTimeToPicture = mVideoTime / 200;

                 //   Toast.makeText(mContext, String.valueOf(mTimeToPicture),Toast.LENGTH_SHORT).show();
                 //   videoCapTime =mTimeToPicture;
                    setImage(videoCapTime);
                   // mMainImageView.setImageBitmap(mSrcBitmap);

             //      mDstBitmap =opencvuse.setCircleMat(mSrcBitmap);
                    mMainImageView.setImageBitmap(opencvuse.setCircleMat(mSrcBitmap));

                    mFlag =2;

                }else if (mFlag == 3){
                    setImage(0); //コメントアウト１が実行できるときに使用

                    mMainImageView.setImageBitmap(opencvuse.getLineImage(mSrcBitmap));
                    mFlag =4;
                    setmBallPosition(mBallCount);
                    mBallCount ++;
                }else if(mFlag == 4){
             //       setImage(videoCapTime);
             //       mMainImageView.setImageBitmap(mSrcBitmap);


                    setImage(videoCapTime ); //コメントアウト１が実行可能のとき　また最後のsetImageを消す

                    mMainImageView.setImageBitmap(opencvuse.tenplateMatch(opencvuse.getMat(mSrcBitmap), opencvuse.getMat(mBallBitmap)));
                    mBallBitmap =opencvuse.getMinusBallImage(mSrcBitmap);
                    mPoint1 =opencvuse.getPlusIntersection();
                    mPoint2 = opencvuse.getMinusIntersection();
                    Bitmap b =drawBitmap(mSrcBitmap,mPoint1);
                    b =drawBitmap(mSrcBitmap,mPoint2);
                    mSubImageView.setImageBitmap(mBallBitmap);
                    mTextView.setText(String.valueOf(opencvuse.pMinMaxResult.maxVal));
                    videoCapTime += 4;
                    setmBallPosition(mBallCount);
                    mBallCount ++;

//                    setImage(videoCapTime +=4);

                }
                else {
                    Toast.makeText(mContext ,"先にSelectButtonを押してください",Toast.LENGTH_SHORT).show();
                }

            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFlag == 2 || mFlag == 3) {
                    mFlag =3;
                    mBallBitmap = opencvuse.getCirclePosition(mIntSelectCircle,mSrcBitmap);
                    mSubImageView.setImageBitmap(mBallBitmap);
                    if (opencvuse.mCirclMat.cols() <= mIntSelectCircle + 1) {
                        mIntSelectCircle = 0;
                    } else {
                        mIntSelectCircle++;
                    }

                }else if(mFlag ==0){
                    Toast.makeText(mContext,"先にSelectButtonを押してください",Toast.LENGTH_SHORT).show();
                }else if (mFlag ==1){
                    Toast.makeText(mContext,"先にActButtonを押してください" , Toast.LENGTH_SHORT).show();
                }
            }
        });

      mFinishButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Resources  r = getResources();
               Bitmap bitmap = BitmapFactory.decodeResource(r,R.drawable.my_lane);
//               testhomo(bitmap);
               if (mFlag == 4) {
                   mMainImageView.setImageBitmap(null);
                   mSubImageView.setImageBitmap(null);
                  // bitmap = homography(bitmap);

                   mLaneImageView.setImageBitmap(bitmap);
               }




           }
       });

        mPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFlag ==2){

                    mTimeToPicture += 7;
                    if (mVideoTime > (mTimeToPicture * 200)){
                        setImage(mTimeToPicture);
                        mMainImageView.setImageBitmap(opencvuse.setCircleMat(mSrcBitmap));
                        videoCapTime = mTimeToPicture;
                    }else {
                        Toast.makeText(mContext,"最後のフレームです",Toast.LENGTH_SHORT).show();
                        setImage(mVideoTime);
                        mMainImageView.setImageBitmap(opencvuse.setCircleMat(mSrcBitmap));
                        videoCapTime = mVideoTime;

                    }

                }
            }
        });

        mMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFlag ==2){

                    mTimeToPicture -= 4;
                    if (mTimeToPicture >0) {
                        setImage(mTimeToPicture);
                        mMainImageView.setImageBitmap(opencvuse.setCircleMat(mSrcBitmap));
                        videoCapTime = mTimeToPicture;

                    }else {
                        Toast.makeText(mContext,"0秒のフレームです",Toast.LENGTH_SHORT).show();
                        setImage(0);
                        mMainImageView.setImageBitmap(opencvuse.setCircleMat(mSrcBitmap));
                        videoCapTime = 0;
                        mTimeToPicture = 0;
                    }
                }
            }
        });
    }

    private void setmBallPosition(int count){
        mBallPosition[0][count] = opencvuse.mCirclCenterPosition.x;
        mBallPosition[1][count] = opencvuse.mCirclCenterPosition.y;
        mLinePosition[0][count] = opencvuse.getMinusIntersection().x;
        mLinePosition[1][count] = opencvuse.getMinusIntersection().y;
        mLinePosition[2][count] = opencvuse.getPlusIntersection().x;
        mLinePosition[3][count] = opencvuse.getPlusIntersection().y;
        Log.e("count =" ,String.valueOf(count));

        Log.e("setBall[0][count] =" , String.valueOf(mBallPosition[0][count]) + "[1][count]=" +
                String.valueOf(mBallPosition[1][count]) + "mLinePosition is=" );


    }

    private Mat matrixLog(Bitmap bitmap){
       // float dataA[][] ={{0,bitmap.getWidth(),bitmap.getWidth(),0},{0,0,bitmap.getHeight(),bitmap.getHeight()}};
       // float dataB[][] = new float[4][2];
        double dataA[] = {0,bitmap.getWidth() , bitmap.getWidth() , 0 , 0 , 0, bitmap.getHeight() , bitmap.getHeight()};
        double dataB[] ={10,bitmap.getWidth()  +10, bitmap.getWidth() , 0 , 0 , 0, bitmap.getHeight() , bitmap.getHeight()} ;
        /*
        mBallCount  = mBallCount -1;
        dataB[0] = mLinePosition[2][mBallCount];
        dataB[1] = mLinePosition[0][mBallCount];
        dataB[2] = mLinePosition[0][0];
        dataB[3] = mLinePosition[2][0];
        dataB[4] = mLinePosition[3][mBallCount];
        dataB[5] = mLinePosition[1][mBallCount];
        dataB[6] = mLinePosition[1][0];
        dataB[7] = mLinePosition[3][0];


        Mat matA = new Mat(4,2,CvType.CV_32FC1);
        Mat matB = new Mat(4,2,CvType.CV_32FC1);
        matA.put(0,0,dataA);
        matB.put(0,0,dataB);
*/
        MatOfPoint2f matOfPoint2fA = new MatOfPoint2f();
        MatOfPoint2f matOfPoint2fB = new MatOfPoint2f();
        matOfPoint2fA.create(4,2,CvType.CV_32FC1);
        matOfPoint2fB.create(4,2,CvType.CV_32FC1);

        double dataC[] = new double[8];
        double dataD[] = new double[8];
         for (int i = 1 ; (i-1) < dataC.length ; i ++){
            dataC[i-1] = i;
            dataD[i-1] = i + 3;
        }
        matOfPoint2fA.put(0,0,dataA);
        matOfPoint2fB.put(0,0,dataB);
        Mat matX = new Mat(3,3,CvType.CV_32FC1);

     //   Core.gemm(matOfPoint2fA,matOfPoint2fB,1,matOfPoint2fA,0,matX);

        matX = Calib3d.findHomography(matOfPoint2fA,matOfPoint2fB);
        Log.e("matC = ", matOfPoint2fA.dump());
        Log.e("matD =" , matOfPoint2fB.dump());
        Log.e("matX =",matX.dump());

      //  return matX;
        return new Mat();

    }

    private void testhomo(Bitmap bitmap){
        double dbefore[]	= {.0, 640.0, 640.0, .0, .0, .0, 480.0, 480.0, 1.0, 1.0, 1.0, 1.0};
        double dafter[]	= {640.0, 640.0, .0, .0, .0, 480.0, 480.0, .0, 1.0, 1.0, 1.0, 1.0};


        Mat _before	=new Mat(3, 4, CvType.CV_64FC1);	// 変換前の座標用変数(実体)
        Mat _after	=new Mat(3, 4, CvType.CV_64FC1);	// 変換後の座標用変数(実体)

        _after.put(0, 0, dafter);
        _before.put(0,0,dbefore);

        MatOfPoint2f pBefore	= new MatOfPoint2f();				// ポインタを格納
        MatOfPoint2f pAfter	= new MatOfPoint2f();				// ポインタを格納

        pBefore.put(0,0,dbefore);
        pAfter.put(0,0,dafter);

            Mat pHomography
                    = new Mat(3,3, CvType.CV_64FC1);		//ホモグラフィ用領域を確保

            Calib3d.findHomography(pBefore, pAfter,Calib3d.RANSAC,10,pHomography);


            // ホモグラフィを計算．CV_RANSACで，誤対応を除去
        Mat result = pBefore.clone();

        Core.gemm(pBefore,pAfter,1,pBefore,0,result);


        }


    private Bitmap homography(Bitmap bitmap){

        mBallCount  = mBallCount -1;

        double [][] position = opencvuse.createRectangleToArbitraryQuadrangleTransform(
                bitmap.getWidth(),bitmap.getHeight(),
                mLinePosition[2][mBallCount], mLinePosition[3][mBallCount],
                mLinePosition[0][mBallCount], mLinePosition[1][mBallCount],
                mLinePosition[2][0],          mLinePosition[3][0],
                mLinePosition[0][0],          mLinePosition[1][0]
                );

        Log.e("[2][mBall] =" , String.valueOf(mLinePosition[2][mBallCount]) );
        Log.e("[3][mBall] =" , String.valueOf(mLinePosition[3][mBallCount]) );
        Log.e("[0][mBall] =" , String.valueOf(mLinePosition[0][mBallCount]) );
        Log.e("[1][mBall] =" , String.valueOf(mLinePosition[1][mBallCount]) );
        Log.e("[2][0] =" , String.valueOf(mLinePosition[2][0]) );
        Log.e("[3][0] =" , String.valueOf(mLinePosition[3][0]) );
        Log.e("[0][0] =" , String.valueOf(mLinePosition[0][0]) );
        Log.e("[1][0] =" , String.valueOf(mLinePosition[1][0]) );


        Mat mat = new Mat(3,3,CvType.CV_32FC1);
        Mat dst;

        double [] p = new double[9];
        double a = position[0][0];
        double b = position[0][1];
        double c = position[0][2];
        double d = position[1][0];
        double e = position[1][1];
        double f = position[1][2];
        double g = position[2][0];
        double h = position[2][1];
      //  p[8] = position[2][2];


        mat.put(0,0,p);


        Log.e("position is" ,  mat.dump());

        double[][] mytruePosition = new double[2][mBallCount];
        double[] tmp;
        Point point = new Point();

        Log.e("a =" , String.valueOf(a));
        Log.e("[0][0] =" ,String.valueOf(position[0][0]));
        Log.e("b =" , String.valueOf(b));
        Log.e("[0][1] =" ,String.valueOf(position[0][1]));
        Log.e("c =" , String.valueOf(c));
        Log.e("d =" , String.valueOf(d));
        Log.e("e =" , String.valueOf(e));
        Log.e("d =" , String.valueOf(f));
        Log.e("e =" , String.valueOf(g));
        Log.e("f =" , String.valueOf(h));

        for (int i = 0 ; i < mBallCount ; i++) {
            point.x = (mBallPosition[0][i]*a + mBallPosition[1][i]*b + c) / (mBallPosition[0][i]*g + mBallPosition[1][i]*h + 1);
            point.y = (mBallPosition[0][i]*d + mBallPosition[1][i]*e + f) / (mBallPosition[0][i]*g + mBallPosition[1][i]*h + 1);

            //    dst = opencvuse.myMatrix(bitmap, mat, mBallPosition[0][i], mBallPosition[1][i]);
        //    Log.e("Last" , dst.dump());
        //    tmp =dst.get(0,0);
          //  mytruePosition[0][i] = tmp[0];
          //  mytruePosition[1][i] = tmp[1];
          //  point.x = tmp[0];
          //  point.y = tmp[1];
            Log.e("mP[0]["+String.valueOf(i) +"]",String.valueOf(mBallPosition[0][i]) +" mp[1] = " + String.valueOf(mBallPosition[1][i]));
            Log.e("Last x =",String.valueOf(point.x) +" y = " + String.valueOf(point.y));
            //    drawBitmap(bitmap,point);
        }


        Log.e("w =" +String.valueOf(bitmap.getWidth()) , " h =" + String.valueOf(bitmap.getHeight()));

        return bitmap;


    }


    private void pickFilenameFromGallery() {
        Intent i = new Intent( Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_PICK_FILENAME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri uri = getVideoUri(data);
        setMediaMetaRetriver(data);
        getTime(uri,getVideoName(uri ));


    }

    private String getVideoName(Uri selectedImage ){

        String[] filePathColumn = {MediaStore.Video.Media.DATA};
        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

        String moviePath = cursor.getString(columnIndex);
        cursor.close();

        return moviePath;

    }

    private Uri getVideoUri(Intent data){
        return data.getData();
    }


    private void setMediaMetaRetriver(Intent data){
        //MMRインスタンスに動画データをセットします(Intent#getDataで動画のURIを取得している)
        //getFrameAtTimeで、任意の時間にあたるフレーム画像を取得できるんだなこれが(単位はμs)

        mMediaMetadataRetriever = new MediaMetadataRetriever();
        mMediaMetadataRetriever.setDataSource(mContext, data.getData());

    }

    private void getTime(Uri ur ,String path){
        MediaPlayer mediaPlayer = MediaPlayer.create(this, ur.parse(path));

        mVideoTime = mediaPlayer.getDuration();
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }


    }
// time は4ずつ なぜか0.4秒ごとにしか画像を取得してくれなかった
    private void setImage(int time){

            mSrcBitmap = mMediaMetadataRetriever.getFrameAtTime(1000 * 100 * time );

        }

    private Bitmap drawBitmap(Bitmap src , Point point){

        Canvas canvas;
        canvas = new Canvas(src);

        Paint paint;
        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(10);
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);
        canvas.drawCircle((float)point.x ,(float) point.y , 5 , paint);

        return src;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //OpenCVがAndroid端末内に入っているかのチェック

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OK", "OpenCV loaded successfully");
                }
                break;
                default:
                {
                    super.onManagerConnected(status);
                }
                break;
            }
        }};

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }



    //　ここまでチェック
}

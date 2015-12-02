package com.example.yusaku.firstmyapplication;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import org.opencv.core.Mat;


public class MainActivity extends ActionBarActivity {
    private int RESULT_PICK_FILENAME = 1;

    private ImageView mMainImageView , mSubImageView;
    private Button mSelectButton , mActButton , mCancelButton , mNextButton , mPlusButton , mMinusButton ;
    private Bitmap mSrcBitmap , mDstBitmap , mBallBitmap ;
    private int mFlag = 0;
    private MediaMetadataRetriever mMediaMetadataRetriever;
    private Mat mSrcMat , mDstMat;
    private int mIntSelectCircle=0;
    private int mVideoTime , mTimeToPicture;
    private Context mContext;
    private TextView mTextView;

    private OpneCVUse opencvuse ;
    int videoCapTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMainImageView = (ImageView)findViewById(R.id.myMainImageVew);
        mSubImageView = (ImageView)findViewById(R.id.mySubImageView);
        mSelectButton = (Button)findViewById(R.id.selectButton);
        mActButton = (Button)findViewById(R.id.actButton);
        mCancelButton = (Button)findViewById(R.id.cancelButton);
        mNextButton = (Button)findViewById(R.id.nextButton);
        mMinusButton = (Button)findViewById(R.id.minusButton);
        mPlusButton = (Button)findViewById(R.id.plusButton);
        mTextView = (TextView)findViewById(R.id.textView);

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
                    mTimeToPicture = mVideoTime / 200;

                 //   Toast.makeText(mContext, String.valueOf(mTimeToPicture),Toast.LENGTH_SHORT).show();
                 //   videoCapTime =mTimeToPicture;
                    setImage(videoCapTime);
                   // mMainImageView.setImageBitmap(mSrcBitmap);

             //      mDstBitmap =opencvuse.setCircleMat(mSrcBitmap);
                    mMainImageView.setImageBitmap(opencvuse.setCircleMat(mSrcBitmap));

                    mFlag =2;

                }else if (mFlag == 3){

                    mMainImageView.setImageBitmap(opencvuse.getLineImage(mSrcBitmap));
                    mFlag =4;

                }else if(mFlag == 4){
             //       setImage(videoCapTime);
             //       mMainImageView.setImageBitmap(mSrcBitmap);
                    setImage(videoCapTime +=4);
                    mMainImageView.setImageBitmap(opencvuse.tenplateMatch(opencvuse.getMat(mSrcBitmap), opencvuse.getMat(mBallBitmap)));
                    mBallBitmap =opencvuse.getMinusBallImage(mSrcBitmap);
                    mSubImageView.setImageBitmap(mBallBitmap);
                    Log.e("通ったよ", "");
                    mTextView.setText(String.valueOf(opencvuse.pMinMaxResult.maxVal));

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

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFlag ==2){

                    mTimeToPicture += 5;
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

                    mTimeToPicture -= 5;
                    if (mTimeToPicture >0) {
                        setImage(mTimeToPicture);
                        mMainImageView.setImageBitmap(opencvuse.setCircleMat(mSrcBitmap));
                        videoCapTime = mTimeToPicture;
                    }else {
                        Toast.makeText(mContext,"0秒のフレームです",Toast.LENGTH_SHORT).show();
                        setImage(0);
                        mMainImageView.setImageBitmap(opencvuse.setCircleMat(mSrcBitmap));
                        videoCapTime = 0;
                    }
                }
            }
        });
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

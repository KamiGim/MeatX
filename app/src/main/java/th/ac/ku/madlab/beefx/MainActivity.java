package th.ac.ku.madlab.beefx;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Blob;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    ProgressBar bar;
    RatingBar rating;
    ArcProgress fatProgress;
    TextView txtSFat;
    TextView txtMFat;
    TextView txtLFat;
    TextView txtAvgDis;
    ScrollView scrollView;
    Button buMoreInfo;
    LinearLayout LLInfo;

    View rootView;

    double[] yData = {25.3f, 10.6f, 66.76f};
    private String[] xData = {"Small", "Medium" , "Large"};
    PieChart pieChart;

    DBManager dbManager;

    Bitmap img;
    Bitmap imgResult;
    Bitmap bmpSmall;
    Bitmap bmpMed;
    Bitmap bmpLarge;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    new Mat();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public static double round(double value, int scale) {
        return Math.round(value * Math.pow(10, scale)) / Math.pow(10, scale);
    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    private Uri getImageUri(Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public void bitmapIntoImageView(ImageView imageView, Bitmap bitmap){
        Uri imageUri = getImageUri(bitmap);
        Picasso.with(this).load(imageUri).into(imageView);
    }

    public void findElement(){
        pieChart = (PieChart) findViewById(R.id.idPieChart);
        imageView = (ImageView) findViewById(R.id.imageViewPreview);
        bar = (ProgressBar)findViewById(R.id.progressBar);
        bar.setVisibility(View.INVISIBLE);
        rating = (RatingBar) findViewById(R.id.ratingBar);
        fatProgress = (ArcProgress) findViewById(R.id.arc_progress);
        txtSFat = (TextView) findViewById(R.id.tvSFat);
        txtMFat = (TextView) findViewById(R.id.tvMFat);
        txtLFat = (TextView) findViewById(R.id.tvLFat);
        txtAvgDis = (TextView) findViewById(R.id.tvAvgDis);
        scrollView = (ScrollView) findViewById(R.id.scrollAll);
        rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        buMoreInfo = (Button) findViewById(R.id.buMoreInfo);
        LLInfo = (LinearLayout) findViewById(R.id.LLInfo);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

//        byte[] byteArray = getIntent().getByteArrayExtra("imageWithCrop");
//        Bitmap img = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        img = null;
        Bitmap imgOri = null;
        String filenameRedMask = getIntent().getStringExtra("imgRedMask");
        String filenameOri  = getIntent().getStringExtra("imgOri");
        try {
            FileInputStream is1 = this.openFileInput(filenameRedMask);
            img = BitmapFactory.decodeStream(is1);
            is1.close();
            FileInputStream is2 = this.openFileInput(filenameOri);
            imgOri = BitmapFactory.decodeStream(is2);
            is2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        findElement();
        LLInfo.setVisibility(View.GONE);

        String latitude = getIntent().getStringExtra("lat");
        String longtitude  = getIntent().getStringExtra("long");
        if (latitude==null){latitude="0.0";}
        if (longtitude==null){longtitude="0.0";}
        TelephonyManager telephonyManager;
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = telephonyManager.getDeviceId();

        Bitmap bmpSmall = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);
        Bitmap bmpMed = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);
        Bitmap bmpLarge = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);

        final ImgProcessing imgPro = new ImgProcessing(img,imgOri,bmpSmall,bmpMed,bmpLarge);
        imgPro.Process2();
        Statistics stat = new Statistics(imgPro.xpos,imgPro.ypos);
        double sdX = round(stat.getStdDevX(),2);
        double sdY = round(stat.getStdDevY(),2);
        double avgDistance = round(stat.getAvgDistance(),2);
        Statistics stat_s = new Statistics(imgPro.xpos_s,imgPro.ypos_s);
        Statistics stat_m = new Statistics(imgPro.xpos_m,imgPro.ypos_m);
        Statistics stat_l = new Statistics(imgPro.xpos_l,imgPro.ypos_l);
        double sdX_s = round(stat_s.getStdDevX(),2);
        double sdY_s = round(stat_s.getStdDevY(),2);
        double avgDistance_S = round(stat_s.getAvgDistance(),2);
        double sdX_m = round(stat_m.getStdDevX(),2);
        double sdY_m = round(stat_m.getStdDevY(),2);
        double avgDistance_m = round(stat_m.getAvgDistance(),2);
        double sdX_l = round(stat_l.getStdDevX(),2);
        double sdY_l = round(stat_l.getStdDevY(),2);
        double avgDistance_l = round(stat_l.getAvgDistance(),2);

        Log.d("Statistics",Double.toString(sdX_s)+','+Double.toString(sdY_s)+','+Double.toString(avgDistance_S)+','+
                Double.toString(sdX_m)+','+Double.toString(sdY_m)+','+Double.toString(avgDistance_m)+','+
                Double.toString(sdX_l)+','+Double.toString(sdY_l)+','+Double.toString(avgDistance_l));

        TextView tvSDX = (TextView) findViewById(R.id.tvSDX);
        TextView tvSDY = (TextView) findViewById(R.id.tvSDY);
        tvSDX.setText(Double.toString(sdX));
        tvSDY.setText(Double.toString(sdY));
        txtAvgDis.setText(Double.toString(avgDistance));
        int countSmall = imgPro.countSmall;
        int countMedium = imgPro.countMedium;
        int countLarge = imgPro.countLarge;
        txtSFat.setText(Integer.toString(countSmall));
        txtMFat.setText(Integer.toString(countMedium));
        txtLFat.setText(Integer.toString(countLarge));
        double fatPercent = round(imgPro.fatPercent,2);
        fatProgress.setProgress((int)fatPercent);
        imgResult = imgPro.result;

        imageView.setImageBitmap(imgResult);
        double sum = imgPro.areaSmall + imgPro.areaMedium + imgPro.areaLarge;

        double areaSmall = round(imgPro.areaSmall/sum*100,2);
        double areaMedium = round(imgPro.areaMedium/sum*100,2);
        double areaLarge = round(imgPro.areaLarge/sum*100,2);

        yData = new double[]{areaSmall,areaMedium , areaLarge};
        pieChartSetting(pieChart,yData,bmpSmall,bmpMed,bmpLarge);

//        NeuronNetwork NN = new NeuronNetwork(fatPercent,countSmall,countMedium,countLarge,avgDistance,sdX,sdY,areaSmall,areaMedium,areaLarge,avgDistance_S,avgDistance_m,avgDistance_l);
        double[] dataNN = {fatPercent,avgDistance,sdX,sdY,avgDistance_S,avgDistance_m,avgDistance_l,areaSmall,areaMedium,areaLarge};
        NeuralNetwork NN1 = new NeuralNetwork(dataNN);
        double[] grades = NN1.getGrades();
        for (int i=0;i <grades.length;i++){
            Log.d("Grade",Double.toString(grades[i]));
        }
        int maxIndex = 0;
        double max = 0;
        for (int i = 0; i < grades.length; i++) {
            if (grades[i] > max) {
                max = grades[i];
                maxIndex = i;
            }
        }

        double[] gradesAll = {2,3,3.5,4,4.5,5};
        double gradeResult = gradesAll[maxIndex];
        rating.setRating((float)gradeResult);


        final Bitmap finalImgOri = imgOri;

        imageView.setOnClickListener(new View.OnClickListener()
        {
            private boolean toggle=false;
            @Override
            public void onClick(View v)
            {
                Log.d("toggle",Boolean.toString(toggle));
                if(toggle)
                {
//                    imageView.setImageBitmap(imgResult);
                    bitmapIntoImageView(imageView,imgResult);
                    toggle=false;
                }
                else
                {
//                    imageView.setImageBitmap(imgPro.scaled);
                    bitmapIntoImageView(imageView, finalImgOri);
                    toggle=true;
                }
            }
        });

        buMoreInfo.setOnClickListener(new View.OnClickListener()
        {
            private boolean toggle=false;
            @Override
            public void onClick(View v)
            {
                if(toggle)
                {
                    LLInfo.setVisibility(View.GONE);
                    buMoreInfo.setText("ดูข้อมูลเพิ่มเติม");
                    toggle=false;
                }
                else
                {
                    LLInfo.setVisibility(View.VISIBLE);
                    buMoreInfo.setText("ซ่อนข้อมูล");
                    toggle=true;
                }
            }
        });

        RandomName rn = new RandomName();
        String tmpName = rn.randomIdentifier();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.PNG,50,byteArrayOutputStream);
        String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
        ByteArrayOutputStream byteArrayOutputStreamOri = new ByteArrayOutputStream();
        imgOri.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStreamOri);
        ByteArrayOutputStream byteArrayOutputStream_S = new ByteArrayOutputStream();
        bmpSmall.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream_S);
        ByteArrayOutputStream byteArrayOutputStream_M = new ByteArrayOutputStream();
        bmpMed.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream_M);
        ByteArrayOutputStream byteArrayOutputStream_L = new ByteArrayOutputStream();
        bmpLarge.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream_L);

        dbManager = new DBManager(this);
        ContentValues values= new ContentValues();
        values.put("longtitude",longtitude);
        values.put("latitude",latitude);
        values.put("device_id",deviceId);
        values.put("sdX",sdX);
        values.put("sdY",sdY);
        values.put("grade",gradeResult);
        values.put("avgDistance",avgDistance);
        values.put("fatPercent",fatPercent);
        values.put("countSmall",countSmall);
        values.put("countMedium",countMedium);
        values.put("countLarge",countLarge);
        values.put("areaSmall",areaSmall);
        values.put("areaMedium",areaMedium);
        values.put("areaLarge",areaLarge);
        values.put("imgPath",tmpName);
        values.put("img",byteArrayOutputStream.toByteArray());
        values.put("imgOri",byteArrayOutputStreamOri.toByteArray());
        values.put("imgFatSmall",byteArrayOutputStream_S.toByteArray());
        values.put("imgFatMedium",byteArrayOutputStream_M.toByteArray());
        values.put("imgFatLarge",byteArrayOutputStream_L.toByteArray());
        String time = getDateTime();
        values.put("created_at",time );
        boolean isSent = false;

//        String url="http://madlab.cpe.ku.ac.th/supab/tracking.php?log="+  longtitude+"&lat="+
//                latitude+"&device_id="+deviceId+"&sdX="+sdX+"&sdY="+sdY+ "&fatPercent="+imgPro.fatPercent+
//                "&countSmall="+imgPro.countSmall+"&countMedium="+imgPro.countMedium+"&countLarge="+imgPro.countLarge+
//                "&imgPath="+tmpName+"&imgArr="+encodedImage;

        String url="http://madlab.cpe.ku.ac.th/supab/tracking-img.php";

        Log.d("Tracking Url",url);

        if (isNetworkAvailable()){
//            new MyAsyncTaskgetNews().execute(url);
            isSent = true;
            new MyAsyncTaskgetNews().execute(url,longtitude,latitude,deviceId,Double.toString(sdX),
                    Double.toString(sdY),Double.toString(fatPercent),Integer.toString(countSmall),
                    Integer.toString(countMedium),Integer.toString(countLarge),tmpName,encodedImage);

            String[] SelectionsArgs = {"0"};
            Cursor cursor=dbManager.query(null,"status = ? ",SelectionsArgs,"status");
            if (cursor.moveToFirst()){
                do {
                    longtitude = cursor.getString( cursor.getColumnIndex("longtitude"));
                    latitude = cursor.getString( cursor.getColumnIndex("latitude"));
                    deviceId = cursor.getString( cursor.getColumnIndex("device_id"));
                    sdX = cursor.getDouble(cursor.getColumnIndex("sdX"));
                    sdY = cursor.getDouble( cursor.getColumnIndex("sdY"));
                    double grade1 = cursor.getDouble( cursor.getColumnIndex("grade"));
                    avgDistance = cursor.getDouble( cursor.getColumnIndex("avgDistance"));
                    fatPercent = cursor.getDouble( cursor.getColumnIndex("fatPercent"));
                    countSmall = cursor.getInt( cursor.getColumnIndex("countSmall"));
                    countMedium = cursor.getInt( cursor.getColumnIndex("countMedium"));
                    countLarge = cursor.getInt( cursor.getColumnIndex("countLarge"));
                    areaSmall = cursor.getDouble( cursor.getColumnIndex("areaSmall"));
                    areaMedium = cursor.getDouble( cursor.getColumnIndex("areaMedium"));
                    areaLarge = cursor.getDouble( cursor.getColumnIndex("areaLarge"));
                    tmpName = cursor.getString( cursor.getColumnIndex("imgPath"));
                    byte[] imgBlob = cursor.getBlob(cursor.getColumnIndex("img"));
                    byte[] imgBlobOri = cursor.getBlob(cursor.getColumnIndex("imgOri"));
                    byte[] imgBlobS = cursor.getBlob(cursor.getColumnIndex("imgFatSmall"));
                    byte[] imgBlobM = cursor.getBlob(cursor.getColumnIndex("imgFatMedium"));
                    byte[] imgBlobL = cursor.getBlob(cursor.getColumnIndex("imgFatLarge"));
                    time = cursor.getString( cursor.getColumnIndex("created_at"));
                    encodedImage = Base64.encodeToString(imgBlob, Base64.DEFAULT);

                    ContentValues values1= new ContentValues();
                    values1.put("longtitude",longtitude);
                    values1.put("latitude",latitude);
                    values1.put("device_id",deviceId);
                    values1.put("sdX",sdX);
                    values1.put("sdY",sdY);
                    values1.put("grade",grade1);
                    values1.put("avgDistance",avgDistance);
                    values1.put("fatPercent",fatPercent);
                    values1.put("countSmall",countSmall);
                    values1.put("countMedium",countMedium);
                    values1.put("countLarge",countLarge);
                    values1.put("areaSmall",areaSmall);
                    values1.put("areaMedium",areaMedium);
                    values1.put("areaLarge",areaLarge);
                    values1.put("imgPath",tmpName);
                    values1.put("img",imgBlob);
                    values1.put("imgOri",imgBlobOri);
                    values1.put("imgFatSmall",imgBlobS);
                    values1.put("imgFatMedium",imgBlobM);
                    values1.put("imgFatLarge",imgBlobL);
                    values1.put("status","1");
                    values1.put("created_at", time);
                    String[] SelectionArgs={String.valueOf(cursor.getString(cursor.getColumnIndex("ID")))};

                    long id= dbManager.Update(values1,"ID=?",SelectionArgs);
//                    if (id>0)
//                        Toast.makeText(getApplicationContext(),"Data is updated and user id:" +cursor.getString(cursor.getColumnIndex("ID")),Toast.LENGTH_LONG).show();
//                    else
//                        Toast.makeText(getApplicationContext(),"cannot update",Toast.LENGTH_LONG).show();

                    new MyAsyncTaskgetNews().execute(url,longtitude,latitude,deviceId,Double.toString(sdX),
                            Double.toString(sdY),Double.toString(fatPercent),Integer.toString(countSmall),
                            Integer.toString(countMedium),Integer.toString(countLarge),tmpName,encodedImage);

                }while (cursor.moveToNext());
//                Toast.makeText(getApplicationContext(),"uploaded successfully",Toast.LENGTH_LONG).show();
            }
        }

        values.put("status",isSent);
        long id= dbManager.Insert(values);
//        if (id>0)
//            Toast.makeText(getApplicationContext(),"Data is added and user id:"+id,Toast.LENGTH_LONG).show();
//        else
//            Toast.makeText(getApplicationContext(),"cannot insert",Toast.LENGTH_LONG).show();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buTakePic(view);
            }
        });

        FloatingActionButton fabGal = (FloatingActionButton) findViewById(R.id.fabGal);
        fabGal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buPickGal(view);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.headerShare:
                View screenView = this.rootView;
                screenView.setDrawingCacheEnabled(true);
                Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
                screenView.setDrawingCacheEnabled(false);
                Uri bmpUri =  getImageUri(bitmap);
                final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/jpg");
                shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                startActivity(Intent.createChooser(shareIntent, "Share image using"));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main1, menu);
        return true;
    }



    private void pieChartSetting(PieChart pieChart, final double[] yData, final Bitmap bmpSmall, final Bitmap bmpMedium, final Bitmap bmpLarge){
        pieChart.setRotationEnabled(true);
        pieChart.setDescription("สัดส่วนไขมันแต่ละขนาด");
        pieChart.setDescriptionPosition(270,270);
        pieChart.setHoleRadius(25f);
        pieChart.setTransparentCircleAlpha(0);
        pieChart.setCenterText("Area");
        pieChart.setCenterTextSize(10);
        addDataSet(yData);

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {

                Log.d("onPie",e.toString());
                int pos1 = e.toString().indexOf("(sum): ");
                String sales = e.toString().substring(pos1 + 7);

                for(int i = 0; i < yData.length; i++){
                    if(yData[i] == Double.parseDouble(sales)){
                        pos1 = i;
                        break;
                    }
                }

                String size = xData[pos1];
                if (size == "Small"){
                    bitmapIntoImageView(imageView,bmpSmall);
                }
                else if(size == "Medium"){
                    bitmapIntoImageView(imageView,bmpMedium);
                }
                else if(size == "Large"){
                    bitmapIntoImageView(imageView,bmpLarge);
                }
                Toast.makeText(MainActivity.this, size + " Fat" + "\n" + "Areas: " + sales +"%", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    private void addDataSet(double[] yData) {
        ArrayList<PieEntry> yEntrys = new ArrayList<>();
        ArrayList<String> xEntrys = new ArrayList<>();

        for(int i = 0; i < yData.length; i++){
            yEntrys.add(new PieEntry((float)yData[i] , i));
        }

        for(int i = 1; i < xData.length; i++){
            xEntrys.add(xData[i]);
        }

        //create the data set
        PieDataSet pieDataSet = new PieDataSet(yEntrys, "Employee Sales");
        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(12);

        //add colors to dataset
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.rgb(0, 255, 255));
        colors.add(Color.rgb(0, 0, 255));
        colors.add(Color.rgb(255, 0, 255));

        pieDataSet.setColors(colors);

        //create pie data object
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    int tag = 1;
    int TAKE_PICTURE = 1;

    public void buTakePic(View view) {
        CheckUserPermissions();
    }

    void CheckUserPermissions(){
        if ( Build.VERSION.SDK_INT >= 23){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED  ){
                requestPermissions(new String[]{
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE_ASK_PERMISSIONS);
                return ;
            }
        }

        TakePicture();// init the contact list

    }

    //get access to location permission
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    TakePicture();// init the contact list
                } else {
                    // Permission Denied
                    Toast.makeText( this,"cannot store file" , Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private final static int SELECT_PHOTO = 12345;

    public void buPickGal(View view) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, SELECT_PHOTO);
    }

    void TakePicture(){
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,tag);
    }

    Bitmap img2;
    Bitmap scaled;
    Bitmap imgHalf;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK){
            Bundle b = data.getExtras();
            img2 = (Bitmap) b.get("data");

            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                img2.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                //Cleanup
                stream.close();
                img2.recycle();

                Intent intent = new Intent(this, DrawingActivity.class);
                intent.putExtra("img", byteArray);
                startActivity(intent);

            } catch (Exception e) {
                e.printStackTrace();
            }

//            Intent intent = new Intent(this, MainActivity.class);
//            intent.putExtra("img", img);
//            startActivity(intent);

        }
        if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK && data != null) {
            // Let's read picked image data - its URI
            Uri pickedImage = data.getData();
            // Let's read picked image path using content resolver
            String[] filePath = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
            cursor.moveToFirst();
            String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap imgFull = BitmapFactory.decodeFile(imagePath, options);

            int nh = (int) ( imgFull.getHeight() * (512.0 / imgFull.getWidth()) );
            img2 = Bitmap.createScaledBitmap(imgFull, 512, nh, true);

            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                img2.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                //Cleanup
                stream.close();
                img2.recycle();
                imgFull.recycle();

                Intent intent = new Intent(this, DrawingActivity.class);
                intent.putExtra("img", byteArray);
                startActivity(intent);

            } catch (Exception e) {
                e.printStackTrace();
            }

//            Intent intent = new Intent(this, MainActivity.class);
//            intent.putExtra("img", img);
//            startActivity(intent);

            cursor.close();
        }
    }

    int counter = 0;
    public void previewImg(View view) {

        imageView.setImageBitmap(img);
//        if (counter%2 == 0) {
//            imageView.setImageBitmap(imgResult);
//        }
//        else {
//            imageView.setImageBitmap(img);
//        }
//        counter++;

//        imageView.buildDrawingCache();
//        Bitmap bmap = imageView.getDrawingCache();
//
//        final Dialog nagDialog = new Dialog(MainActivity.this,android.R.style.Theme_Translucent_NoTitleBar);
//        nagDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        nagDialog.setCancelable(false);
//        nagDialog.setContentView(R.layout.dialog_picture);
//        Button btnClose = (Button)nagDialog.findViewById(R.id.btnIvClose);
//        ImageView ivPreview = (ImageView)nagDialog.findViewById(R.id.iv_preview_image);
//        ivPreview.setImageBitmap(bmap);
//
//        btnClose.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View arg0) {
//                nagDialog.dismiss();
//            }
//        });
//        nagDialog.show();
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    // get news from server
    public class MyAsyncTaskgetNews extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            //before works
        }
        @Override
        protected String  doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                String NewsData;
                //define the url we have to connect with
                URL url = new URL(params[0]);
                //make connect with url and send request
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                //waiting for 7000ms for response
                urlConnection.setConnectTimeout(7000);//set timeout to 5 seconds
                JSONObject jsonObject;
                jsonObject = new JSONObject();
                jsonObject.put("longitude", params[1]);
                jsonObject.put("latitude", params[2]);
                jsonObject.put("device_id", params[3]);
                jsonObject.put("sdX", params[4]);
                jsonObject.put("sdY", params[5]);
                jsonObject.put("fatPercent", params[6]);
                jsonObject.put("countSmall", params[7]);
                jsonObject.put("countMedium", params[8]);
                jsonObject.put("countLarge", params[9]);
                jsonObject.put("imgPath", params[10]);
                jsonObject.put("imgArr", params[11]);
                String data = jsonObject.toString();
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                urlConnection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
                urlConnection.setFixedLengthStreamingMode(data.getBytes().length);
                urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                writer.write(data);
                Log.d("Tester", "Data to php = " + data);
                writer.flush();
                writer.close();
                out.close();
                urlConnection.connect();

                try {
                    //getting the response data
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    //convert the stream to string
                    NewsData = ConvertInputToStringNoChange(in);
                    //send to display data
                    publishProgress(NewsData);
                } finally {
                    //end connection
                    urlConnection.disconnect();
                }

            }catch (Exception ex){}
            return null;
        }
        protected void onProgressUpdate(String... progress) {

            try {
                JSONObject json= new JSONObject(progress[0]);
                //display response data
                Toast.makeText(getApplicationContext(),json.getString("msg"),Toast.LENGTH_LONG).show();

            } catch (Exception ex) {
            }
        }
        protected void onPostExecute(String  result2){

        }
    }

    // this method convert any stream to string
    public static String ConvertInputToStringNoChange(InputStream inputStream) {
        BufferedReader bureader=new BufferedReader( new InputStreamReader(inputStream));
        String line ;
        String linereultcal="";
        try{
            while((line=bureader.readLine())!=null) {
                linereultcal+=line;
            }
            inputStream.close();
        }catch (Exception ex){}
        return linereultcal;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}

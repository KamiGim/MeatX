package th.ac.ku.madlab.beefx;


import android.Manifest;
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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
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
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    TextView txtAnswer;
    ProgressBar bar;
    RatingBar rating;
    ArcProgress fatProgress;
    TextView txtSFat;
    TextView txtMFat;
    TextView txtLFat;
    ScrollView scrollView;

    View rootView;

    float[] yData = {25.3f, 10.6f, 66.76f};
    private String[] xData = {"Small", "Medium" , "Big"};
    PieChart pieChart;

    DBManager dbManager;

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

        Bitmap img = null;
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

        pieChart = (PieChart) findViewById(R.id.idPieChart);
        pieChartSetting(pieChart);
        txtAnswer = (TextView) findViewById(R.id.textViewSize);
        imageView = (ImageView) findViewById(R.id.imageViewPreview);
        bar = (ProgressBar)findViewById(R.id.progressBar);
        bar.setVisibility(View.INVISIBLE);
        rating = (RatingBar) findViewById(R.id.ratingBar);
        fatProgress = (ArcProgress) findViewById(R.id.arc_progress);
        txtSFat = (TextView) findViewById(R.id.tvSFat);
        txtMFat = (TextView) findViewById(R.id.tvMFat);
        txtLFat = (TextView) findViewById(R.id.tvLFat);
        scrollView = (ScrollView) findViewById(R.id.scrollAll);
        rootView = getWindow().getDecorView().findViewById(android.R.id.content);

        String latitude = getIntent().getStringExtra("lat");
        String longtitude  = getIntent().getStringExtra("long");
        if (latitude==null){latitude="0.0";}
        if (longtitude==null){longtitude="0.0";}
        TelephonyManager telephonyManager;
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = telephonyManager.getDeviceId();

        ImgProcessing imgPro = new ImgProcessing(img,imgOri);
        imgPro.Process1();
        Statistics statX = new Statistics(imgPro.xpos);
        Statistics statY = new Statistics(imgPro.ypos);
        double sdX = round(statX.getStdDev(),2);
        double sdY = round(statY.getStdDev(),2);
        TextView tvSDX = (TextView) findViewById(R.id.tvSDX);
        TextView tvSDY = (TextView) findViewById(R.id.tvSDY);
        tvSDX.setText(Double.toString(sdX));
        tvSDY.setText(Double.toString(sdY));
        txtAnswer.setText(Integer.toString(imgPro.w)+"x"+Integer.toString(imgPro.h));
        txtSFat.setText(Integer.toString(imgPro.countSmall));
        txtMFat.setText(Integer.toString(imgPro.countMedium));
        txtLFat.setText(Integer.toString(imgPro.countLarge));
        fatProgress.setProgress((int)imgPro.fatPercent);
        imageView.setImageBitmap(imgPro.scaled);
        yData = new float[]{(float)imgPro.areaSmall, (float)imgPro.areaMedium, (float)imgPro.areaLarge};
        addDataSet(yData);

        RandomName rn = new RandomName();
        String tmpName = rn.randomIdentifier();
        double fatPercent = round(imgPro.fatPercent,2);
        int countSmall = imgPro.countSmall;
        int countMedium = imgPro.countMedium;
        int countLarge = imgPro.countLarge;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.PNG,20,byteArrayOutputStream);
        String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);

        dbManager = new DBManager(this);
        ContentValues values= new ContentValues();
        values.put("longtitude",longtitude);
        values.put("latitude",latitude);
        values.put("device_id",deviceId);
        values.put("sdX",sdX);
        values.put("sdY",sdY);
        values.put("fatPercent",fatPercent);
        values.put("countSmall",countSmall);
        values.put("countMedium",countMedium);
        values.put("countLarge",countLarge);
        values.put("imgPath",tmpName);
        values.put("img",byteArrayOutputStream.toByteArray());
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
                String tableData="";
                do {

                    tableData+=cursor.getString(cursor.getColumnIndex("ID"))+ ","+
                            cursor.getString( cursor.getColumnIndex("imgPath")) +"::";
                    longtitude = cursor.getString( cursor.getColumnIndex("longtitude"));
                    latitude = cursor.getString( cursor.getColumnIndex("latitude"));
                    deviceId = cursor.getString( cursor.getColumnIndex("device_id"));
                    sdX = cursor.getDouble(cursor.getColumnIndex("sdX"));
                    sdY = cursor.getDouble( cursor.getColumnIndex("sdY"));
                    fatPercent = cursor.getDouble( cursor.getColumnIndex("fatPercent"));
                    countSmall = cursor.getInt( cursor.getColumnIndex("countSmall"));
                    countMedium = cursor.getInt( cursor.getColumnIndex("countMedium"));
                    countLarge = cursor.getInt( cursor.getColumnIndex("countLarge"));
                    tmpName = cursor.getString( cursor.getColumnIndex("imgPath"));
                    byte[] imgBlob = cursor.getBlob(cursor.getColumnIndex("img"));
                    time = cursor.getString( cursor.getColumnIndex("created_at"));
                    encodedImage = Base64.encodeToString(imgBlob, Base64.DEFAULT);

                    values= new ContentValues();
                    values.put("longtitude",longtitude);
                    values.put("latitude",latitude);
                    values.put("device_id",deviceId);
                    values.put("sdX",sdX);
                    values.put("sdY",sdY);
                    values.put("fatPercent",fatPercent);
                    values.put("countSmall",countSmall);
                    values.put("countMedium",countMedium);
                    values.put("countLarge",countLarge);
                    values.put("imgPath",tmpName);
                    values.put("img",byteArrayOutputStream.toByteArray());
                    values.put("status","1");
                    values.put("created_at", time);
                    //values.put(DBManager.ColID,RecordID);
                    String[] SelectionArgs={String.valueOf(cursor.getString(cursor.getColumnIndex("ID")))};
                    dbManager.Update(values,"ID=?",SelectionArgs);

                    long id= dbManager.Update(values,"ID=?",SelectionArgs);
                    if (id>0)
                        Toast.makeText(getApplicationContext(),"Data is updated and user id:"
                                +cursor.getString(cursor.getColumnIndex("ID")),Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(getApplicationContext(),"cannot update",Toast.LENGTH_LONG).show();

                    new MyAsyncTaskgetNews().execute(url,longtitude,latitude,deviceId,Double.toString(sdX),
                            Double.toString(sdY),Double.toString(fatPercent),Integer.toString(countSmall),
                            Integer.toString(countMedium),Integer.toString(countLarge),tmpName,encodedImage);

                }while (cursor.moveToNext());
                Toast.makeText(getApplicationContext(),"uploaded successfully",Toast.LENGTH_LONG).show();
            }
        }

        values.put("status",isSent);
        long id= dbManager.Insert(values);
        if (id>0)
            Toast.makeText(getApplicationContext(),"Data is added and user id:"+id,Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getApplicationContext(),"cannot insert",Toast.LENGTH_LONG).show();

        String[] SelectionsArgs = {"1"};
        Cursor cursor=dbManager.query(null,"status = ? ",SelectionsArgs,"status");
        if (cursor.moveToFirst()){
            String tableData="";
            do {
                tableData+=cursor.getString(cursor.getColumnIndex("ID"))+ ","+
                        cursor.getString( cursor.getColumnIndex("imgPath")) +"::";

            }while (cursor.moveToNext());
            Toast.makeText(getApplicationContext(),tableData,Toast.LENGTH_LONG).show();
        }

//        imageView.setImageBitmap(img);
        //new MainActivity.UploadImage(img,"test").execute();
//        scrollView.setVisibility(View.INVISIBLE);

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

    private void pieChartSetting(PieChart pieChart){
        pieChart.setDescription("Sales by employee (In Thousands $) ");
        pieChart.setRotationEnabled(true);
        pieChart.setDescription("sdfsdfs");
        //pieChart.setUsePercentValues(true);
        //pieChart.setHoleColor(Color.BLUE);
        //pieChart.setCenterTextColor(Color.BLACK);
        pieChart.setHoleRadius(25f);
        pieChart.setTransparentCircleAlpha(0);
        pieChart.setCenterText("Super Cool Chart");
        pieChart.setCenterTextSize(10);
        //pieChart.setDrawEntryLabels(true);
        //pieChart.setEntryLabelTextSize(20);

        addDataSet(yData);

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {

                int pos1 = e.toString().indexOf("(sum): ");
                String sales = e.toString().substring(pos1 + 7);

                for(int i = 0; i < yData.length; i++){
                    if(yData[i] == Float.parseFloat(sales)){
                        pos1 = i;
                        break;
                    }
                }
                String employee = xData[pos1 + 1];
                Toast.makeText(MainActivity.this, "Employee " + employee + "\n" + "Sales: $" + sales + "K", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    private void addDataSet(float[] yData) {
        ArrayList<PieEntry> yEntrys = new ArrayList<>();
        ArrayList<String> xEntrys = new ArrayList<>();

        for(int i = 0; i < yData.length; i++){
            yEntrys.add(new PieEntry(yData[i] , i));
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
        colors.add(Color.GREEN);
        colors.add(Color.BLUE);
        colors.add(Color.RED);

        pieDataSet.setColors(colors);

        //add legend to chart
        Legend legend = pieChart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setPosition(Legend.LegendPosition.LEFT_OF_CHART);

        //create pie data object
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    int tag = 1;

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

    Bitmap img;
    Bitmap scaled;
    Bitmap imgHalf;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == tag && resultCode == RESULT_OK){
            Bundle b = data.getExtras();
            img = (Bitmap) b.get("data");
            int nh = (int) ( img.getHeight() * (512.0 / img.getWidth()) );
            imgHalf = Bitmap.createScaledBitmap(img, 512, nh, true);

            scaled = Bitmap.createScaledBitmap(img, 512, nh, true);
            new MainActivity.UploadImage(img,"test").execute();
            //recycle Bitmap object
//            if(img!=null)
//            {
//                img.recycle();
//                img=null;
//            }

            imageView.setImageBitmap(imgHalf);
//            new MainActivity.UploadImage(img,"test").execute();
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
            img = BitmapFactory.decodeFile(imagePath, options);
            int nh = (int) ( img.getHeight() * (512.0 / img.getWidth()) );
            imgHalf = Bitmap.createScaledBitmap(img, 512, nh, true);
            //scaled
            scaled = Bitmap.createScaledBitmap(img, 512, nh, true);

            Log.d("Path","Path : " + imagePath);
//            Picasso.with(this).load(pickedImage).fit().centerInside().into(imageView);

//            imageView.setImageBitmap(imgHalf);
//            new MainActivity.UploadImage(img,"test").execute();

            //recycle Bitmap object
//            if(img!=null)
//            {
//                img.recycle();
//                img=null;
//            }
//            new MainActivity.UploadImage(img,"test").execute();

            // At the end remember to close the cursor or you will end with the RuntimeException!
            cursor.close();
        }
    }

    public void previewImg(View view) {

        imageView.buildDrawingCache();
        Bitmap bmap = imageView.getDrawingCache();

        final Dialog nagDialog = new Dialog(MainActivity.this,android.R.style.Theme_Translucent_NoTitleBar);
        nagDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        nagDialog.setCancelable(false);
        nagDialog.setContentView(R.layout.dialog_picture);
        Button btnClose = (Button)nagDialog.findViewById(R.id.btnIvClose);
        ImageView ivPreview = (ImageView)nagDialog.findViewById(R.id.iv_preview_image);
//        ivPreview.setImageResource(R.mipmap.ic_launcher);
        ivPreview.setImageBitmap(bmap);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                nagDialog.dismiss();
            }
        });
        nagDialog.show();
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public void buShare(View view) {

        View screenView = view.getRootView();
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);

        Uri bmpUri =  getImageUri(this,bitmap);

        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/jpg");
        shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
        startActivity(Intent.createChooser(shareIntent, "Share image using"));
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

    private class UploadImage extends AsyncTask<Void,Void,Void> {

        Bitmap image;
        String name;
        JSONObject jsonObject;
        String result;
        ByteArrayOutputStream byteArrayOutputStream;

        public UploadImage(Bitmap image,String name){
            super();
            this.image = image;
            this.name = "test";
        }

        @Override
        protected void onPreExecute(){
            bar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {

            byteArrayOutputStream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
            String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);

//            if(img!=null)
//            {
//                img.recycle();
//                img=null;
//            }

            Log.d("Image encode", "Image encode = " + encodedImage);
            try {

                //URL url = new URL("http://supab.net23.net/uploadImg.php");
//                URL url = new URL("http://192.168.1.64/beef/uploadImg.php");
                URL url = new URL("http://madlab.cpe.ku.ac.th/supab/uploadImg.php");

                long time= System.currentTimeMillis();
                Date date = new Date();
                Log.d("Time Class ", " Time value in millisecinds "+time);
                Log.d("Date Class ", " Date "+date);

                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                jsonObject = new JSONObject();
                jsonObject.put("name", name);
                jsonObject.put("image", encodedImage);
                String data = jsonObject.toString();
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
                connection.setFixedLengthStreamingMode(data.getBytes().length);
                connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                OutputStream out = new BufferedOutputStream(connection.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                writer.write(data);
                Log.d("Tester", "Data to php = " + data);
                writer.flush();
                writer.close();
                out.close();
                connection.connect();

                InputStream in = new BufferedInputStream(connection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        in, "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                in.close();
//                final String result = sb.toString();
                result = sb.toString();
                final String[] items = result.split(",");
                Log.d("Tester", "Response from php = " + result);
                Log.d("String", "String = " + items);
                txtAnswer.post(new Runnable() {
                    public void run() {
                        txtAnswer.setText(items[0]);
                        fatProgress.setProgress((int)(Double.parseDouble(items[1])));
                        txtSFat.setText(items[2]);
                        txtMFat.setText(items[3]);
                        txtLFat.setText(items[4]);
                        rating.setRating(Integer.parseInt(items[7]));
                        scrollView.setVisibility(View.VISIBLE);
                    }
                });
                connection.disconnect();
            } catch (Exception e) {
                Log.d("Vicky", "Error Encountered");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            bar.setVisibility(View.GONE);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}

package th.ac.ku.madlab.beefx;


import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import th.ac.ku.madlab.kubeef.R;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    TextView txtAnswer;
    ProgressBar bar;
    RatingBar rating;
    ArcProgress fat;
    TextView txtSFat;
    TextView txtMFat;
    TextView txtLFat;
    TextView txtXLFat;
    ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        ivPreview.setImageResource(R.mipmap.ic_launcher);
        Bundle b = getIntent().getExtras();
        img = (Bitmap) b.get("img");
        setContentView(R.layout.activity_main);
//        DonutProgress fat = (DonutProgress) findViewById(R.id.donut_progress);
//        fat.setProgress(70);
        txtAnswer = (TextView) findViewById(R.id.textViewSize);
        imageView = (ImageView) findViewById(R.id.imageViewPreview);
        bar = (ProgressBar)findViewById(R.id.progressBar);
        bar.setVisibility(View.INVISIBLE);
        rating = (RatingBar) findViewById(R.id.ratingBar);
        fat = (ArcProgress) findViewById(R.id.arc_progress);
        txtSFat = (TextView) findViewById(R.id.tvSFat);
        txtMFat = (TextView) findViewById(R.id.tvMFat);
        txtLFat = (TextView) findViewById(R.id.tvLFat);
        scrollView = (ScrollView) findViewById(R.id.scrollAll);

        imageView.setImageBitmap(img);
        new MainActivity.UploadImage(img,"test").execute();

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
//        Intent photoPickerIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
//                "content://media/internal/images/media"));
//        photoPickerIntent.setType("image/*");
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
            imageView.setImageBitmap(imgHalf);
            new MainActivity.UploadImage(img,"test").execute();
            //recycle Bitmap object
//            if(img!=null)
//            {
//                img.recycle();
//                img=null;
//            }

//            new MainActivity.UploadImage(img,"test").execute();

            // Do something with the bitmap

            // At the end remember to close the cursor or you will end with the RuntimeException!
            cursor.close();
        }
    }

    public void previewImg(View view) {
        final Dialog nagDialog = new Dialog(MainActivity.this,android.R.style.Theme_Translucent_NoTitleBar);
        nagDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        nagDialog.setCancelable(false);
        nagDialog.setContentView(R.layout.dialog_picture);
        Button btnClose = (Button)nagDialog.findViewById(R.id.btnIvClose);
        ImageView ivPreview = (ImageView)nagDialog.findViewById(R.id.iv_preview_image);
//        ivPreview.setImageResource(R.mipmap.ic_launcher);
        ivPreview.setImageBitmap(scaled);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                nagDialog.dismiss();
            }
        });
        nagDialog.show();
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

            if(img!=null)
            {
                img.recycle();
                img=null;
            }

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
                        fat.setProgress((int)(Double.parseDouble(items[1])));
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

}

package th.ac.ku.madlab.beefx;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import th.ac.ku.madlab.kubeef.R;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);


        ImageButton imgBuCam = (ImageButton) findViewById(R.id.imgBuCam);
        ImageButton imgBuGal = (ImageButton) findViewById(R.id.imgBuGal);
        imgBuCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buTakePic(view);
            }
        });
        imgBuGal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buPickGal(view);
            }
        });
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                buTakePic(view);
//            }
//        });
//
//        FloatingActionButton fabGal = (FloatingActionButton) findViewById(R.id.fabGal);
//        fabGal.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                buPickGal(view);
//            }
//        });
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == tag && resultCode == RESULT_OK){
            Bundle b = data.getExtras();
            img = (Bitmap) b.get("data");

//            Intent intent = new Intent(this, MainActivity.class);
//            intent.putExtra("img", img);
//            startActivity(intent);

            Intent intent = new Intent(this, DrawingActivity.class);
            intent.putExtra("img", img);
            startActivity(intent);

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
            int nh = (int) ( imgFull.getHeight() * (256.0 / imgFull.getWidth()) );
            Log.d("nh : ", String.valueOf(nh));
            img = Bitmap.createScaledBitmap(imgFull, 256, nh, true);
            if(imgFull!=null)
            {
                imgFull.recycle();
                imgFull=null;
            }

//            Intent intent = new Intent(this, MainActivity.class);
//            intent.putExtra("img", img);
//            startActivity(intent);

            Intent intent = new Intent(this, DrawingActivity.class);
            intent.putExtra("img", img);
            startActivity(intent);

            cursor.close();
        }
    }
}

package th.ac.ku.madlab.beefx;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileOutputStream;

import th.ac.ku.madlab.kubeef.R;

public class DrawingActivity extends AppCompatActivity {

    LinearLayout mDrawingPad;
    Bitmap img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        Bundle b = getIntent().getExtras();
        img = (Bitmap) b.get("img");

        DrawingView mDrawingView=new DrawingView(this);
        mDrawingPad=(LinearLayout)findViewById(R.id.DrawingPad);
        mDrawingPad.addView(mDrawingView);

        File dir= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File file = new File(dir,"resize.png");
        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(file);
            img.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
//                img.recycle();
//                scaled.recycle();
        } catch (Exception e) {}

        String newPath = file.getAbsolutePath();

        Drawable d;
        d = Drawable.createFromPath(newPath);
        mDrawingPad.setBackground(d);

    }


}

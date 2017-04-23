package th.ac.ku.madlab.beefx;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {

    DBManager dbManager;

    ImageView imageView;
    RatingBar rating;
    ArcProgress fatProgress;
    TextView txtSFat;
    TextView txtMFat;
    TextView txtLFat;
    TextView txtAvgDis;
    Button buMoreInfo;
    LinearLayout LLInfo;
    PieChart pieChart;
    View rootView;

    String[] xData = {"Small", "Medium" , "Large"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        imageView = (ImageView) findViewById(R.id.imageViewPreview);
        rating = (RatingBar) findViewById(R.id.ratingBar);
        fatProgress = (ArcProgress) findViewById(R.id.arc_progress);
        txtSFat = (TextView) findViewById(R.id.tvSFat);
        txtMFat = (TextView) findViewById(R.id.tvMFat);
        txtLFat = (TextView) findViewById(R.id.tvLFat);
        txtAvgDis = (TextView) findViewById(R.id.tvAvgDis);
        buMoreInfo = (Button) findViewById(R.id.buMoreInfo);
        LLInfo = (LinearLayout) findViewById(R.id.LLInfo);
        pieChart = (PieChart) findViewById(R.id.idPieChart);
        TextView tvSDX = (TextView) findViewById(R.id.tvSDX);
        TextView tvSDY = (TextView) findViewById(R.id.tvSDY);
        LLInfo.setVisibility(View.GONE);
        rootView = getWindow().getDecorView().findViewById(android.R.id.content);

        dbManager = new DBManager(this);
        Intent intent = getIntent();
        String ID  = intent.getStringExtra("ID");
        Log.d("ID",ID);
        String[] SelectionsArgs = {ID};
        Cursor cursor=dbManager.query(null,"ID = ? ",SelectionsArgs,"ID");
        if (cursor.moveToFirst()){
            fatProgress.setProgress(cursor.getInt(Math.round(cursor.getColumnIndex("fatPercent"))));
            tvSDX.setText(cursor.getString(cursor.getColumnIndex("sdX")));
            tvSDY.setText(cursor.getString(cursor.getColumnIndex("sdY")));
            txtAvgDis.setText(cursor.getString(cursor.getColumnIndex("avgDistance")));
            txtSFat.setText(cursor.getString( cursor.getColumnIndex("countSmall")));
            txtMFat.setText(cursor.getString( cursor.getColumnIndex("countMedium")));
            txtLFat.setText(cursor.getString( cursor.getColumnIndex("countLarge")));
            rating.setRating(cursor.getFloat( cursor.getColumnIndex("grade")));
            ByteArrayInputStream imageStream = new ByteArrayInputStream(cursor.getBlob(cursor.getColumnIndex("img")));
            final Bitmap theImage= BitmapFactory.decodeStream(imageStream);
            imageView.setImageBitmap(theImage);
            ByteArrayInputStream imageStream1 = new ByteArrayInputStream(cursor.getBlob(cursor.getColumnIndex("imgFatSmall")));
            Bitmap bmpSmall= BitmapFactory.decodeStream(imageStream1);
            ByteArrayInputStream imageStream2 = new ByteArrayInputStream(cursor.getBlob(cursor.getColumnIndex("imgFatMedium")));
            Bitmap bmpMedium= BitmapFactory.decodeStream(imageStream2);
            ByteArrayInputStream imageStream3 = new ByteArrayInputStream(cursor.getBlob(cursor.getColumnIndex("imgFatLarge")));
            Bitmap bmpLarge= BitmapFactory.decodeStream(imageStream3);
            ByteArrayInputStream imageStream4 = new ByteArrayInputStream(cursor.getBlob(cursor.getColumnIndex("imgOri")));
            final Bitmap imgOri= BitmapFactory.decodeStream(imageStream4);

            double[] yData = new double[]{cursor.getDouble(cursor.getColumnIndex("areaSmall")),cursor.getDouble(cursor.getColumnIndex("areaMedium")) ,
                    cursor.getDouble(cursor.getColumnIndex("areaLarge"))};
            pieChartSetting(pieChart,yData,bmpSmall,bmpMedium,bmpLarge);

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
                        bitmapIntoImageView(imageView,theImage);
                        toggle=false;
                    }
                    else
                    {
//                    imageView.setImageBitmap(imgPro.scaled);
                        bitmapIntoImageView(imageView, imgOri);
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
        }
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
//        pieChart.setDescription("Sales by employee (In Thousands $) ");
        pieChart.setRotationEnabled(true);
        pieChart.setDescription("สัดส่วนไขมันแต่ละขนาด");
        //pieChart.setUsePercentValues(true);
        //pieChart.setHoleColor(Color.BLUE);
        //pieChart.setCenterTextColor(Color.BLACK);
        pieChart.setDescriptionPosition(270,270);
        pieChart.setHoleRadius(25f);
        pieChart.setTransparentCircleAlpha(0);
        pieChart.setCenterText("Area");
        pieChart.setCenterTextSize(10);
        addDataSet(yData);
        //pieChart.setDrawEntryLabels(true);
        //pieChart.setEntryLabelTextSize(20);

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
                Toast.makeText(DetailActivity.this, size + " Fat" + "\n" + "Areas : " + sales +"%", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });
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

        //add legend to chart
//        Legend legend = pieChart.getLegend();
//        legend.setForm(Legend.LegendForm.CIRCLE);
//        legend.setPosition(Legend.LegendPosition.LEFT_OF_CHART);

        //create pie data object
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }
}

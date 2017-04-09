package th.ac.ku.madlab.beefx;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

public class ShowActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    DBManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dbManager = new DBManager(this);
        LoadElement();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            Intent intent = new Intent(this, Main2Activity.class);
            startActivity(intent);
        } else if (id == R.id.nav_gallery) {
            Intent intent = new Intent(this, ShowActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_slideshow) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_share) {
            Intent intent = new Intent(this, UniversityActivity.class);
            startActivity(intent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    ArrayList<AdapterItems> listnewsData = new ArrayList<AdapterItems>();
    MyCustomAdapter myadapter;

    void LoadElement()
    {
        //add data and view it
        listnewsData.clear();
        Cursor cursor=dbManager.query(null,null,null,null);
        if (cursor.moveToFirst()){
//            String tableData="";
            do {
                listnewsData.add(new AdapterItems(cursor.getInt(cursor.getColumnIndex("ID"))
                        ,cursor.getDouble( cursor.getColumnIndex("fatPercent"))
                        ,cursor.getDouble(cursor.getColumnIndex("sdX"))
                        ,cursor.getDouble(cursor.getColumnIndex("sdY"))
                        ,cursor.getBlob(cursor.getColumnIndex("img"))
                        ,cursor.getString(cursor.getColumnIndex("created_at"))));
            }while (cursor.moveToNext());
//            Toast.makeText(getApplicationContext(),tableData,Toast.LENGTH_LONG).show();
        }

        myadapter=new MyCustomAdapter(listnewsData);
        ListView lsNews=(ListView)findViewById(R.id.LVNews);
        lsNews.setAdapter(myadapter);//intisal with data
    }

    private class MyCustomAdapter extends BaseAdapter {
        public ArrayList<AdapterItems> listnewsDataAdpater ;

        public MyCustomAdapter(ArrayList<AdapterItems>  listnewsDataAdpater) {
            this.listnewsDataAdpater=listnewsDataAdpater;
        }


        @Override
        public int getCount() {
            return listnewsDataAdpater.size();
        }

        @Override
        public String getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            LayoutInflater mInflater = getLayoutInflater();
            View myView = mInflater.inflate(R.layout.info_layout, null);

            final   AdapterItems s = listnewsDataAdpater.get(position);

            TextView tvID=( TextView)myView.findViewById(R.id.tvID);
            tvID.setText(Html.fromHtml("<b>" + "ID : " + "</b>" + String.valueOf( s.ID)));

            TextView tvFat=( TextView)myView.findViewById(R.id.tvFat);
            tvFat.setText(Html.fromHtml("<b>" + "FatPercent : " + "</b>" + Double.toString(s.FatPercent)));

            TextView tvSdX=( TextView)myView.findViewById(R.id.tvSdX);
            tvSdX.setText(Html.fromHtml("<b>" + "SdX : " + "</b>" + Double.toString(s.SdX)));

            TextView tvSdY=( TextView)myView.findViewById(R.id.tvSdY);
            tvSdY.setText(Html.fromHtml("<b>" + "SdY : " + "</b>" + Double.toString(s.SdY)));

            TextView tvTime=( TextView)myView.findViewById(R.id.tvTime);
            tvTime.setText(s.CreateTime);

            ImageView iv = (ImageView)myView.findViewById(R.id.ivOri);
            ByteArrayInputStream imageStream = new ByteArrayInputStream(s.img);
            Bitmap theImage= BitmapFactory.decodeStream(imageStream);
            iv.setImageBitmap(theImage);


            return myView;
        }

    }
}

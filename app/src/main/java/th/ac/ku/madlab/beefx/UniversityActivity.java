package th.ac.ku.madlab.beefx;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.widget.TextView;

public class UniversityActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_university);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        TextView txtUni = (TextView)findViewById(R.id.txtUni);
        txtUni.setText(Html.fromHtml("<h2><b>มหาวิทยาลัยเกษตรศาสตร์</b></h2>" +
                "<p>เลขที่ 50 ถนนงามวงศ์วาน แขวงลาดยาว เขตจตุจักร กรุงเทพฯ 10900</p>" +
                "<p>โทรศัพท์ : 0-2579-0113, 0-2942-8200-45</p>" +
                "<p>เว็ปไซดิ์  :  <a href=\"http://www.ku.ac.th\">http://www.ku.ac.th</a></p>"));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

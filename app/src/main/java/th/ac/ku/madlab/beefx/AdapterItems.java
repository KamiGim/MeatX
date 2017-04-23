package th.ac.ku.madlab.beefx;

/**
 * Created by kami on 4/8/2017.
 */

public class AdapterItems {
    public  int ID;
    public  double FatPercent;
    public  double SdX;
    public  double SdY;
    public  byte[] img;
    public  String CreateTime;
    double Grade;

    //for news details
    AdapterItems( int ID,double Grade, double FatPercent,double SdX,double SdY,byte[] img,String CreateTime)
    {
        this.ID=ID;
        this.Grade = Grade;
        this.FatPercent = FatPercent;
        this.SdX = SdX;
        this.SdY = SdY;
        this.img = img;
        this.CreateTime = CreateTime;
    }
}

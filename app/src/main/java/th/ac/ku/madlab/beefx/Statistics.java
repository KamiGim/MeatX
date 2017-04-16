package th.ac.ku.madlab.beefx;

import java.util.Arrays;

/**
 * Created by kami on 4/5/2017.
 */

public class Statistics {
    double[] dataX;
    double[] dataY;
    int size;

    public Statistics(double[] dataX,double[] dataY)
    {
        this.dataX = dataX;
        this.dataY = dataY;
        size = dataX.length;
    }

    double getMeanX()
    {
        double sum = 0.0;
        for(double a : dataX)
            sum += a;
        return sum/size;
    }

    double getMeanY()
    {
        double sum = 0.0;
        for(double a : dataY)
            sum += a;
        return sum/size;
    }

    double getVarianceX()
    {
        double mean = getMeanX();
        double temp = 0;
        for(double a :dataX)
            temp += (a-mean)*(a-mean);
        return temp/size;
    }

    double getVarianceY()
    {
        double mean = getMeanY();
        double temp = 0;
        for(double a :dataY)
            temp += (a-mean)*(a-mean);
        return temp/size;
    }

    double getAvgDistance()
    {
        double meanX = getMeanX();
        double meanY = getMeanY();

        double temp = 0;
        for(int i = 0;i < size;i++) {
            double deltaX = dataX[i] - meanX;
            double deltaY = dataY[i] - meanY;
            temp += Math.sqrt((deltaY) * (deltaY) + (deltaX) * (deltaX));
        }
        return temp/size;
    }

    double getStdDevX()
    {
        return Math.sqrt(getVarianceX());
    }

    double getStdDevY()
    {
        return Math.sqrt(getVarianceY());
    }

}

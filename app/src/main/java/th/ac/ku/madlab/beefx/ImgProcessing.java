package th.ac.ku.madlab.beefx;

import android.graphics.Bitmap;
import android.util.Log;

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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by kami on 4/6/2017.
 */

public class ImgProcessing {
    Bitmap scaled;
    Bitmap original;
    int countSmall = 0;
    int countMedium = 0;
    int countLarge = 0;
    double areaSmall = 0;
    double areaMedium = 0;
    double areaLarge = 0;
    double[] xpos;
    double[] ypos;
    int w;
    int h;
    int totalNum;
    int fatNum;
    double fatPercent;

    public ImgProcessing(Bitmap scaled,Bitmap original) {
        this.scaled = scaled;
        this.original = original;
    }

    public void Process1() {
        w = scaled.getWidth();
        h = scaled.getHeight();
        Mat imgMat = new Mat(h, w, CvType.CV_8UC3);
        Utils.bitmapToMat(scaled, imgMat);
        Mat tmp = new Mat(h, w, CvType.CV_8UC3);
        Utils.bitmapToMat(scaled, tmp);

        Mat imgMatOri = new Mat(h, w, CvType.CV_8UC3);
        Utils.bitmapToMat(original, imgMatOri);

        Mat hsv = new Mat();
        Imgproc.cvtColor(tmp, hsv, Imgproc.COLOR_RGB2HSV);
        Mat mask = new Mat();
        Core.inRange(hsv, new Scalar(0, 254, 254), new Scalar(1, 255, 255), mask);

//            Core.bitwise_not(mask,mask);
        Mat beef = new Mat();
        Imgproc.dilate(mask, mask, Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(10, 10)));

        double maxVal = 0;
        int maxValIdx = 0;
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(mask, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
            double contourArea = Imgproc.contourArea(contours.get(contourIdx));
            if (maxVal < contourArea) {
                maxVal = contourArea;
                maxValIdx = contourIdx;
            }
        }

        Log.d("contours.size() :", Integer.toString(contours.size()));

        Mat blank = new Mat(h, w, CvType.CV_8UC3, new Scalar(0, 0, 0));
        Imgproc.drawContours(blank, contours, maxValIdx, new Scalar(255, 255, 255), -1);
//            Mat BW = new Mat();
        Imgproc.cvtColor(blank, blank, Imgproc.COLOR_RGB2GRAY);
        Imgproc.threshold(blank, blank, 10, 255, Imgproc.THRESH_BINARY);
        Imgproc.erode(blank, blank, Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(10, 10)));

        Core.bitwise_and(tmp, tmp, beef, blank);
        List<MatOfPoint> contours_beef = new ArrayList<>();
        Imgproc.findContours(blank, contours_beef, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        Imgproc.drawContours(beef, contours_beef, -1, new Scalar(255, 0, 0), 5);

        Mat gray = new Mat();
        Imgproc.cvtColor(imgMatOri, gray, Imgproc.COLOR_RGB2GRAY);
        Mat fat = new Mat();
        Imgproc.threshold(gray, gray, 0, 255, Imgproc.THRESH_OTSU);
//            Imgproc.threshold(gray, gray, 140, 255,Imgproc.THRESH_BINARY);

        Core.bitwise_and(gray, blank, fat);

        List<MatOfPoint> contours_fat = new ArrayList<>();
//        Imgproc.findContours(fat, contours_fat, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        Imgproc.findContours(fat, contours_fat, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);

        countSmall = 0;
        countMedium = 0;
        countLarge = 0;

        areaSmall = 0;
        areaMedium = 0;
        areaLarge = 0;

        List<Moments> mu = new ArrayList<Moments>(contours_fat.size());


        xpos = new double[contours_fat.size()];
        ypos = new double[contours_fat.size()];

        for (int contourIdx = 0; contourIdx < contours_fat.size(); contourIdx++) {
            double contourArea = Imgproc.contourArea(contours_fat.get(contourIdx));
            mu.add(contourIdx, Imgproc.moments(contours_fat.get(contourIdx), false));

            Moments p = mu.get(contourIdx);


            int x = (int) (p.get_m10() / p.get_m00());
            int y = (int) (p.get_m01() / p.get_m00());

            if (x == 0 && y == 0) {
                Rect bound = Imgproc.boundingRect(contours_fat.get(contourIdx));
                x = bound.x;
                y = bound.y;
            }

            xpos[contourIdx] = (double) x;
            ypos[contourIdx] = (double) y;

            Log.d("area,centroid", Double.toString(contourArea) + ",(" + Integer.toString(x) + "," + Integer.toString(y) + ")");

            if (contourArea <= 100) {
                Imgproc.drawContours(beef, contours_fat, contourIdx, new Scalar(0, 255, 255), 2);
                countSmall++;
                areaSmall = areaSmall + contourArea;
            } else if (contourArea > 100 && contourArea < 500) {
                Imgproc.drawContours(beef, contours_fat, contourIdx, new Scalar(0, 0, 255), 2);
                countMedium++;
                areaMedium = areaMedium + contourArea;
            } else {
                Imgproc.drawContours(beef, contours_fat, contourIdx, new Scalar(255, 0, 255), 2);
                countLarge++;
                areaLarge = areaLarge + contourArea;
            }
        }
        Utils.matToBitmap(beef, scaled);
        totalNum = Core.countNonZero(blank);
        fatNum = Core.countNonZero(fat);
        fatPercent = (float)fatNum/totalNum*100;
    }

    public void Process2() {
        Mat imgMatOri = new Mat(h, w, CvType.CV_8UC3);
        Utils.bitmapToMat(original, imgMatOri);

        w = scaled.getWidth();
        h = scaled.getHeight();
        Mat imgMat = new Mat(h, w, CvType.CV_8UC3);
        Utils.bitmapToMat(scaled, imgMat);
        Mat tmp = new Mat(h, w, CvType.CV_8UC3);
        Utils.bitmapToMat(scaled, tmp);

        Mat hsv = new Mat();
        Imgproc.cvtColor(tmp, hsv, Imgproc.COLOR_RGB2HSV);
        Mat mask = new Mat();
        Core.inRange(hsv, new Scalar(0, 254, 254), new Scalar(1, 255, 255), mask);

//            Core.bitwise_not(mask,mask);
        Mat beef = new Mat();
        Imgproc.dilate(mask, mask, Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(10, 10)));

        double maxVal = 0;
        int maxValIdx = 0;
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(mask, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
            double contourArea = Imgproc.contourArea(contours.get(contourIdx));
            if (maxVal < contourArea) {
                maxVal = contourArea;
                maxValIdx = contourIdx;
            }
        }

        Log.d("contours.size() :", Integer.toString(contours.size()));

        Mat blank = new Mat(h, w, CvType.CV_8UC3, new Scalar(0, 0, 0));
        Imgproc.drawContours(blank, contours, maxValIdx, new Scalar(255, 255, 255), -1);
//            Mat BW = new Mat();
        Imgproc.cvtColor(blank, blank, Imgproc.COLOR_RGB2GRAY);
        Imgproc.threshold(blank, blank, 10, 255, Imgproc.THRESH_BINARY);
        Imgproc.erode(blank, blank, Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(10, 10)));

        Core.bitwise_and(tmp, tmp, beef, blank);

        Mat gray = new Mat();
        Imgproc.cvtColor(beef, gray, Imgproc.COLOR_RGB2GRAY);

        byte [] pixels = new byte[ w * h ];
        gray.get(0,0,pixels);

        List<Integer> intList = new ArrayList<>();
        for (int index = 0; index < pixels.length; index++)
        {
            if (pixels[index]>0){
                intList.add((int)pixels[index]);
            }
        }

        Log.d("mat2List",intList.toString());

        int [] histData = new int[256];

        // Calculate histogram
        int ptr = 0;
        while (ptr < intList.size()) {
            int h = 0xFF & intList.get(ptr);
            histData[h] ++;
            ptr ++;
        }

        // Total number of pixels
        int total = intList.size();

        float sum = 0;
        for (int t=0 ; t<256 ; t++) sum += t * histData[t];

        float sumB = 0;
        int wB = 0;
        int wF = 0;

        float varMax = 0;
        int threshold = 0;

        for (int t=0 ; t<256 ; t++) {
            wB += histData[t];               // Weight Background
            if (wB == 0) continue;

            wF = total - wB;                 // Weight Foreground
            if (wF == 0) break;

            sumB += (float) (t * histData[t]);

            float mB = sumB / wB;            // Mean Background
            float mF = (sum - sumB) / wF;    // Mean Foreground

            // Calculate Between Class Variance
            float varBetween = (float)wB * (float)wF * (mB - mF) * (mB - mF);

            // Check if new maximum found
            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = t;
            }
        }

        Log.d("mat2List",Integer.toString(threshold));

        Mat fat = new Mat();
        Imgproc.threshold(gray, gray, threshold+5, 255,Imgproc.THRESH_BINARY);
//        Imgproc.threshold(gray, gray, 10, 255, Imgproc.THRESH_OTSU);

        List<MatOfPoint> contours_beef = new ArrayList<>();
        Imgproc.findContours(blank, contours_beef, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        Imgproc.drawContours(beef, contours_beef, -1, new Scalar(255, 0, 0), 5);

        /*Mat gray = new Mat();
        Imgproc.cvtColor(imgMatOri, gray, Imgproc.COLOR_RGB2GRAY);
        Mat fat = new Mat();
        Imgproc.threshold(gray, gray, 1, 255, Imgproc.THRESH_OTSU);*/

//            Imgproc.threshold(gray, gray, 140, 255,Imgproc.THRESH_BINARY);

        Core.bitwise_and(gray, blank, fat);

        List<MatOfPoint> contours_fat = new ArrayList<>();
//        Imgproc.findContours(fat, contours_fat, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        Imgproc.findContours(fat, contours_fat, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);

        countSmall = 0;
        countMedium = 0;
        countLarge = 0;

        areaSmall = 0;
        areaMedium = 0;
        areaLarge = 0;

        List<Moments> mu = new ArrayList<Moments>(contours_fat.size());


        xpos = new double[contours_fat.size()];
        ypos = new double[contours_fat.size()];

        for (int contourIdx = 0; contourIdx < contours_fat.size(); contourIdx++) {
            double contourArea = Imgproc.contourArea(contours_fat.get(contourIdx));
            mu.add(contourIdx, Imgproc.moments(contours_fat.get(contourIdx), false));

            Moments p = mu.get(contourIdx);


            int x = (int) (p.get_m10() / p.get_m00());
            int y = (int) (p.get_m01() / p.get_m00());

            if (x == 0 && y == 0) {
                Rect bound = Imgproc.boundingRect(contours_fat.get(contourIdx));
                x = bound.x;
                y = bound.y;
            }

            xpos[contourIdx] = (double) x;
            ypos[contourIdx] = (double) y;

            Log.d("area,centroid", Double.toString(contourArea) + ",(" + Integer.toString(x) + "," + Integer.toString(y) + ")");

            if (contourArea <= 100) {
                Imgproc.drawContours(beef, contours_fat, contourIdx, new Scalar(0, 255, 255), 2);
                countSmall++;
                areaSmall = areaSmall + contourArea;
            } else if (contourArea > 100 && contourArea < 500) {
                Imgproc.drawContours(beef, contours_fat, contourIdx, new Scalar(0, 0, 255), 2);
                countMedium++;
                areaMedium = areaMedium + contourArea;
            } else {
                Imgproc.drawContours(beef, contours_fat, contourIdx, new Scalar(255, 0, 255), 2);
                countLarge++;
                areaLarge = areaLarge + contourArea;
            }
        }

        Utils.matToBitmap(beef, scaled);
        totalNum = Core.countNonZero(blank);
        fatNum = Core.countNonZero(fat);
        fatPercent = (double)fatNum/totalNum*100;
    }

}

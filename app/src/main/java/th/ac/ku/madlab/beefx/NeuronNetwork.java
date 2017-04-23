package th.ac.ku.madlab.beefx;

/**
 * Created by Kami on 4/16/2017.
 */

public class NeuronNetwork {
    double fatPercent;
    int countSmall;
    int countMedium;
    int countLarge;
    double avgDistance;
    double avgDistance_s;
    double avgDistance_m;
    double avgDistance_l;
    double sdX;
    double sdY;
    double areaSmall;
    double areaMedium;
    double areaLarge;
    public NeuronNetwork(double fatPercent,int countSmall,int countMedium,int countLarge,double avgDistance,double sdX,double sdY,double areaSmall,double areaMedium,double areaLarge,double avgDistance_s,double avgDistance_m,double avgDistance_l){
        this.fatPercent = fatPercent;
        this.countSmall = countSmall;
        this.countMedium = countMedium;
        this.countLarge = countLarge;
        this.avgDistance = avgDistance;
        this.avgDistance_s = avgDistance_s;
        this.avgDistance_m = avgDistance_m;
        this.avgDistance_l = avgDistance_l;
        this.sdX = sdX;
        this.sdY = sdY;
        this.areaSmall = areaSmall;
        this.areaMedium = areaMedium;
        this.areaLarge = areaLarge;
    }

    double node1(){
        double tmp = fatPercent*-4.371+avgDistance*3.891+sdX*1.920+sdY*2.204+avgDistance_s*3.750+avgDistance_m*-2.866+avgDistance_l*0.427+areaSmall*2.176+areaMedium*-5.476+areaLarge*3.670+-1.768;
        return 1 / (1 + Math.pow(Math.E,-tmp));
    }
    double node2(){
        double tmp = fatPercent*-12.256+avgDistance*0.947+sdX*-0.479+sdY*-0.843+avgDistance_s*-0.412+avgDistance_m*0.854+avgDistance_l*0.213+areaSmall*-2.279+areaMedium*3.226+areaLarge*1.194+-5.770;
        return 1 / (1 + Math.pow(Math.E,-tmp));
    }
    double node3(){
        double tmp = fatPercent*-12.507+avgDistance*0.931+sdX*3.852+sdY*-0.289+avgDistance_s*-1.546+avgDistance_m*-4.830+avgDistance_l*4.265+areaSmall*9.990+areaMedium*-4.154+areaLarge*-1.504+-6.788;
        return 1 / (1 + Math.pow(Math.E,-tmp));
    }
    double node4(){
        double tmp = fatPercent*0.969+avgDistance*-0.162+sdX*-3.340+sdY*1.251+avgDistance_s*2.067+avgDistance_m*-2.719+avgDistance_l*11.553+areaSmall*-5.416+areaMedium*7.251+areaLarge*-1.482+-1.477;
        return 1 / (1 + Math.pow(Math.E,-tmp));
    }
    double node5(){
        double tmp = fatPercent*1.207+avgDistance*-1.844+sdX*0.255+sdY*-6.183+avgDistance_s*-2.690+avgDistance_m*5.442+avgDistance_l*-3.572+areaSmall*5.425+areaMedium*-0.651+areaLarge*-2.643+-2.885;
        return 1 / (1 + Math.pow(Math.E,-tmp));
    }
    double node6(){
        double tmp = fatPercent*-0.151+avgDistance*1.842+sdX*0.862+sdY*2.047+avgDistance_s*2.042+avgDistance_m*1.342+avgDistance_l*-0.034+areaSmall*1.527+areaMedium*2.023+areaLarge*-2.838+-0.353;
        return 1 / (1 + Math.pow(Math.E,-tmp));
    }
    double node7(){
        double tmp = fatPercent*-8.676+avgDistance*-0.378+sdX*-0.229+sdY*-2.111+avgDistance_s*-0.673+avgDistance_m*-2.149+avgDistance_l*1.862+areaSmall*-0.387+areaMedium*1.228+areaLarge*-0.416+-0.796;
        return 1 / (1 + Math.pow(Math.E,-tmp));
    }
    double node8(){
        double tmp = fatPercent*4.314+avgDistance*2.183+sdX*3.652+sdY*0.038+avgDistance_s*2.958+avgDistance_m*7.235+avgDistance_l*-3.357+areaSmall*-0.742+areaMedium*-3.148+areaLarge*3.893+-1.400;
        return 1 / (1 + Math.pow(Math.E,-tmp));
    }
    double node9(){
        double tmp = fatPercent*-2.932+avgDistance*1.811+sdX*0.417+sdY*0.300+avgDistance_s*2.699+avgDistance_m*2.364+avgDistance_l*-0.484+areaSmall*7.299+areaMedium*-3.967+areaLarge*-2.568+0.542;
        return 1 / (1 + Math.pow(Math.E,-tmp));
    }
//    double getGrade(){
//        return node1()*0.419+node2()*0.567+node3()*1.010+node4()*0.856+node5()*-2.038+node6()*1.712+node7()*2.333-1.070;
//    }
    double getGrade2(){
        double tmp =  node1()*-5.590+node2()*8.349+node3()*9.689+node4()*-8.027+node5()*-5.467+node6()*2.145+node7()*1.134+node8()*8.056+node9()*-5.323+-4.737;
        return 1 / (1 + Math.pow(Math.E,-tmp));
//        return tmp;
    }
    double getGrade3(){
        double tmp =  node1()*6.036+node2()*-6.396+node3()*-9.859+node4()*8.106+node5()*6.404+node6()*-1.705+node7()*7.565+node8()*-8.170+node9()*5.123+-5.999;
//        return tmp;
        return 1 / (1 + Math.pow(Math.E,-tmp));
    }
    double getGrade35(){
        double tmp =  node1()*1.231+node2()*-5.695+node3()*-3.227+node4()*-4.663+node5()*-3.112+node6()*0.493+node7()*-2.505+node8()*1.199+node9()*3.568+-1.613;
//        return tmp;
        return 1 / (1 + Math.pow(Math.E,-tmp));
    }
    double getGrade4(){
        double tmp =  node1()*-2.285+node2()*-2.683+node3()*-1.349+node4()*-1.228+node5()*-1.399+node6()*0.293+node7()*-0.475+node8()*-2.163+node9()*-1.465+0.887;
//        return tmp;
        return 1 / (1 + Math.pow(Math.E,-tmp));
    }
    double getGrade45(){
        double tmp =  node1()*-2.138+node2()*-1.816+node3()*-1.310+node4()*-1.721+node5()*-1.641+node6()*-2.107+node7()*-2.196+node8()*4.223+node9()*-1.133+-1.689;
//        return tmp;
        return 1 / (1 + Math.pow(Math.E,-tmp));
    }
    double getGrade5(){
        double tmp =  node1()*-1.380+node2()*-3.049+node3()*-1.954+node4()*4.654+node5()*-0.340+node6()*0.550+node7()*-4.500+node8()*-1.565+node9()*-2.637+-1.239;
//        return tmp;
        return 1 / (1 + Math.pow(Math.E,-tmp));
    }

}

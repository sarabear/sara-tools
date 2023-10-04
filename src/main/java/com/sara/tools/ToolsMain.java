package com.sara.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLHandshakeException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

public class ToolsMain {

    private static final Logger log = LoggerFactory.getLogger(ToolsMain.class);

    private static Random random = new Random();

    public static void main(String[] args) {
        //checkIp();
        generateCalculationQuestions();
    }

    private static void generateCalculationQuestions(){
        int count = 40;
        int max = 20;
        int separateCount = 2;
        for(int i = 0; i < count; i++){
            int random34 = random.nextInt(4);
            //1/4概率生成4个数，1/3概率生成3个数
            String result = random34 == 0 ? generate4(max) : generate3(max);
            System.out.println(result+"=");
            if(i == count / separateCount - 1){
                System.out.println();
            }
        }
    }

    /**
     * 生成3位数加减法
     * 1、先随机一个凑10或减10的
     * 2、再随机加或减一个数
     * 3、再随机第三个数的位置
     */
    private static String generate3(int max){
        //随机第一第二个数是加还是减
        int randomOp1 = random.nextInt(2);
        String op1 = randomOp1 == 1 ? "+" : "-";
        int[] twoNumber = randomOp1 == 1 ? generateTen2(max) : generateMinusTen2(max);
        //随机第三个数是加还是减
        int randomOp2 = random.nextInt(2);
        String op2 = randomOp2 == 1 ? "+" : "-";
        int threeNumber = randomOp2 == 1 ? random.nextInt(max) + 1 : (randomOp1 == 1 ? random.nextInt(twoNumber[0] + twoNumber[1]) + 1 : random.nextInt(twoNumber[0] - twoNumber[1] + 1));
        int randomSeq = random.nextInt(2);
        switch (randomSeq){
            case 0:
                return String.format("%d%s%d%s%d", twoNumber[0], op2, threeNumber, op1, twoNumber[1]);
            case 1:
                return String.format("%d%s%d%s%d", twoNumber[0], op1, twoNumber[1], op2, threeNumber);
        }
        return null;
    }

    /**
     * 生成3位数加减法
     * 1、先随机一个凑10或减10的
     * 2、再随机另一个凑10或减10的
     * 3、再随机2、3、4个数的位置
     */
    private static String generate4(int max) {
        //随机第一第二个数是加还是减
        int randomOp1 = random.nextInt(2);
        String op1 = randomOp1 == 1 ? "+" : "-";
        int[] twoNumber = randomOp1 == 1 ? generateTen2(max) : generateMinusTen2(max);

        //随机第三第四个数是加还是减
        int randomOp2 = random.nextInt(2);
        String op2 = randomOp2 == 1 ? "+" : "-";
        int[] twoNumber2 = randomOp2 == 1 ? generateTen2(max) : generateMinusTen2(max);

        //随机第二第三第四个数的位置
        int randomSeq = random.nextInt(3);
        switch (randomSeq){
            case 0:
                return String.format("%d%s%d%s%d%s%d", twoNumber[0], op1, twoNumber[1], "+", twoNumber2[0], op2, twoNumber2[1]);
            case 1:
                return String.format("%d%s%d%s%d%s%d", twoNumber[0], "+", twoNumber2[0], op1, twoNumber[1], op2, twoNumber2[1]);
            case 2:
                return String.format("%d%s%d%s%d%s%d", twoNumber[0], "+", twoNumber2[0], op2, twoNumber2[1], op1, twoNumber[1]);
        }
        return null;
    }

    /**
     * 生成2个凑10的加数
     */
    private static int[] generateTen2(int max){
        int first = random.nextInt(max - 1) + 1;
        int second = random.nextInt(max);
        second = second / 10 * 10 + (10 - first % 10);
        return new int[]{first, second};
    }

    /**
     * 生成2个减10的减数
     */
    private static int[] generateMinusTen2(int max){
        int first = random.nextInt(max - 11) + 11;
        int second = random.nextInt(first - 10);
        second = (second / 10) * 10 + first % 10;
        return new int[]{first, second};
    }

    private static void checkIp(){
        String urlFormat = "http://192.168.42.%d/";
        for(int i = 2; i < 255 ; i ++){
            String url = String.format(urlFormat, i);
            try {
                URL realUrl = new URL(url);
                URLConnection connection = realUrl.openConnection();
                connection.setConnectTimeout(1000);
                connection.setReadTimeout(1000);
                connection.connect();
                log.info("ToolsMain ok: {}", i);
            } catch (SSLHandshakeException sslException){
                log.info("ToolsMain ok: {}", i);
            } catch (Exception e) {
                log.info("ToolsMain error: {}", i);
            }
        }
    }

}

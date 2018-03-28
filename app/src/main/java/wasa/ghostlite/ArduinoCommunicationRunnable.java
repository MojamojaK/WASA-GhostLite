package wasa.ghostlite;

import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class ArduinoCommunicationRunnable implements Runnable {

    private static final String TAG = MainActivity.class.getSimpleName();

    private FileInputStream inputStream = null;
    private OutputStream outputStream = null;
    private DrawMapView drawMapView;
    private ArduinoCommunication arduinoCommunication;


    private static final int payload_len = 98;
    private static int buffer_in_len = 0;
    private static byte[] buffer = new byte[1024];
    private static byte[] in_payload = new byte[payload_len];
    private byte payload[] = new byte[128];

    private final Handler handler = new Handler();
    private File file;
    private FileOutputStream fos;

    private static DecimalFormat df = new DecimalFormat("#.00");


    ArduinoCommunicationRunnable(FileInputStream inputStream, OutputStream outputStream, DrawMapView drawMapView, ArduinoCommunication arduinoCommunication) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.drawMapView = drawMapView;
        this.arduinoCommunication = arduinoCommunication;
        df.setRoundingMode(RoundingMode.CEILING);
    }

    @Override
    public void run(){
        arduinoCommunication.threadRunning = true;
        while (arduinoCommunication.threadRunning){
            try {
                Thread.sleep(200);
                parseInputBuffer(inputStream);
                //ファイルへの書き込み
            /*try {
                fos = new FileOutputStream(file, true);
                OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                BufferedWriter bw = new BufferedWriter(osw);

                //String str = h+":"+m+":"+s + "," + air + "," + rot + "," + height + "," +rud + "," + ele + "," + rudTemp + "," + eleTemp + "\r\n";
                String str = cadence +"\n";
                bw.write(str);
                bw.flush();
                bw.close();
            } catch (Exception e) {
                Log.e(TAG, "file write failed",e);
            }*/

                //Arduinoへの出力
                /*if (outputStream != null) {
                    try {
                        String tmpStr = "TEST PAYLOAD " + Long.toString(Thread.currentThread().getId()) + "\n";
                        outputStream.write(tmpStr.getBytes());
                    } catch (IOException e) {
                        Log.e(TAG, "arduino write failed", e);
                    }
                }*/

                //ViewTest呼び出し
            /*if(c_sens == 0) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                       drawMapView.updateData(cadence);
                    }
                });
            }
            c_sens = (c_sens+1)%2;*/
            } catch (InterruptedException e) {
            }
        }
    }

    public void cancel() {
        arduinoCommunication.threadRunning = false;
    }

    private static boolean got_head_high = false;
    private static boolean got_head_low = false;
    private static boolean got_head_type = false;
    private static boolean got_length = false;
    private static boolean got_checksum = false;
    private static boolean reading = false;
    private static int head_type = 0;
    private static int length = 0;
    private static int index = 0;
    private static byte receive_checksum = 0;

    private synchronized void parseInputBuffer (FileInputStream inputStream) {
        try {
            /*if (this.drawMapView.altitude == 1) {
                this.drawMapView.altitude = 0;
            } else {
                this.drawMapView.altitude = 1;
            }*/
            buffer_in_len = inputStream.read(buffer);
            this.drawMapView.air = 0;
            for (int i = 0; i < buffer_in_len; i++) {
                int read_byte = (buffer[i] & 0xFF);
                if (!reading) {
                    if (!got_head_high) {
                        if (read_byte == 0x7C) {
                            got_head_high = true;
                        } else {
                            resetParser();
                        }
                    } else if (!got_head_low) {
                        if (read_byte == 0xC7) {
                            got_head_low = true;
                        } else {
                            resetParser();
                        }
                    } else if (!got_head_type) {
                        if (read_byte < 2) {
                            head_type = read_byte;
                            got_head_type = true;
                        } else {
                            resetParser();
                        }
                    } else if (!got_length) {
                        if (read_byte < 128) {
                            length = read_byte;
                            got_length = true;
                            reading = true;
                        } else {
                            resetParser();
                        }
                    }
                } else {
                    if (index < length) {
                        in_payload[index++] = (byte)read_byte;
                    } else if (!got_checksum) {
                        receive_checksum = (byte)read_byte;
                        got_checksum = true;
                        if ((receive_checksum & 0xFF) == (checksum(in_payload) & 0xFF)) {
                            packVariables();
                        }
                        resetParser();
                    } else {
                        resetParser();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void packVariables() {
        drawMapView.cadence = (int)(composeInt32(in_payload[4], in_payload[3], in_payload[2], in_payload[1]) * 0.00046875); // 回転数
        drawMapView.air = Float.parseFloat(df.format((float)(composeInt32(in_payload[8], in_payload[7], in_payload[6], in_payload[5]) * 6.15180146890866e-7))); // 機速(10倍)
        // ground += 0.1f;
        drawMapView.altitude = Float.parseFloat(df.format(composeInt16(in_payload[23],in_payload[22]) / 100.0f)); // 高度(100倍)
        // drawMapView.rud = composeInt(buffer[6],buffer[7]); // ラダー
        // drawMapView.ele = composeInt(buffer[8],buffer[9]); // エレベータ
        // drawMapView.rudTemp = composeInt(buffer[10],buffer[11]); // ラダー温度
        // drawMapView.eleTemp = composeInt(buffer[12],buffer[13]); // エレベータ温度

        drawMapView.yaw = composeInt16(in_payload[10], in_payload[9]);
        drawMapView.pitch = composeInt16(in_payload[14], in_payload[13]) / -100.0f;
        drawMapView.roll = composeInt16(in_payload[12], in_payload[11]) / 100.0f;
        // if(buffer[30] == 1) rud*= -1;
        // if(buffer[31] == 1) ele*= -1;
        /*if(outputStream != null){
            try{
                String tmpStr = "TEST PAYLOAD" + Thread.currentThread().getId() + "\n";
                outputStream.write(tmpStr.getBytes());
            } catch(IOException e) {
                Log.e(TAG, "arduino write failed",e);
            }
        }*/

        // 画面更新
        // 画面の更新はメインスレッドからしか行えないため
        // handler.postを介してupdateData()しないと更新されない
        handler.post(new Runnable() {
            @Override
            public void run() {
                drawMapView.updateData();
            }
        });
    }

    private void resetParser() {
        got_head_high = false;
        got_head_low = false;
        got_head_type = false;
        got_length = false;
        got_checksum = false;
        reading = false;
        index = 0;
    }

    private static int composeInt16(byte hi, byte lo){
        int val = ((hi & 0xFF) << 8) | (lo & 0xFF);
        if ((val & 0x8000) > 0) val -= 0x10000; // 2byte signed int から 4byte unsigned int に変換
        return val;
    }

    private static int composeInt32(byte hihi, byte hilo, byte lohi, byte lolo){
        return (((hihi & 0xFF) << 24) & 0xFF000000) | (((hilo & 0xFF) << 16) & 0x00FF0000) | (((lohi & 0xFF) << 8) & 0x0000FF00) | (lolo & 0xFF);
    }

    private static byte checksum(byte[] buffer) {
        byte sum = 0;
        for (int i = 0; i < payload_len; i++) {
            sum ^= (buffer[i] & 0xFF);
        }
        return sum;
    }
}

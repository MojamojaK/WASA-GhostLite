package wasa.ghostlite;

import android.os.Handler;

import java.io.*;
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

    private static DecimalFormat df1 = new DecimalFormat("#.0");
    private static DecimalFormat df2 = new DecimalFormat("#.00");
    private static DecimalFormat df3 = new DecimalFormat("#.00");
    private static DecimalFormat df7 = new DecimalFormat("#.0000000");


    ArduinoCommunicationRunnable(FileInputStream inputStream, OutputStream outputStream, DrawMapView drawMapView, ArduinoCommunication arduinoCommunication) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.drawMapView = drawMapView;
        this.arduinoCommunication = arduinoCommunication;
        df2.setRoundingMode(RoundingMode.CEILING);
    }

    @Override
    public void run(){
        arduinoCommunication.threadRunning = true;
        while (arduinoCommunication.threadRunning){
            try {
                Thread.sleep(200);
                if (parseInputBuffer(inputStream)) {

                }
                //ファイルへの書き込み
//            try {
//                fos = new FileOutputStream(file, true);
//                OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
//                BufferedWriter bw = new BufferedWriter(osw);
//
//                //String str = h+":"+m+":"+s + "," + air + "," + rot + "," + height + "," +rud + "," + ele + "," + rudTemp + "," + eleTemp + "\r\n";
//                String str = cadence +"\n";
//                bw.write(str);
//                bw.flush();
//                bw.close();
//            } catch (Exception e) {
//                Log.e(TAG, "file write failed",e);
//            }

                //Arduinoへの出力
//                if (outputStream != null) {
//                    try {
//                        String tmpStr = "TEST PAYLOAD " + Long.toString(Thread.currentThread().getId()) + "\n";
//                        outputStream.write(tmpStr.getBytes());
//                    } catch (IOException e) {
//                        Log.e(TAG, "arduino write failed", e);
//                    }
//                }

            } catch (InterruptedException e) {}
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

    private synchronized boolean parseInputBuffer (FileInputStream inputStream) {
        try {
            buffer_in_len = inputStream.read(buffer);
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
                            return true;
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
        return false;
    }

    private void packVariables() {
        drawMapView.air = Float.parseFloat(df2.format((float)(composeInt32(in_payload[8], in_payload[7], in_payload[6], in_payload[5]) * 6.15180146890866e-7))); // 機速(10倍)
        drawMapView.cadence = (int)(composeInt32(in_payload[4], in_payload[3], in_payload[2], in_payload[1]) * 0.0009375); // 回転数
        drawMapView.altitude = Float.parseFloat(df2.format(composeInt16(in_payload[23],in_payload[22]) / 100.0f)); // 高度(100倍)

        drawMapView.rud_flag = in_payload[53];
        drawMapView.rud_torque_mode = in_payload[54];
        drawMapView.rud_pos = Float.parseFloat(df1.format((float)composeInt16(in_payload[56], in_payload[55]) * 0.1));
        drawMapView.rud_load = composeInt16(in_payload[58], in_payload[57]);
        drawMapView.rud_temp = composeInt16(in_payload[60], in_payload[59]);
        drawMapView.rud_volt = Float.parseFloat(df2.format((float)composeInt16(in_payload[62], in_payload[61]) * 0.01));
        drawMapView.ele_flag = in_payload[63];
        drawMapView.ele_torque_mode = in_payload[64];
        drawMapView.ele_pos = Float.parseFloat(df1.format( (float)composeInt16(in_payload[66], in_payload[65]) * 0.1));
        drawMapView.ele_load = composeInt16(in_payload[68], in_payload[67]);
        drawMapView.ele_temp = composeInt16(in_payload[70], in_payload[69]);
        drawMapView.ele_volt = Float.parseFloat(df2.format((float)composeInt16(in_payload[72], in_payload[61]) * 0.01));

        drawMapView.yaw = (composeInt16(in_payload[10], in_payload[9]) + 180) % 360;
        drawMapView.pitch = composeInt16(in_payload[14], in_payload[13]) / -100.0f;
        drawMapView.roll = composeInt16(in_payload[12], in_payload[11]) / -100.0f;
        drawMapView.yaw4log = drawMapView.yaw - 180;
        drawMapView.accelX = Float.parseFloat(df2.format(composeInt16(in_payload[16], in_payload[15]) / 100f));
        drawMapView.accelY = Float.parseFloat(df2.format(composeInt16(in_payload[18], in_payload[17]) / 100f));
        drawMapView.accelZ = Float.parseFloat(df2.format(composeInt16(in_payload[20], in_payload[19]) / 100f));

        drawMapView.calibAccel = (byte)(in_payload[21] & (byte)0x03);
        drawMapView.calibMag = (byte)((in_payload[21] >> 2) & (byte)0x03);
        drawMapView.calibGyro = (byte)((in_payload[21] >> 4) & (byte)0x03);
        drawMapView.calibSystem = (byte)((in_payload[21] >> 6) & (byte)0x03);

        drawMapView.longitude = Float.parseFloat(df7.format(composeInt32(in_payload[27], in_payload[26], in_payload[25], in_payload[24]) / 10000000.0f));
        drawMapView.latitude = Float.parseFloat(df7.format(composeInt32(in_payload[31], in_payload[30], in_payload[29], in_payload[28]) / 10000000.0f));
        drawMapView.ground = Float.parseFloat(df3.format(composeInt16(in_payload[33], in_payload[32]) / 1000f));
        drawMapView.satellites = in_payload[38];
        drawMapView.hdop = Float.parseFloat(df2.format(composeInt32(in_payload[42], in_payload[41], in_payload[40], in_payload[39]) / 100.0f));
        drawMapView.lng_error = Float.parseFloat(df1.format(composeInt16(in_payload[74], in_payload[73]) / 10f));
        drawMapView.lat_error = Float.parseFloat(df1.format(composeInt16(in_payload[76], in_payload[75]) / 10f));
        drawMapView.gps_altitude = Float.parseFloat(df2.format(composeInt32(in_payload[37], in_payload[36], in_payload[35], in_payload[34]) / 100f));
        drawMapView.gps_course = Float.parseFloat(df2.format(composeInt16(in_payload[44], in_payload[43]) / 100f));

        drawMapView.temperature = Float.parseFloat(df2.format(composeInt16(in_payload[46], in_payload[47]) / 100.0f));
        drawMapView.pressure = Float.parseFloat(df2.format(composeInt32(in_payload[50], in_payload[49], in_payload[48], in_payload[47]) / 100.0f));
        drawMapView.humidity = composeInt16(in_payload[52], in_payload[51]);

        drawMapView.updateMap(); // マップデータを更新

        drawMapView.logData();

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

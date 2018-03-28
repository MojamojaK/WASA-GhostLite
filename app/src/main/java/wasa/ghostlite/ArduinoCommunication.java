package wasa.ghostlite;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class ArduinoCommunication {

    private static final String ACTION_USB_PERMISSION = "com.android.wasa.ghostlite.USB_PERMISSION";
    private static final String TAG = MainActivity.class.getSimpleName();

    Activity mainActivity;
    private DrawMapView drawMapView;

    boolean threadRunning = false;
    private UsbManager usbManager;
    private PendingIntent permissionIntent;
    private boolean permissionRequestPending;

    private UsbAccessory usbAccessory;
    private ParcelFileDescriptor fileDescriptor;
    private FileInputStream inputStream;
    private OutputStream outputStream;

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(ACTION_USB_PERMISSION.equals(action)){
                synchronized (this){
                    UsbAccessory accessory =(UsbAccessory)intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    if(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)){
                        open(accessory);
                    } else {
                        Toast.makeText(mainActivity.getBaseContext(), "permission denied : " + accessory.toString(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "permission denied : " + accessory.toString());
                    }
                    permissionRequestPending = false;
                }
            } else if(UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)){
                UsbAccessory accessory = (UsbAccessory)intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                if(accessory != null && accessory.equals(usbAccessory)){
                    close();
                }
            }

        }
    };

    public ArduinoCommunication(final Activity mainActivity, DrawMapView drawMapView) {
        this.mainActivity = mainActivity;
        this.drawMapView = drawMapView;

        usbManager = (UsbManager)this.mainActivity.getSystemService(Context.USB_SERVICE);
        permissionIntent = PendingIntent.getBroadcast(this.mainActivity, 0, new Intent(ACTION_USB_PERMISSION), 0);

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        this.mainActivity.registerReceiver(usbReceiver, filter);
    }

    void open(UsbAccessory accessory) {
        fileDescriptor = usbManager.openAccessory(accessory);
        if(fileDescriptor != null){
            FileDescriptor fd = fileDescriptor.getFileDescriptor();
            inputStream = new FileInputStream(fd);
            outputStream = new FileOutputStream(fd);
            new Thread(new ArduinoCommunicationRunnable(inputStream, outputStream, drawMapView, this)).start();
            Toast.makeText(mainActivity.getBaseContext(), "Receiver Opened \n" + accessory.getDescription() + "\n", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Receiver Opened" + accessory);
        } else {
            Toast.makeText(mainActivity.getBaseContext(), "Failed to open the accessory", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Failed to open the accessory");
        }
    }

    void close(){
        threadRunning = false;
        try{
            if(fileDescriptor != null){
                fileDescriptor.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            inputStream = null;
            outputStream = null;
            fileDescriptor = null;
            usbAccessory = null;
        }
    }

    public void resume() {
        UsbAccessory[] accessories = usbManager.getAccessoryList();
        UsbAccessory accessory = (accessories == null ? null : accessories[0]);
        if(accessory != null){
            if(usbManager.hasPermission(accessory)){
                open(accessory);
            } else {
                synchronized (usbReceiver){
                    if(!permissionRequestPending){
                        usbManager.requestPermission(accessory, permissionIntent);
                        permissionRequestPending = true;
                    }
                }
            }
        } else {
            Toast.makeText(mainActivity.getBaseContext(), "accessory is null", Toast.LENGTH_SHORT).show();
            Log.d(TAG,"accessory is null");
        }
    }

    public void destroy() {
        this.mainActivity.unregisterReceiver(usbReceiver);
    }

    //byte値をint値に変換
    private static int composeInt(byte hi, byte lo){
        return (((hi & 0xff) << 8) | lo & 0xff );
    }
}

package com.example.led_java;

import io.flutter.embedding.android.FlutterActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

///
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
///

import com.led.sdk.Constants;
import com.led.sdk.LedSdk;
import com.led.sdk.ResultBean;
import com.led.sdk.callback.SendDataCallback;
import com.led.sdk.entity.Device;
import com.led.sdk.entity.SingleWork;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;


import androidx.annotation.NonNull;

import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;


public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "com.hodhod.led_example/led";
    private static final String TAG = MainActivity.class.getSimpleName();

    private LedSdk ledSdk;
    public static final int[] COLORS = new int[]{
            0XFFFFFF,
            0xFF0000,
            0xFF7F00,
            0xFFFF00,
            0x00FF00,
            0x00FFFF,
            0x0000FF,
            0xFF00FF};

    //    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        GeneratedPluginRegistrant.registerWith(this);
//    }
    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        registerReceiver();
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler(
                        (call, result) -> {
                            switch (call.method) {
                                case "connect":
                                    String macAddress = call.argument("macAddress");
                                    int connectResult = connect(macAddress);
                                    result.success(connectResult);
                                    break;
                                case "playText":
                                    String text = call.argument("text");
                                    String playRresult =
                                            playText(text);
                                    result.success(playRresult);
                                    break;
//                                case "playMonograph":
//
//                                    String playResultMono = playMonograph();
//                                    result.success(playResultMono);
//                                    break;
                                 case "playMonograph":
                                    byte[] imageData = call.argument("imageData");
                                    String playResultMono = playMonograph(imageData);
                                    result.success(playResultMono);
                                    break;
                                case "playGif":
//                                playGif();
                                    result.success(null);
                                    break;
                                case "playHistogram":
//                                playHistogram();
                                    result.success(null);
                                    break;
                                default:
                                    result.notImplemented();
                            }
                        }
                );
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        registerReceiver(bluetoothEventReceiver, filter);
    }

    private final BroadcastReceiver bluetoothEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.e(TAG, "STATE_OFF : Turn off the phone's Bluetooth");
                        disconnect();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.e(TAG, "STATE_TURNING_OFF : The phone's Bluetooth is turning off");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.e(TAG, "STATE_ON : Turn on the phone's Bluetooth");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.e(TAG, "STATE_TURNING_ON : The phone's Bluetooth is turned on");
                        break;
                }
            } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                System.out.println("BluetoothDevice.ACTION_ACL_DISCONNECTED");
                disconnect();
            }
        }
    };

//    LedSdk sdk = new LedSdk(this);

    private int connect(String mac) {
        if (ledSdk != null && ledSdk.isConnected()) {
            return -1;//"Already connected"; // "Already connected"
        }

        if (mac.isEmpty() || mac == null) {
            return -2; //"Empty MAC address"; // Invalid MAC address
        }
        ledSdk = new LedSdk(this);
        ledSdk.setSendDataCallback(new SendDataCallback() {
            @Override
            public void onProgress(ResultBean resultBean) {
                int progress = Integer.parseInt(String.valueOf(resultBean.getObj()));
                // Update progress here if needed
            }

            @Override
            public void onDone(ResultBean resultBean) {
                String content = "Result = " + (resultBean.getCode() == 0 ? "成功" : "失败");
                System.out.println("onDone = " + resultBean.getCode());

            }
        });
        int ret = ledSdk.connect(mac); // Connect to the device using MAC address
        return ret;
    }

    private String playText(String text) {
//        if (text.isEmpty() || text == null) return "Empty Text";
//        if (ledSdk == null || !ledSdk.isConnected()) return "Not connected";

        Device device = ledSdk.getDevice();
        int[] colors = randomColor(text);
        byte[] fontData = getFontData();
        SingleWork work = new SingleWork();
        work.setType(Constants.TYPE_TEXT);
        work.setText(text);
        work.setColors(colors);
        work.setDevice(device);
        work.setWidth(device.getCol());
        work.setHeight(device.getRow());
        work.setFontData(fontData);
        work.setKeepTime(200);
        ledSdk.playWork(this, work);

        return "Success";
    }

    public static int[] randomColor(String text) {
        int[] colors = new int[text.length()];
        for (int n = 0; n < text.length(); n++) {
            int random = new Random().nextInt(COLORS.length - 1);
            colors[n] = COLORS[random];
        }
        return colors;
    }

//    private byte[] getFontData() {
//        // Implement the getFontData method
//        return new byte[0];
//    }

    private String getFileName() {
        String fileName = "12x12.DZK";
        Device device = ledSdk == null ? null : ledSdk.getDevice();
        if (device == null)
            return fileName;

        if (device.getRow() == 16)
            fileName = "16x16.DZK";
        else if (device.getRow() == 22)
            fileName = "22x22.DZK";
        else if (device.getRow() == 32)
            fileName = "32x32.DZK";
        else if (device.getRow() == 64)
            fileName = "64x64.DZK";

        return fileName;
    }

    private byte[] getFontData() {
        try {
            String fileName = getFileName();
            InputStream inputStream = getAssets().open(fileName);
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);

            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void disconnect() {
        if (ledSdk != null)
            ledSdk.disconnect();

        ledSdk = null;
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int width, int height) {
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();

        float sx = (float) width / bitmapWidth;
        float sy = (float) height / bitmapHeight;

        Matrix matrix = new Matrix();
        matrix.postScale(sx, sy);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, true);
    }

    public static int[] getPixels(Bitmap img) {
        int w = img.getWidth();
        int h = img.getHeight();

        int[] data = new int[w * h];
        img.getPixels(data, 0, w, 0, 0, w, h);

        for (int n = 0; n < data.length; n++) {
            int pixel = data[n];
            data[n] = 16777215 & pixel;
        }

        return data;
    }

    public static int[] getBitmapPixels(Bitmap bitmap, int width, int height) {
        Bitmap img = scaleBitmap(bitmap, width, height);
        return getPixels(img);
    }

//    private static SingleWork newSingleWork(Bitmap bitmap, Device device) {
//        int[] data = getBitmapPixels(bitmap, device.getCol(), device.getRow());
//        SingleWork singleWork = new SingleWork();
//        singleWork.setDevice(device);
//        singleWork.setType(Constants.TYPE_GRAFFITI);
//        singleWork.setColors(data);
//        singleWork.setMode(Constants.MODE_SLIDE);
//        return singleWork;
//    }


//    private String playMonograph() {
//        try {
////            showProgress(0);
////
////            if (!check())
////                return;
//
//            Device device = ledSdk.getDevice();
//            Bitmap bitmap = BitmapFactory.decodeStream(getAssets().open("1.png"));
////            Bitmap bitmap = getBitmap("https://oss.spotled.xyz/pulbic/1622019969670wNJA.png");
//            SingleWork singleWork = newSingleWork(bitmap, device);
//
//            List<SingleWork> list = new ArrayList<>();
//            list.add(singleWork);
//            ledSdk.playWork(this, list);
//
//            return "Success!";
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "Failed!";
//        }
//
//    }


    private String playMonograph(byte[] imageData) {
        try {
            if (!check())
                return "Check Failed";

            if (imageData == null || imageData.length == 0) {
                return "No image data received";
            }

            Device device = ledSdk.getDevice();
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            SingleWork singleWork = newSingleWork(bitmap, device);

            List<SingleWork> list = new ArrayList<>();
            list.add(singleWork);
            ledSdk.playWork(this, list);

            return "Success!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed!";
        }
    }


    private static SingleWork newSingleWork(Bitmap bitmap, Device device) {
        int[] data = getBitmapPixels(bitmap, device.getCol(), device.getRow());
        SingleWork singleWork = new SingleWork();
        singleWork.setDevice(device);
        singleWork.setType(Constants.TYPE_GRAFFITI);
        singleWork.setColors(data);
        singleWork.setMode(Constants.MODE_SLIDE);
        return singleWork;
    }


    private boolean check() {


        if (ledSdk == null || !ledSdk.isConnected()) {
//            ToastUtils.show("No device connected");
            return false;
        }

        if (ledSdk.getDevice() == null) {
//            ToastUtils.show("Device information not obtained");
            return false;
        }
        return true;
    }

}

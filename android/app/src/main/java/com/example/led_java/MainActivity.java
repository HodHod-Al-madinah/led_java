package com.example.led_java;

import io.flutter.embedding.android.FlutterActivity;



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

import androidx.annotation.NonNull;
import java.util.Random;

import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;


public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "com.hodhod.led_example/led";
    private LedSdk ledSdk;
    public static final int[] COLORS = new int[] {
            0XFFFFFF,
            0xFF0000,
            0xFF7F00,
            0xFFFF00,
            0x00FF00,
            0x00FFFF,
            0x0000FF,
            0xFF00FF };

    //    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        GeneratedPluginRegistrant.registerWith(this);
//    }
    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
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
                                case "playMonograph":
//                                playMonograph();
                                    result.success(null);
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
        work.setWidth(96);
        work.setHeight(32);
        work.setFontData(fontData);
        work.setKeepTime(200);
        ledSdk.playWork(this, work);

        return getFileName();
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
}

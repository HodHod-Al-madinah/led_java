package com.example.led_java;

import io.flutter.embedding.android.FlutterActivity;

import com.led.sdk.LedSdk;
import com.led.sdk.ResultBean;
import com.led.sdk.callback.SendDataCallback;
import com.led.sdk.entity.Device;
import com.led.sdk.entity.SingleWork;

import androidx.annotation.NonNull;
import java.util.Random;

import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;


public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "com.hodhod.led_example/led";
    private LedSdk ledSdk;

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
        if (text.isEmpty() || text == null) return "Empty Text";
        if (ledSdk == null || !ledSdk.isConnected()) return "Not connected";

        Device device = ledSdk.getDevice();
        int[] colors = randomColor(text);
        byte[] fontData = getFontData();
        SingleWork work = new SingleWork();
        work.setType(com.led.sdk.Constants.TYPE_TEXT);
        work.setText(text);
        work.setColors(colors);
        work.setDevice(device);
        work.setWidth(64);
        work.setHeight(64);
        work.setFontData(fontData);
        work.setKeepTime(200);
        ledSdk.playWork(this, work);
        return "Success!";
    }

    private int[] randomColor(String text) {
        int[] colors = new int[text.length()];
        Random random = new Random();
        for (int i = 0; i < text.length(); i++) {
            colors[i] = Constants.COLORS[random.nextInt(Constants.COLORS.length)];
        }
        return colors;
    }

    private byte[] getFontData() {
        // Implement the getFontData method
        return new byte[0];
    }
    private void disconnect() {
        if (ledSdk != null)
            ledSdk.disconnect();

        ledSdk = null;
    }
}

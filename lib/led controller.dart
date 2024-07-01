import 'package:flutter/services.dart';
import 'package:led_java/generated/assets.dart';

class LedController {
  static const MethodChannel _channel =
      MethodChannel('com.hodhod.led_example/led');

  static Future<String> playMonograph() async {
    try {
      // Load image from assets
      ByteData byteData = await rootBundle.load(Assets.assets1);
      Uint8List imageData = byteData.buffer.asUint8List();

      // Send image data to native code
      final String result = await _channel
          .invokeMethod('playMonograph', {'imageData': imageData});
      return result;
    } catch (e) {
      print("Failed to play monograph: $e");
      return "Failed";
    }
  }
}

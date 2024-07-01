import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';
import 'package:flutter_smart_dialog/flutter_smart_dialog.dart';
import 'package:led_java/devices.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  static const platform = MethodChannel('com.hodhod.led_example/led');
  TextEditingController macAddressController = TextEditingController();
  TextEditingController textController = TextEditingController();
  GlobalKey formKey = GlobalKey();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("LED Screen Test"),
      ),
      body: Padding(
        padding: const EdgeInsets.all(10.0),
        child: Column(
          children: [
            TextFormField(
              onChanged: (val) {
                setState(() {
                  textController.text = val;
                });
              },
              decoration: InputDecoration(
                hintText: "Enter a Text",
                focusedBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(20.0),
                  borderSide: const BorderSide(
                    color: Colors.blue,
                  ),
                ),
                enabledBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(20.0),
                  borderSide: const BorderSide(
                    color: Colors.blue,
                  ),
                ),
              ),
              controller: textController,
            ),
            const SizedBox(height: 15),
            TextFormField(
              decoration: InputDecoration(
                hintText: "",
                focusedBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(20.0),
                  borderSide: const BorderSide(
                    color: Colors.blue,
                  ),
                ),
                enabledBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(20.0),
                  borderSide: const BorderSide(
                    color: Colors.blue,
                  ),
                ),
              ),
              controller: macAddressController,
            ),
            const SizedBox(height: 25),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                ElevatedButton(
                  // onPressed: _connectToLed,
                  onPressed: () => Navigator.of(context).push(
                      MaterialPageRoute(builder: (_) => const LedDevices())),
                  style: ButtonStyle(
                    backgroundColor: WidgetStateProperty.all(Colors.green),
                  ),
                  child: const Text(
                    "Connect !",
                    style: TextStyle(color: Colors.white),
                  ),
                ),
                ElevatedButton(
                  onPressed: _playText,
                  style: ButtonStyle(
                    backgroundColor: WidgetStateProperty.all(Colors.blue),
                  ),
                  child: const Text(
                    "Send Text",
                    style: TextStyle(color: Colors.white),
                  ),
                ),
                ElevatedButton(
                  onPressed: _monograph,
                  style: ButtonStyle(
                    backgroundColor: WidgetStateProperty.all(Colors.blue),
                  ),
                  child: const Text(
                    "Monograph",
                    style: TextStyle(color: Colors.white),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 25),
            Row(
              children: [
                ElevatedButton(
                  onPressed: () {},
                  style: ButtonStyle(
                    backgroundColor: WidgetStateProperty.all(Colors.blue),
                  ),
                  child: const Text(
                    "Histogram",
                    style: TextStyle(color: Colors.white),
                  ),
                ),
                const SizedBox(width: 25),
                ElevatedButton(
                  onPressed: () {},
                  style: ButtonStyle(
                    backgroundColor: WidgetStateProperty.all(Colors.blue),
                  ),
                  child: const Text(
                    "Gif",
                    style: TextStyle(color: Colors.white),
                  ),
                ),
                const SizedBox(height: 15),
              ],
            ),
            RepaintBoundary(
              key: formKey,
              child: Container(
                width: 96,
                height: 32,
                color: Colors.black,
                child: Center(
                  child: Text(
                    textController.text,
                    style: const TextStyle(
                      fontSize: 12,
                      color: Colors.white,
                    ),
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _connectToLed() async {
    try {
      final String macAddress = macAddressController.text;
      final int result =
          await platform.invokeMethod('connect', {'macAddress': macAddress});
      SmartDialog.showToast('Connect result: $result');
    } on PlatformException catch (e) {
      SmartDialog.showToast("Failed to connect to LED: '${e.message}'.");
    }
  }

  Future<void> _playText() async {
    try {
      final String text = textController.text;
      String result = await platform.invokeMethod('playText', {'text': text});
      // print('PlayText  result: $result');
      SmartDialog.showToast('$result');
    } on PlatformException catch (e) {
      SmartDialog.showToast("Failed to play text on LED: '${e.message}'.");
    }
  }

  // Future<void> _monographo() async {
  //   try {
  //     String result = await platform.invokeMethod('playMonograph');
  //     // print('PlayText  result: $result');
  //     SmartDialog.showToast(result);
  //   } on PlatformException catch (e) {
  //     SmartDialog.showToast("Failed to play text on LED: '${e.message}'.");
  //   }
  // }
  // Future<void> _monographo() async {
  //   try {
  //     // Load image from assets
  //     ByteData byteData = await rootBundle.load('assets/1.png');
  //     Uint8List imageData = byteData.buffer.asUint8List();
  //
  //     // Send image data to native code
  //     String result = await platform
  //         .invokeMethod('playMonograph', {'imageData': imageData});
  //     SmartDialog.showToast(result);
  //   } on PlatformException catch (e) {
  //     SmartDialog.showToast("Failed to play monograph on LED: '${e.message}'.");
  //   }
  // }
  Future<void> _monograph() async {
    try {
      // Capture the widget as an image
      ByteData? byteData = await _capturePng();
      if (byteData != null) {
        Uint8List imageData = byteData.buffer.asUint8List();

        // Send image data to native code
        String result = await platform
            .invokeMethod('playMonograph', {'imageData': imageData});
        SmartDialog.showToast(result);
      } else {
        SmartDialog.showToast("Failed to capture image.");
      }
    } on PlatformException catch (e) {
      SmartDialog.showToast("Failed to play monograph on LED: '${e.message}'.");
    }
  }

  Future<ByteData?> _capturePng() async {
    try {
      RenderRepaintBoundary boundary =
          formKey.currentContext!.findRenderObject() as RenderRepaintBoundary;
      ui.Image image = await boundary.toImage();
      ByteData? byteData =
          await image.toByteData(format: ui.ImageByteFormat.png);
      return byteData;
    } catch (e) {
      print(e);
      return null;
    }
  }
}

import 'package:flutter/material.dart';
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
                hintText: "17:42:0A:5B:5A:A0",
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
                  onPressed: () {},
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
              ],
            )
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
}

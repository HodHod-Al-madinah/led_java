import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_blue_plus/flutter_blue_plus.dart';
import 'package:flutter_smart_dialog/flutter_smart_dialog.dart';

class LedDevices extends StatefulWidget {
  const LedDevices({super.key});

  @override
  State<LedDevices> createState() => _LedDevicesState();
}

class _LedDevicesState extends State<LedDevices> {
  List<BluetoothDevice> devices = [];
  static const platform = MethodChannel('com.hodhod.led_example/led');

  @override
  void initState() {
    scanAvailableDevices();
    super.initState();
  }

  scanAvailableDevices() async {
    await FlutterBluePlus.startScan();

    FlutterBluePlus.scanResults.listen((results) {
      for (ScanResult result in results) {
        if (!devices.contains(result.device)) {
          setState(() {
            devices.add(result.device);
          });
        }
      }
    });
    // var subscription = FlutterBluePlus.onScanResults.listen(
    //   (results) {
    //     print("Scan results : $results");
    //     if (results.isNotEmpty) {
    //       ScanResult r = results.last;
    //       // devices.add(results);// the most recently found device
    //       print(
    //           '${r.device.remoteId}: "${r.advertisementData.advName}" found!');
    //     }
    //   },
    //   onError: (e) => print(e),
    // );

// cleanup: cancel subscription when scanning stops
//     FlutterBluePlus.cancelWhenScanComplete(subscription);

// Wait for Bluetooth enabled & permission granted
// In your real app you should use `FlutterBluePlus.adapterState.listen` to handle all states
//     await FlutterBluePlus.adapterState
//         .where((val) => val == BluetoothAdapterState.on)
//         .first;

// Start scanning w/ timeout
// Optional: use `stopScan()` as an alternative to timeout
//     await FlutterBluePlus.startScan(
//         withServices: [Guid("180D")], // match any of the specified services
//         withNames: ["Bluno"], // *or* any of the specified names
//         timeout: Duration(seconds: 15));

// wait for scanning to stop
//     await FlutterBluePlus.isScanning.where((val) => val == false).first;
  }

  Future<void> connectToDevice(BluetoothDevice device) async {
    await device.connect();
    print("Connected to device");
    // Once connected, you can perform operations on the device.
  }

  @override
  void dispose() {
    FlutterBluePlus.stopScan();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("LED Devices"),
      ),
      body: Container(
        margin: const EdgeInsets.all(15),
        child: Column(
          children: [
            Expanded(
              child: ListView.builder(
                shrinkWrap: true,
                itemCount: devices.length,
                itemBuilder: (BuildContext context, int index) {
                  return ListTile(
                    title: Text(devices[index].platformName),
                    subtitle: Text(devices[index].remoteId.toString()),
                    trailing: ElevatedButton(
                      onPressed: () async {
                        // await connectToDevice(devices[index]);
                        _connectToLed(devices[index].remoteId.toString());
                      },
                      style: ButtonStyle(
                        backgroundColor: WidgetStateProperty.all(Colors.green),
                      ),
                      child: const Text(
                        "Connect",
                        style: TextStyle(color: Colors.white),
                      ),
                    ),
                  );
                },
              ),
            ),
            ElevatedButton(
              onPressed: scanAvailableDevices,
              style: ButtonStyle(
                backgroundColor: WidgetStateProperty.all(Colors.green),
              ),
              child: const Text(
                "Scan",
                style: TextStyle(color: Colors.white),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _connectToLed(String macAddress) async {
    try {
      final int result =
          await platform.invokeMethod('connect', {'macAddress': macAddress});
      SmartDialog.showToast('Connect result: $result');
    } on PlatformException catch (e) {
      SmartDialog.showToast("Failed to connect to LED: '${e.message}'.");
    }
  }
}

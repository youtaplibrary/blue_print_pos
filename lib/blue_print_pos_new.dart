import 'dart:async';
import 'dart:developer';

import 'package:blue_print_pos/models/models.dart';
import 'package:blue_print_pos/new/receipt/receipt.dart';
import 'package:esc_pos_utils_plus/esc_pos_utils.dart';
import 'package:fluetooth/fluetooth.dart';
import 'package:flutter/services.dart';
import 'package:image/image.dart' as img;

export 'package:esc_pos_utils_plus/esc_pos_utils.dart' show PaperSize;
export 'package:fluetooth/fluetooth.dart' show FluetoothDevice;

export 'models/models.dart';
export 'new/receipt/receipt.dart';

class BluePrintPos {
  static final BluePrintPos _instance = BluePrintPos();

  static BluePrintPos get instance => _instance;

  static const MethodChannel _channel = MethodChannel('blue_print_pos');

  static final PrinterFeatures _printerFeatures = PrinterFeatures();

  /// Register printer device name with its features.
  /// Example:
  /// ```dart
  /// BluePrintPos.addPrinterFeatures(<String, Set<PrinterFeature>>{
  ///   // The name of the device is from [FluetoothDevice.name]
  ///   const PrinterFeatureRule.allowFor('PRJ-80AT-BT'): {
  //      PrinterFeature.paperFullCut,
  //    },
  ///   // Allow all printers for this feature
  ///   PrinterFeatureRule.allowAll: {PrinterFeature.paperFullCut},
  /// });
  /// ```
  ///
  /// If [BluePrintPos.selectedDevice] name does not match, for example, for
  /// [PrinterFeature.paperFullCut], then when printing using `useCut`
  /// it will not produce any ESC command for paper full cut.
  static void addPrinterFeatures(PrinterFeatureMap features) {
    _printerFeatures.featureMap.addAll(features);
  }

  /// Check if the printer has the feature.
  ///
  /// This will return `true` if [feature] is allowed for the printer or
  /// if [feature] is allowed for all printers.
  static bool printerHasFeatureOf(String printerName, PrinterFeature feature) {
    return _printerFeatures.hasFeatureOf(printerName, feature);
  }

  /// State to get bluetooth is connected
  bool _isConnected = false;

  /// Getter value [_isConnected]
  bool get isConnected => _isConnected;

  FluetoothDevice? _selectedDevice;

  /// Selected device after connecting
  FluetoothDevice? get selectedDevice => _selectedDevice;

  /// return bluetooth device list, handler Android and iOS in [BlueScanner]
  Future<List<FluetoothDevice>> scan() {
    return Fluetooth().getAvailableDevices();
  }

  /// When connecting, reassign value [selectedDevice] from parameter [device]
  /// and if connection time more than [timeout]
  /// will return [ConnectionStatus.timeout]
  /// When connection success, will return [ConnectionStatus.connected]
  Future<ConnectionStatus> connect(
    FluetoothDevice device, {
    Duration timeout = const Duration(seconds: 5),
  }) async {
    try {
      final FluetoothDevice fDevice =
          await Fluetooth().connect(device.id).timeout(timeout);
      _selectedDevice = fDevice;
      _isConnected = true;
      return Future<ConnectionStatus>.value(ConnectionStatus.connected);
    } on Exception catch (error) {
      log('$runtimeType - Error $error');
      _isConnected = false;
      _selectedDevice = null;
      return Future<ConnectionStatus>.value(ConnectionStatus.timeout);
    }
  }

  /// To stop communication between bluetooth device and application
  Future<ConnectionStatus> disconnect() async {
    await Fluetooth().disconnect();
    _isConnected = false;
    _selectedDevice = null;
    return ConnectionStatus.disconnect;
  }

  Future<void> printReceiptText(
    ReceiptSectionText receiptSectionText, {
    int feedCount = 0,
    bool useCut = false,
    bool useRaster = false,
    double duration = 0,
    PaperSize paperSize = PaperSize.mm58,
    double? textScaleFactor,
    BatchPrintOptions? batchPrintOptions,
  }) async {
    log(receiptSectionText.getContent());

    final int contentLength = receiptSectionText.contentLength;

    final BatchPrintOptions batchOptions =
        batchPrintOptions ?? BatchPrintOptions.full;

    final Iterable<List<Object>> startEndIter =
        batchOptions.getStartEnd(contentLength);

    for (final List<Object> startEnd in startEndIter) {
      final ReceiptSectionText section = receiptSectionText.getSection(
        startEnd[0] as int,
        startEnd[1] as int,
      );

      final bool isEndOfBatch = startEnd[2] as bool;
      final Uint8List? bytes =
          await convertTextToBytes(content: section.getContent());

      if (bytes == null) {
        return;
      }

      final List<int> byteBuffer = await _getBytesFromEscPos(
        List<int>.from(bytes),
        paperSize: paperSize,
        feedCount: isEndOfBatch ? feedCount : batchOptions.feedCount,
        useCut: isEndOfBatch ? useCut : batchOptions.useCut,
      );

      await _printProcess(byteBuffer);
    }
  }

  Future<String?> convertImageToString(String image, {int? width}) async {
    final String? result = await getImageHexadecimal(
      content: image,
      width: width,
    );
    return result;
  }

  /// This method only for print image with parameter [bytes] in List<int>
  /// define [width] to custom width of image, default value is 120
  /// [feedCount] to create more space after printing process done
  /// [useCut] to cut printing process
  Future<void> printReceiptImage(
    List<int> bytes, {
    int width = 120,
    int feedCount = 0,
    bool useCut = false,
    bool useRaster = false,
    PaperSize paperSize = PaperSize.mm58,
  }) async {
    final List<int> byteBuffer = await _getBytes(
      bytes,
      customWidth: width,
      feedCount: feedCount,
      useCut: useCut,
      useRaster: useRaster,
      paperSize: paperSize,
    );
    await _printProcess(byteBuffer);
  }

  /// This method only for print QR, only pass value on parameter [data]
  /// define [size] to size of QR, default value is 120
  /// [feedCount] to create more space after printing process done
  /// [useCut] to cut printing process
  Future<void> printQR(
    String data, {
    int size = 120,
    int feedCount = 0,
    bool useCut = false,
  }) async {
    final String content = "[C]<qrcode size='20'>$data</qrcode>";
    final Uint8List? byteBuffer = await convertTextToBytes(content: content);
    if (byteBuffer == null) {
      return;
    }

    await _printProcess(byteBuffer);
  }

  /// Reusable method for print text, image or QR based value [byteBuffer]
  /// Handler Android or iOS will use method writeBytes from ByteBuffer
  /// But in iOS more complex handler using service and characteristic
  Future<void> _printProcess(List<int> byteBuffer) async {
    try {
      if (!await Fluetooth().isConnected) {
        _isConnected = false;
        _selectedDevice = null;
        return;
      }
      await Fluetooth().sendBytes(byteBuffer);
    } on Exception catch (error) {
      log('$runtimeType - Error $error');
    }
  }

  /// This method to convert byte from [data] into as image canvas.
  /// It will automatically set width and height based [paperSize].
  /// [customWidth] to print image with specific width
  /// [feedCount] to generate byte buffer as feed in receipt.
  /// [useCut] to cut of receipt layout as byte buffer.
  Future<List<int>> _getBytes(
    List<int> data, {
    PaperSize paperSize = PaperSize.mm58,
    int customWidth = 0,
    int feedCount = 0,
    bool useCut = false,
    bool useRaster = false,
  }) async {
    List<int> bytes = <int>[];
    final CapabilityProfile profile = await CapabilityProfile.load();
    final Generator generator = Generator(paperSize, profile);
    final img.Image _resize = img.copyResize(
      img.decodeImage(data)!,
      width: customWidth > 0 ? customWidth : paperSize.width,
    );
    final bool canFullCut = printerHasFeatureOf(
      _selectedDevice!.name,
      PrinterFeature.paperFullCut,
    );
    if (useRaster) {
      bytes += generator.imageRaster(_resize);
    } else {
      bytes += generator.image(_resize);
    }
    if (feedCount > 0) {
      bytes += generator.feed(feedCount);
    }
    if (useCut && canFullCut) {
      bytes += generator.cut();
    }
    return bytes;
  }

  /// This method to convert byte from [data] into as image canvas.
  /// It will automatically set width and height based [paperSize].
  /// [customWidth] to print image with specific width
  /// [feedCount] to generate byte buffer as feed in receipt.
  /// [useCut] to cut of receipt layout as byte buffer.
  Future<List<int>> _getBytesFromEscPos(
    List<int> data, {
    PaperSize paperSize = PaperSize.mm58,
    int feedCount = 0,
    bool useCut = false,
  }) async {
    List<int> bytes = data;
    final CapabilityProfile profile = await CapabilityProfile.load();
    final Generator generator = Generator(paperSize, profile);

    final bool canFullCut = printerHasFeatureOf(
      _selectedDevice!.name,
      PrinterFeature.paperFullCut,
    );

    if (feedCount > 0) {
      bytes += generator.feed(feedCount);
    }
    if (useCut && canFullCut) {
      bytes += generator.cut();
    }
    return bytes;
  }

  static Future<Uint8List?> convertTextToBytes({
    required String content,
  }) async {
    final Map<String, dynamic> arguments = <String, dynamic>{
      'content': content,
    };
    Uint8List? results = Uint8List.fromList(<int>[]);
    try {
      results =
          await _channel.invokeMethod<Uint8List>('parseTextToBytes', arguments);
      if (results != null) {
        return results;
      }
    } on Exception catch (e) {
      log('[method:parseTextToBytes]: $e');
      throw Exception('Error: $e');
    }
    return null;
  }

  static Future<String?> getImageHexadecimal({
    required String content,
    int? width,
  }) async {
    final Map<String, dynamic> arguments = <String, dynamic>{
      'content': content,
      'width': width,
    };
    String? results;
    try {
      results = await _channel.invokeMethod<String>(
          'convertImageToHexadecimal', arguments);
    } on Exception catch (e) {
      log('[method:parserTextToBytes]: $e');
      throw Exception('Error: $e');
    }
    return results;
  }
}

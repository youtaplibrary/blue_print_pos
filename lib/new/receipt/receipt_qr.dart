import 'package:blue_print_pos/blue_print_pos_new.dart';

class ReceiptQR {
  ReceiptQR(
    this.data, {
    this.size = 20,
  });

  final String data;
  final int size;

  int get mm => BluePrintPos.pixelToMM(size);
  String get html => "[C]<qrcode size='$mm'>$data</qrcode>\n";
}

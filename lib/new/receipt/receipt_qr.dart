class ReceiptQR {
  ReceiptQR(
    this.data, {
    this.size = 20,
  });

  final String data;
  final int size;

  String get html => "[C]<qrcode size='$size'>$data</qrcode>\n";
}

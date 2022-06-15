class ReceiptPixelSpace {
  const ReceiptPixelSpace({
    required this.pixels,
  }) : assert(pixels > 0);

  final int pixels;

  String get html {
    return '<div style="width:1px;height:${pixels}px"></div>';
  }
}

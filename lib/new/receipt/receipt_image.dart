import 'package:blue_print_pos/blue_print_pos_new.dart';

import 'collection_style.dart';

class ReceiptImage {
  ReceiptImage(
    this.data, {
    this.alignment = ReceiptAlignment.center,
    this.width = 120,
  });

  final String data;
  final int width;
  final ReceiptAlignment alignment;

  String get html =>
      _alignmentStyle + '<img ' + "size='$width'>" + data + '</img>\n';

  String get _alignmentStyle {
    if (alignment == ReceiptAlignment.left) {
      return CollectionStyle.textLeft;
    } else if (alignment == ReceiptAlignment.right) {
      return CollectionStyle.textRight;
    }
    return CollectionStyle.textCenter;
  }
}

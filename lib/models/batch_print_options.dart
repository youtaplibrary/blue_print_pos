import 'dart:math' as math;

class BatchPrintOptions {
  const BatchPrintOptions(
    int iterations, {
    this.delay = Duration.zero,
  })  : _iterations = iterations,
        _n = null;

  const BatchPrintOptions.perNContent(
    int n, {
    this.delay = Duration.zero,
  })  : _n = n,
        _iterations = null;

  static const BatchPrintOptions full = BatchPrintOptions(1);

  final int? _iterations;

  final int? _n;

  /// Delay between each print
  final Duration delay;

  Iterable<List<int>> getStartEnd(int contentLength) sync* {
    final int iterations = _iterations ?? contentLength ~/ _n!;
    final int subListLength = _n ?? contentLength ~/ iterations;
    int start = 0;
    int end = 0;
    for (int i = 0; i < iterations; i++) {
      end += subListLength;
      end = math.min(end, contentLength);
      yield <int>[start, end];
      start = end;
    }
  }
}

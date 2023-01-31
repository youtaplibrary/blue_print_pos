typedef PrinterFeatureMap = Map<String, Set<PrinterFeature>>;

enum PrinterFeature {
  paperFullCut,
}

class PrinterFeatures {
  final PrinterFeatureMap featureMap = <String, Set<PrinterFeature>>{};

  bool hasFeatureOf(String printerName, PrinterFeature feature) {
    return featureMap[printerName]?.contains(feature) == true;
  }
}

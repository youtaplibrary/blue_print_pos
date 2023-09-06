package com.ayeee.blue_print_pos.barcode;

import com.ayeee.blue_print_pos.EscPosPrinterCommands;
import com.ayeee.blue_print_pos.EscPosPrinterSize;
import com.ayeee.blue_print_pos.exceptions.EscPosBarcodeException;

public class BarcodeEAN13 extends BarcodeNumber {

    public BarcodeEAN13(EscPosPrinterSize printerSize, String code, float widthMM, float heightMM, int textPosition) throws EscPosBarcodeException {
        super(printerSize, EscPosPrinterCommands.BARCODE_TYPE_EAN13, code, widthMM, heightMM, textPosition);
    }

    @Override
    public int getCodeLength() {
        return 13;
    }
}

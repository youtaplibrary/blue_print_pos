package com.ayeee.blue_print_pos.textparser;

import com.ayeee.blue_print_pos.EscPosPrinter;
import com.ayeee.blue_print_pos.EscPosPrinterCommands;
import com.ayeee.blue_print_pos.barcode.Barcode;
import com.ayeee.blue_print_pos.barcode.Barcode128;
import com.ayeee.blue_print_pos.barcode.BarcodeEAN13;
import com.ayeee.blue_print_pos.barcode.BarcodeEAN8;
import com.ayeee.blue_print_pos.barcode.BarcodeUPCA;
import com.ayeee.blue_print_pos.barcode.BarcodeUPCE;
import com.ayeee.blue_print_pos.exceptions.EscPosBarcodeException;
import com.ayeee.blue_print_pos.exceptions.EscPosEncodingException;
import com.ayeee.blue_print_pos.exceptions.EscPosParserException;

import java.util.Hashtable;

public class PrinterTextParserBarcode implements IPrinterTextParserElement {

    private final Barcode barcode;
    private final int length;
    private byte[] align;

    public PrinterTextParserBarcode(PrinterTextParserColumn printerTextParserColumn,
                                    String textAlign,
                                    Hashtable<String, String> barcodeAttributes,
                                    String code) throws EscPosParserException, EscPosBarcodeException {

        EscPosPrinter printer = printerTextParserColumn.getLine().getTextParser().getPrinter();
        code = code.trim();

        this.align = EscPosPrinterCommands.TEXT_ALIGN_LEFT;
        switch (textAlign) {
            case PrinterTextParser.TAGS_ALIGN_CENTER:
                this.align = EscPosPrinterCommands.TEXT_ALIGN_CENTER;
                break;
            case PrinterTextParser.TAGS_ALIGN_RIGHT:
                this.align = EscPosPrinterCommands.TEXT_ALIGN_RIGHT;
                break;
        }

        this.length = printer.getPrinterNbrCharactersPerLine();
        float height = 10f;

        if (barcodeAttributes.containsKey(PrinterTextParser.ATTR_BARCODE_HEIGHT)) {
            String barCodeAttribute = barcodeAttributes.get(PrinterTextParser.ATTR_BARCODE_HEIGHT);

            if (barCodeAttribute == null) {
                throw new EscPosParserException("Invalid barcode attribute: " + PrinterTextParser.ATTR_BARCODE_HEIGHT);
            }

            try {
                height = Float.parseFloat(barCodeAttribute);
            } catch (NumberFormatException nfe) {
                throw new EscPosParserException("Invalid barcode " + PrinterTextParser.ATTR_BARCODE_HEIGHT + " value");
            }
        }

        float width = 0f;
        if (barcodeAttributes.containsKey(PrinterTextParser.ATTR_BARCODE_WIDTH)) {
            String barCodeAttribute = barcodeAttributes.get(PrinterTextParser.ATTR_BARCODE_WIDTH);

            if (barCodeAttribute == null) {
                throw new EscPosParserException("Invalid barcode attribute: " + PrinterTextParser.ATTR_BARCODE_WIDTH);
            }

            try {
                width = Float.parseFloat(barCodeAttribute);
            } catch (NumberFormatException nfe) {
                throw new EscPosParserException("Invalid barcode " + PrinterTextParser.ATTR_BARCODE_WIDTH + " value");
            }
        }

        int textPosition = EscPosPrinterCommands.BARCODE_TEXT_POSITION_BELOW;
        if (barcodeAttributes.containsKey(PrinterTextParser.ATTR_BARCODE_TEXT_POSITION)) {
            String barCodeAttribute = barcodeAttributes.get(PrinterTextParser.ATTR_BARCODE_TEXT_POSITION);

            if (barCodeAttribute == null) {
                throw new EscPosParserException("Invalid barcode attribute: " + PrinterTextParser.ATTR_BARCODE_TEXT_POSITION);
            }

            switch (barCodeAttribute) {
                case PrinterTextParser.ATTR_BARCODE_TEXT_POSITION_NONE:
                    textPosition = EscPosPrinterCommands.BARCODE_TEXT_POSITION_NONE;
                    break;
                case PrinterTextParser.ATTR_BARCODE_TEXT_POSITION_ABOVE:
                    textPosition = EscPosPrinterCommands.BARCODE_TEXT_POSITION_ABOVE;
                    break;
            }
        }

        String barcodeType = PrinterTextParser.ATTR_BARCODE_TYPE_EAN13;

        if (barcodeAttributes.containsKey(PrinterTextParser.ATTR_BARCODE_TYPE)) {
            barcodeType = barcodeAttributes.get(PrinterTextParser.ATTR_BARCODE_TYPE);

            if (barcodeType == null) {
                throw new EscPosParserException("Invalid barcode attribute : " + PrinterTextParser.ATTR_BARCODE_TYPE);
            }
        }

        switch (barcodeType) {
            case PrinterTextParser.ATTR_BARCODE_TYPE_EAN8:
                this.barcode = new BarcodeEAN8(printer, code, width, height, textPosition);
                break;
            case PrinterTextParser.ATTR_BARCODE_TYPE_EAN13:
                this.barcode = new BarcodeEAN13(printer, code, width, height, textPosition);
                break;
            case PrinterTextParser.ATTR_BARCODE_TYPE_UPCA:
                this.barcode = new BarcodeUPCA(printer, code, width, height, textPosition);
                break;
            case PrinterTextParser.ATTR_BARCODE_TYPE_UPCE:
                this.barcode = new BarcodeUPCE(printer, code, width, height, textPosition);
                break;
            case PrinterTextParser.ATTR_BARCODE_TYPE_128:
                this.barcode = new Barcode128(printer, code, width, height, textPosition);
                break;
            default:
                throw new EscPosParserException("Invalid barcode attribute : " + PrinterTextParser.ATTR_BARCODE_TYPE);
        }
    }

    /**
     * Get the barcode width in char length.
     *
     * @return int
     */
    @Override
    public int length() throws EscPosEncodingException {
        return this.length;
    }

    /**
     * Print barcode
     *
     * @param printerSocket Instance of EscPosPrinterCommands
     */
    @Override
    public void print(EscPosPrinterCommands printerSocket) {
        printerSocket
                .setAlign(this.align)
                .printBarcode(this.barcode);
    }
}
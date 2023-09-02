package com.ayeee.blue_print_pos.textparser;

import com.ayeee.blue_print_pos.EscPosCharsetEncoding;
import com.ayeee.blue_print_pos.EscPosPrinter;
import com.ayeee.blue_print_pos.EscPosPrinterCommands;
import com.ayeee.blue_print_pos.exceptions.EscPosEncodingException;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class PrinterTextParserString implements IPrinterTextParserElement {
    private final EscPosPrinter printer;
    private final String text;
    private final byte[] textSize;
    private final byte[] textColor;
    private final byte[] textReverseColor;
    private final byte[] textBold;
    private final byte[] textUnderline;
    private final byte[] textDoubleStrike;

    public PrinterTextParserString(PrinterTextParserColumn printerTextParserColumn, String text, byte[] textSize, byte[] textColor, byte[] textReverseColor, byte[] textBold, byte[] textUnderline, byte[] textDoubleStrike) {
        this.printer = printerTextParserColumn.getLine().getTextParser().getPrinter();
        this.text = text;
        this.textSize = textSize;
        this.textColor = textColor;
        this.textReverseColor = textReverseColor;
        this.textBold = textBold;
        this.textUnderline = textUnderline;
        this.textDoubleStrike = textDoubleStrike;
    }

    @Override
    public int length() throws EscPosEncodingException {
        EscPosCharsetEncoding charsetEncoding = this.printer.getEncoding();

        int coef = 1;
        if(Arrays.equals(this.textSize, EscPosPrinterCommands.TEXT_SIZE_DOUBLE_WIDTH) || Arrays.equals(this.textSize, EscPosPrinterCommands.TEXT_SIZE_BIG))
            coef = 2;
        else if(Arrays.equals(this.textSize, EscPosPrinterCommands.TEXT_SIZE_BIG_2))
            coef = 3;
        else if(Arrays.equals(this.textSize, EscPosPrinterCommands.TEXT_SIZE_BIG_3))
            coef = 4;
        else if(Arrays.equals(this.textSize, EscPosPrinterCommands.TEXT_SIZE_BIG_4))
            coef = 5;
        else if(Arrays.equals(this.textSize, EscPosPrinterCommands.TEXT_SIZE_BIG_5))
            coef = 6;
        else if(Arrays.equals(this.textSize, EscPosPrinterCommands.TEXT_SIZE_BIG_6))
            coef = 7;

        if (charsetEncoding != null) {
            try {
                return this.text.getBytes(charsetEncoding.getName()).length * coef;
            } catch (UnsupportedEncodingException e) {
                throw new EscPosEncodingException(e.getMessage());
            }
        }

        return this.text.length() * coef;
    }

    /**
     * Print text
     *
     * @param printerSocket Instance of EscPosPrinterCommands
     */
    @Override
    public void print(EscPosPrinterCommands printerSocket) throws EscPosEncodingException {
        printerSocket.printText(this.text, this.textSize, this.textColor, this.textReverseColor, this.textBold, this.textUnderline, this.textDoubleStrike);
    }
}

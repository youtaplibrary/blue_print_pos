package com.ayeee.blue_print_pos.textparser;

import com.ayeee.blue_print_pos.EscPosPrinter;
import com.ayeee.blue_print_pos.EscPosPrinterCommands;
import com.ayeee.blue_print_pos.exceptions.EscPosBarcodeException;
import com.ayeee.blue_print_pos.exceptions.EscPosEncodingException;
import com.ayeee.blue_print_pos.exceptions.EscPosParserException;

public class PrinterTextParser {
    
    public static final String TAGS_ALIGN_LEFT = "L";
    public static final String TAGS_ALIGN_CENTER = "C";
    public static final String TAGS_ALIGN_RIGHT = "R";
    public static final String[] TAGS_ALIGN = {PrinterTextParser.TAGS_ALIGN_LEFT, PrinterTextParser.TAGS_ALIGN_CENTER, PrinterTextParser.TAGS_ALIGN_RIGHT};
    
    public static final String TAGS_IMAGE = "img";
    public static final String TAGS_BARCODE = "barcode";
    public static final String TAGS_QRCODE = "qrcode";

    public static final String ATTR_BARCODE_WIDTH = "width";
    public static final String ATTR_BARCODE_HEIGHT = "height";
    public static final String ATTR_BARCODE_TYPE = "type";
    public static final String ATTR_BARCODE_TYPE_EAN8 = "ean8";
    public static final String ATTR_BARCODE_TYPE_EAN13 = "ean13";
    public static final String ATTR_BARCODE_TYPE_UPCA = "upca";
    public static final String ATTR_BARCODE_TYPE_UPCE = "upce";
    public static final String ATTR_BARCODE_TYPE_128 = "128";
    public static final String ATTR_BARCODE_TEXT_POSITION = "text";
    public static final String ATTR_BARCODE_TEXT_POSITION_NONE = "none";
    public static final String ATTR_BARCODE_TEXT_POSITION_ABOVE = "above";
    public static final String ATTR_BARCODE_TEXT_POSITION_BELOW = "below";

    public static final String TAGS_FORMAT_TEXT_FONT = "font";
    public static final String TAGS_FORMAT_TEXT_BOLD = "b";
    public static final String TAGS_FORMAT_TEXT_UNDERLINE = "u";
    public static final String[] TAGS_FORMAT_TEXT = {PrinterTextParser.TAGS_FORMAT_TEXT_FONT, PrinterTextParser.TAGS_FORMAT_TEXT_BOLD, PrinterTextParser.TAGS_FORMAT_TEXT_UNDERLINE};

    public static final String ATTR_FORMAT_TEXT_UNDERLINE_TYPE = "type";
    public static final String ATTR_FORMAT_TEXT_UNDERLINE_TYPE_NORMAL = "normal";
    public static final String ATTR_FORMAT_TEXT_UNDERLINE_TYPE_DOUBLE = "double";

    public static final String ATTR_FORMAT_TEXT_FONT_SIZE = "size";
    public static final String ATTR_FORMAT_TEXT_FONT_SIZE_BIG = "big";
    public static final String ATTR_FORMAT_TEXT_FONT_SIZE_BIG_2 = "big-2";
    public static final String ATTR_FORMAT_TEXT_FONT_SIZE_BIG_3 = "big-3";
    public static final String ATTR_FORMAT_TEXT_FONT_SIZE_BIG_4 = "big-4";
    public static final String ATTR_FORMAT_TEXT_FONT_SIZE_BIG_5 = "big-5";
    public static final String ATTR_FORMAT_TEXT_FONT_SIZE_BIG_6 = "big-6";
    public static final String ATTR_FORMAT_TEXT_FONT_SIZE_TALL = "tall";
    public static final String ATTR_FORMAT_TEXT_FONT_SIZE_WIDE = "wide";
    public static final String ATTR_FORMAT_TEXT_FONT_SIZE_NORMAL = "normal";

    public static final String ATTR_FORMAT_TEXT_FONT_COLOR = "color";
    public static final String ATTR_FORMAT_TEXT_FONT_COLOR_BLACK = "black";
    public static final String ATTR_FORMAT_TEXT_FONT_COLOR_BG_BLACK = "bg-black";
    public static final String ATTR_FORMAT_TEXT_FONT_COLOR_RED = "red";
    public static final String ATTR_FORMAT_TEXT_FONT_COLOR_BG_RED = "bg-red";

    public static final String ATTR_QRCODE_SIZE = "size";

    public static final String ATTR_IMAGE_SIZE = "size";
    
    private static String regexAlignTags;
    public static String getRegexAlignTags() {
        if(PrinterTextParser.regexAlignTags == null) {
            StringBuilder regexAlignTags = new StringBuilder();
            for (int i = 0; i < PrinterTextParser.TAGS_ALIGN.length; i++) {
                regexAlignTags.append("|\\[").append(PrinterTextParser.TAGS_ALIGN[i]).append("\\]");
            }
            PrinterTextParser.regexAlignTags = regexAlignTags.substring(1);
        }
        return PrinterTextParser.regexAlignTags;
    }
    
    public static boolean isTagTextFormat(String tagName) {
        if (tagName.charAt(0) == '/') {
            tagName = tagName.substring(1);
        }
        
        for (String tag : PrinterTextParser.TAGS_FORMAT_TEXT) {
            if (tag.equals(tagName)) {
                return true;
            }
        }
        return false;
    }
    
    public static byte[][] arrayByteDropLast(byte[][] arr) {
        if (arr.length == 0) {
            return arr;
        }
        
        byte[][] newArr = new byte[arr.length - 1][];
        System.arraycopy(arr, 0, newArr, 0, newArr.length);
        
        return newArr;
    }
    
    public static byte[][] arrayBytePush(byte[][] arr, byte[] add) {
        byte[][] newArr = new byte[arr.length + 1][];
        System.arraycopy(arr, 0, newArr, 0, arr.length);
        newArr[arr.length] = add;
        return newArr;
    }
    
    
    
    
    private final EscPosPrinter printer;
    private byte[][] textSize = {EscPosPrinterCommands.TEXT_SIZE_NORMAL};
    private byte[][] textColor = {EscPosPrinterCommands.TEXT_COLOR_BLACK};
    private byte[][] textReverseColor = {EscPosPrinterCommands.TEXT_COLOR_REVERSE_OFF};
    private byte[][] textBold = {EscPosPrinterCommands.TEXT_WEIGHT_NORMAL};
    private byte[][] textUnderline = {EscPosPrinterCommands.TEXT_UNDERLINE_OFF};
    private byte[][] textDoubleStrike = {EscPosPrinterCommands.TEXT_DOUBLE_STRIKE_OFF};
    private String text = "";
    
    public PrinterTextParser(EscPosPrinter printer) {
        this.printer = printer;
    }
    
    public EscPosPrinter getPrinter() {
        return printer;
    }
    
    public PrinterTextParser setFormattedText(String text) {
        this.text = text;
        return this;
    }

    public byte[] getLastTextSize() {
        return this.textSize[this.textSize.length - 1];
    }

    public void addTextSize(byte[] newTextSize) {
        this.textSize = PrinterTextParser.arrayBytePush(this.textSize, newTextSize);
    }

    public void dropLastTextSize() {
        if (this.textSize.length > 1) {
            this.textSize = PrinterTextParser.arrayByteDropLast(this.textSize);
        }
    }

    public byte[] getLastTextColor() {
        return this.textColor[this.textColor.length - 1];
    }

    public void addTextColor(byte[] newTextColor) {
        this.textColor = PrinterTextParser.arrayBytePush(this.textColor, newTextColor);
    }

    public void dropLastTextColor() {
        if (this.textColor.length > 1) {
            this.textColor = PrinterTextParser.arrayByteDropLast(this.textColor);
        }
    }

    public byte[] getLastTextReverseColor() {
        return this.textReverseColor[this.textReverseColor.length - 1];
    }

    public void addTextReverseColor(byte[] newTextReverseColor) {
        this.textReverseColor = PrinterTextParser.arrayBytePush(this.textReverseColor, newTextReverseColor);
    }

    public void dropLastTextReverseColor() {
        if (this.textReverseColor.length > 1) {
            this.textReverseColor = PrinterTextParser.arrayByteDropLast(this.textReverseColor);
        }
    }
    
    public byte[] getLastTextBold() {
        return this.textBold[this.textBold.length - 1];
    }
    
    public void addTextBold(byte[] newTextBold) {
        this.textBold = PrinterTextParser.arrayBytePush(this.textBold, newTextBold);
    }

    public void dropTextBold() {
        if (this.textBold.length > 1) {
            this.textBold = PrinterTextParser.arrayByteDropLast(this.textBold);
        }
    }

    public byte[] getLastTextUnderline() {
        return this.textUnderline[this.textUnderline.length - 1];
    }

    public void addTextUnderline(byte[] newTextUnderline) {
        this.textUnderline = PrinterTextParser.arrayBytePush(this.textUnderline, newTextUnderline);
    }

    public void dropLastTextUnderline() {
        if (this.textUnderline.length > 1) {
            this.textUnderline = PrinterTextParser.arrayByteDropLast(this.textUnderline);
        }
    }

    public byte[] getLastTextDoubleStrike() {
        return this.textDoubleStrike[this.textDoubleStrike.length - 1];
    }

    public void addTextDoubleStrike(byte[] newTextDoubleStrike) {
        this.textDoubleStrike = PrinterTextParser.arrayBytePush(this.textDoubleStrike, newTextDoubleStrike);
    }

    public void dropLastTextDoubleStrike() {
        if (this.textDoubleStrike.length > 1) {
            this.textDoubleStrike = PrinterTextParser.arrayByteDropLast(this.textDoubleStrike);
        }
    }
    
    public PrinterTextParserLine[] parse() throws EscPosParserException, EscPosBarcodeException, EscPosEncodingException {
        String[] stringLines = this.text.split("\n|\r\n");
        PrinterTextParserLine[] lines = new PrinterTextParserLine[stringLines.length];
        int i = 0;
        for (String line : stringLines) {
            lines[i++] = new PrinterTextParserLine(this, line);
        }
        return lines;
    }
}

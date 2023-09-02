package com.ayeee.blue_print_pos.textparser;

import com.ayeee.blue_print_pos.EscPosPrinterCommands;
import com.ayeee.blue_print_pos.exceptions.EscPosConnectionException;
import com.ayeee.blue_print_pos.exceptions.EscPosEncodingException;

public interface IPrinterTextParserElement {
    int length() throws EscPosEncodingException;
    void print(EscPosPrinterCommands printerSocket) throws EscPosEncodingException, EscPosConnectionException;
}

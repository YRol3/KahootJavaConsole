package com.finalproject;

import java.io.IOException;
import java.io.OutputStream;

public interface Writable {
    void write(OutputStream outputStream) throws IOException;
}

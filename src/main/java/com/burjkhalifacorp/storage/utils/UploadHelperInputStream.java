package com.burjkhalifacorp.storage.utils;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

// Count uploaded file size and buffering file header for contentType
public class UploadHelperInputStream extends FilterInputStream {
    private final int MAX_BUFFER_SIZE = 64 * 1024;

    private long totalBytesCount = 0;
    private final ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream();
    private int bufferPos = 0;

    public UploadHelperInputStream(InputStream inputStream) {
        super(inputStream);
    }

    public long getTotalBytesCount() {
        return totalBytesCount;
    }

    public byte[] getHeaderBuffer() {
        return headerBuffer.toByteArray();
    }

    @Override
    public int read() throws IOException {
        int b = super.read();
        if (b != -1) {
            totalBytesCount++;

            if(bufferPos < MAX_BUFFER_SIZE) {
                headerBuffer.write(b);
            }
        }
        return b;
    }

    @Override
    public int read(@NotNull byte[] b, int off, int len) throws IOException {
        int bytesRead = super.read(b, off, len);
        if (bytesRead > 0) {
            totalBytesCount += bytesRead;

            if(bufferPos < MAX_BUFFER_SIZE) {
                int toBuffer = Math.min(MAX_BUFFER_SIZE - bufferPos, bytesRead);
                headerBuffer.write(b, off, toBuffer);
                bufferPos += toBuffer;
            }
        }
        return bytesRead;
    }
}

package com.downloader.internal.stream;

import android.content.Context;

import com.downloader.PRDownloader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileDownloadRandomAccessFile implements FileDownloadOutputStream {

    private final BufferedOutputStream out;
    private final FileDescriptor fd;
    private final RandomAccessFile randomAccess;

    private FileDownloadRandomAccessFile(File file) throws IOException {
        randomAccess = new RandomAccessFile(file, "rw");
        fd = randomAccess.getFD();
        FileOutputStream fileOutputStream = PRDownloader.getApplicationContext().openFileOutput(file.getName().replace(".temp", ""), Context.MODE_PRIVATE);
        out = new BufferedOutputStream(fileOutputStream);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    @Override
    public void flushAndSync() throws IOException {
        out.flush();
        fd.sync();
    }

    @Override
    public void close() throws IOException {
        out.close();
        randomAccess.close();
    }

    @Override
    public void seek(long offset) throws IOException {
        randomAccess.seek(offset);
    }

    @Override
    public void setLength(long totalBytes) throws IOException {
        randomAccess.setLength(totalBytes);
    }

    public static FileDownloadOutputStream create(File file) throws IOException {
        return new FileDownloadRandomAccessFile(file);
    }

}

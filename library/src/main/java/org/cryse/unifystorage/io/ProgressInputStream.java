package org.cryse.unifystorage.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ProgressInputStream extends FilterInputStream {
    private long length;
    private long readLength = 0;
    private boolean listenByPercent;
    private double lastRate = -1;

    private List<StreamProgressListener> listeners = Collections.synchronizedList(new LinkedList<StreamProgressListener>());

    public ProgressInputStream(InputStream in) {
        this(in, -1);
    }

    public ProgressInputStream(InputStream in, long length) {
        super(in);
        this.length = length;
    }

    public void addListener(StreamProgressListener listener) {
        listeners.add(listener);
    }

    public void removeListener(StreamProgressListener listener) {
        listeners.remove(listener);
    }

    public void removeAllListener() {
        listeners.clear();
    }

    @Override
    public int read() throws IOException {
        int ret = in.read();
        if (ret >= 0)
            readed(1);
        return ret;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int ret = in.read(b);
        if (ret > 0)
            readed(ret);
        return ret;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int ret = in.read(b, off, len);
        if (ret > 0)
            readed(ret);
        return ret;
    }

    @Override
    public long skip(long n) throws IOException {
        long ret = in.skip(n);
        if (n > 0)
            readed(n);
        return ret;
    }

    @Override
    public synchronized void mark(int readlimit) {
        throw new UnsupportedOperationException("Mark is unsupported by ProgressInputStream.");
    }

    @Override
    public synchronized void reset() throws IOException {
        throw new UnsupportedOperationException("Reset is unsupported by ProgressInputStream.");
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    //private static DecimalFormat rateFormat = new DecimalFormat("#.00");
    //private static DecimalFormat rateFormat = new DecimalFormat("#.0");

    private synchronized void readed(long byteNum) throws IOException {
        readLength += byteNum;
        long length = this.length >= 0 ? this.length : readLength + available();
        int rate = (int) Math.round(((double)readLength / (double)length) * 100f);
        //double rate = Double.parseDouble(rateFormat.format(readLength / (double) length));
        if (rate != lastRate) {
            synchronized (listeners) {
                for (StreamProgressListener listener : listeners)
                    listener.onProgress(this, readLength, length, rate);
                lastRate = rate;
            }
        }
    }

}

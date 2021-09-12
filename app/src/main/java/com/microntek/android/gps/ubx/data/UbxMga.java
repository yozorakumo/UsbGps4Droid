package com.microntek.android.gps.ubx.data;

import android.location.Location;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 *　MGA
 *
 * @author Kamabokoz
 */
public class UbxMga extends UbxData {

    private static final String LOG_TAG = UbxMga.class.getSimpleName();
    private static Queue<byte[]> mgaData = new ArrayDeque<>();

    public static void pushMgaData(byte[] data) {
        synchronized (mgaData) {
            mgaData.add(data);
        }
    }

    public static byte[] popMgaData() {
        synchronized (mgaData) {
            return mgaData.poll();
        }
    }

    public static int length() {
        synchronized (mgaData) {
            return mgaData.size();
        }
    }

    public UbxMga(byte[] data) {
        super(data);
    }

    @Override
    public boolean parse(Location fix) {
        return false;
    }

    @Override
    public long getITow() {
        return -1;
    }
}

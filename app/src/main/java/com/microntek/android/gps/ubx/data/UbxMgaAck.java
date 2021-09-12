package com.microntek.android.gps.ubx.data;

import android.location.Location;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 *　MGA-ACK
 *
 * @author Kamabokoz
 */
public class UbxMgaAck extends UbxData {

    private static final String LOG_TAG = UbxMgaAck.class.getSimpleName();
    public UbxMgaAck(byte[] data) {
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

package com.microntek.android.gps.ubx.data;

import android.location.Location;

/**
 *　ESF-RESETALG
 *
 * @author Kamabokoz
 */
public class UbxEsfResetAlg extends UbxData {

    private static final String LOG_TAG = UbxEsfResetAlg.class.getSimpleName();

    private static final byte[] base =
            {(byte) 0xB5, (byte) 0x62, (byte) 0x10, (byte) 0x13 // header
                    , (byte) 0x00, (byte) 0x00 // len
                    , (byte) 0x23, (byte) 0x79}; // checksum

    public UbxEsfResetAlg(byte[] data) {
        super(data);
    }

    public UbxEsfResetAlg() {
        byte[] data = (byte[])base.clone();
        this.data = data;
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

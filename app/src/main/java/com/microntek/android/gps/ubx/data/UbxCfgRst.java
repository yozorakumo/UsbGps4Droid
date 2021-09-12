package com.microntek.android.gps.ubx.data;

import android.location.Location;

/**
 * リセット用コマンド
 *
 * @author Kamabokoz
 */
public class UbxCfgRst extends UbxData {

    private static final String LOG_TAG = UbxCfgRst.class.getSimpleName();

    private static final byte[] base =
            {(byte) 0xB5, (byte) 0x62, (byte) 0x06, (byte) 0x04 // header
                    , (byte) 0x04, (byte) 0x00 // len
                    , (byte) 0xFF, (byte) 0xB9, (byte) 0x02, (byte) 0x00 // ColdStart
                    , (byte) 0xC8, (byte) 0x8F}; // checksum

    public UbxCfgRst(byte[] data) {
        super(data);
    }

    public UbxCfgRst() {
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

package com.microntek.android.gps.ubx.data;

import android.location.Location;

/**
 * NavX5を設定するためのメッセージ（送信用）
 *
 * @author Kamabokoz
 */
public class UbxCfgNavX5 extends UbxData {

    private static final String LOG_TAG = UbxCfgNavX5.class.getSimpleName();

    private static final byte[] base =
            {(byte) 0xB5, (byte) 0x62, (byte) 0x06, (byte) 0x23 // header
                    , (byte) 0x2C, (byte) 0x00 // len
                    , (byte) 0x03, (byte) 0x00, (byte) 0x4C, (byte) 0x66, (byte) 0xC0, (byte) 0x00
                    , (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0x18
                    , (byte) 0x0A, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
                    , (byte) 0x32, (byte) 0x08, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
                    , (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00
                    , (byte) 0x64, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
                    , (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00
                    , (byte) 0x00, (byte) 0x00// payload
                    , (byte) 0xFF, (byte) 0xFF}; // checksum

    public UbxCfgNavX5(byte[] data) {
        super(data);
    }

    public UbxCfgNavX5(int minCN) {
        byte[] data = (byte[])base.clone();
        data[18] = (byte)(minCN & 0xFF); // minC/N

        int[] chk = calcCheckSum(data, 2);
        data[base.length - 2] = (byte)chk[0];
        data[base.length - 1] = (byte)chk[1];
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

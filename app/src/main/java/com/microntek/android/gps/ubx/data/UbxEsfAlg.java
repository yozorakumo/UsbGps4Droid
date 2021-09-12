package com.microntek.android.gps.ubx.data;

import android.location.Location;
import android.os.Bundle;

/**
 *　ESF-ALG
 *
 * @author Kamabokoz
 */
public class UbxEsfAlg extends UbxData {

    private static final String LOG_TAG = UbxEsfAlg.class.getSimpleName();

    public UbxEsfAlg(byte[] data) {
        super(data);
    }

    @Override
    public boolean parse(Location fix) {
        Bundle bundle = fix.getExtras();

        int idx = IDX_LEN + 2 + 5; // flags Offset=5
        byte flags = data[idx];

        bundle.putInt("ESFALG_auto", (int) flags & 0x01);
        bundle.putInt("ESFALG_status", ((int)flags & 0x0E) >>> 1);

        idx = IDX_LEN + 2 + 8; // yaw Offset=8
        long yaw = byte2hex(data, idx, 4);
        bundle.putInt("ESFALG_yaw", (int) yaw);

        idx = IDX_LEN + 2 + 12; // pitch Offset=12
        long pitch = byte2hex(data, idx, 2);
        bundle.putInt("ESFALG_pitch", (int) pitch);

        idx = IDX_LEN + 2 + 14; // roll Offset=14
        long roll = byte2hex(data, idx, 2);
        bundle.putInt("ESFALG_roll", (int) roll);

        fix.setExtras(bundle);

        return true;
    }

    @Override
    public long getITow() {
        int idx = IDX_LEN + 2 + 0; // iTOW Offset=0
        long time = byte2hex(data, idx, 4);
        return time;
    }
}

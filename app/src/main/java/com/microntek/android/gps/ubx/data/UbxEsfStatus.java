package com.microntek.android.gps.ubx.data;

import android.location.Location;
import android.os.Bundle;

/**
 *　ESF-STATUS
 *
 * @author Kamabokoz
 */
public class UbxEsfStatus extends UbxData {

    private static final String LOG_TAG = UbxEsfStatus.class.getSimpleName();

    public UbxEsfStatus(byte[] data) {
        super(data);
    }

    @Override
    public boolean parse(Location fix) {
        int idx = IDX_LEN + 2 + 12; // fusionMode Offset=12
        long fusionMode = byte2hex(data, idx, 1);

        Bundle bundle = fix.getExtras();
        bundle.putInt(FUSION_STATUS_KEY, (int) fusionMode);

        idx = IDX_LEN + 2 + 15; // numSens Offset=15
        long numSens = byte2hex(data, idx, 1);
        bundle.putInt("ESF_NUM", (int) numSens);

        for(int i = 0; i < numSens; i++) {
            idx = IDX_LEN + 2 + 16 + (i * 4); // sensStatus1 Offset=16 + N * 4
            byte sensStatus1 = data[idx];
            bundle.putInt("ESF" + i + "_type", (int) sensStatus1 & 0x3F);
            bundle.putInt("ESF" + i + "_used", ((int)sensStatus1 & 0x40) >>> 6);
            bundle.putInt("ESF" + i + "_ready", ((int)sensStatus1 & 0x80) >>> 7);

            idx = IDX_LEN + 2 + 17 + (i * 4); // sensStatus2 Offset=17 + N * 4
            byte sensStatus2 = data[idx];
            bundle.putInt("ESF" + i + "_calibStatus", (int) sensStatus2 & 0x03);
            bundle.putInt("ESF" + i + "_timeStatus", ((int)sensStatus2 & 0x0C) >>> 2);

            idx = IDX_LEN + 2 + 18 + (i * 4); // freq Offset=18 + N * 4
            long freq = byte2hex(data, idx, 1);
            bundle.putInt("ESF" + i + "_freq", (int) freq);

            idx = IDX_LEN + 2 + 19 + (i * 4); // faults Offset=19 + N * 4
            byte faults = data[idx];
            bundle.putInt("ESF" + i + "_badMeas", (int) faults & 0x01);
            bundle.putInt("ESF" + i + "_badTTag", (int) faults & 0x02);
            bundle.putInt("ESF" + i + "_missingMeas", (int) faults & 0x04);
            bundle.putInt("ESF" + i + "_noisyMeas", (int) faults & 0x08);
        }

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

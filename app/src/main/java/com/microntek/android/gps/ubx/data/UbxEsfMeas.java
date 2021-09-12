package com.microntek.android.gps.ubx.data;

import android.location.Location;
import android.os.Bundle;

/**
 *　ESF-MEAS
 *
 * @author Kamabokoz
 */
public class UbxEsfMeas extends UbxData {

    private static final String LOG_TAG = UbxEsfMeas.class.getSimpleName();

    public UbxEsfMeas(byte[] data) {
        super(data);
    }

    @Override
    public boolean parse(Location fix) {
        int idx = IDX_LEN + 2 + 0; // timeTag Offset=0
        long timetag = byte2hex(data, idx, 4);

        Bundle bundle = fix.getExtras();

        boolean fromCan = false;

        idx = IDX_LEN + 2 + 4; // flags Offset=4
        byte numSens = (byte)(data[idx+1] >>> 3);

        for(int i = 0; i < numSens; i++) {
            idx = IDX_LEN + 2 + 8 + (i * 4); // data Offset=8 + N * 4 //TODO:タイムタグにも対応する
            byte senstype = data[idx+3];
            // speedの場合
            if(senstype == 11) {
                long sensdata = byte2hex(data, idx, 3);
                bundle.putDouble("ESF_MEAS_Speed", sensdata / 100.0);

            }

            if((senstype >= 8 && senstype <= 11))
                fromCan = true;
        }

        if(fromCan)
            bundle.putInt("ESF_MEAS_Timetag2", (int) timetag);
        else
            bundle.putInt("ESF_MEAS_Timetag1", (int) timetag);


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

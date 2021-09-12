package com.microntek.android.gps.ubx.data;

import android.location.Location;
import android.os.Bundle;

/**
 *　破損データ
 *
 * @author Kamabokoz
 */
public class UbxBroken extends UbxData {

    private static final String LOG_TAG = UbxBroken.class.getSimpleName();

    public UbxBroken() {
    }

    @Override
    public boolean parse(Location fix) {
        Bundle bundle = fix.getExtras();
        bundle.putInt("BrokenData", bundle.getInt("BrokenData", 0)+1);
        fix.setExtras(bundle);
        return false;
    }

    @Override
    public long getITow() {
        return -1;
    }

}

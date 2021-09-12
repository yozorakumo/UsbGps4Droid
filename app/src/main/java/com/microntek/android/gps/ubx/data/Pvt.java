package com.microntek.android.gps.ubx.data;

/**
 * 測位情報を扱うI/F
 *
 * @author Kamabokoz
 */
public interface Pvt {
    public long parseUbxTime();
    public boolean isFix();
}

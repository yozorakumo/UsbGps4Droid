package com.microntek.android.gps.ubx.data;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 *　HNR-PVT
 *
 * @author Kamabokoz
 */
public class UbxHnrPvt extends UbxData implements Pvt {

    private static final String LOG_TAG = UbxHnrPvt.class.getSimpleName();

    // スレッドセーブじゃないので取り扱いには注意
    static SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    private boolean enableHNR = false;
    private boolean enableSpeedParam = false;
    private boolean enableAccuracyParam = false;

    public UbxHnrPvt(byte[] data, boolean enableHNR, boolean enableSpeedParam, boolean enableAccuracyParam) {
        super(data);
        this.enableHNR = enableHNR;
        this.enableSpeedParam = enableSpeedParam;
        this.enableAccuracyParam = enableAccuracyParam;
    }

    @Override
    public boolean parse(Location fix) {
        if (enableHNR == false)
            return false;

        int idx = IDX_LEN + 2 + 0; // iTOW Offset=0
        long time = byte2hex(data, idx, 4);

        idx = IDX_LEN + 2 + 16; // gpsFix Offset=16
        byte status = data[idx];

        idx = IDX_LEN + 2 + 17; // flags Offset=17
        byte flags = data[idx];

        idx = IDX_LEN + 2 + 20; // lon Offset=20
        long lon = byte2hex(data, idx, 4);

        idx = IDX_LEN + 2 + 24; // lat Offset=24
        long lat = byte2hex(data, idx, 4);

        idx = IDX_LEN + 2 + 32; // hMSL Offset=32
        long height = byte2hex(data, idx, 4);

        idx = IDX_LEN + 2 + 36; // gSpeed Offset=36
        long speed = byte2hex(data, idx, 4);

        //idx = IDX_LEN + 2 + 44; // headMot Offset=48
        //long heading = byte2hex(data, idx, 4);

        idx = IDX_LEN + 2 + 48; // headVeh Offset=48
        long heading = byte2hex(data, idx, 4);

        idx = IDX_LEN + 2 + 52; // hAcc Offset=52
        long acc = byte2hex(data, idx, 4);

        idx = IDX_LEN + 2 + 56; // cAcc Offset=56
        long vAcc = byte2hex(data, idx, 4);

        idx = IDX_LEN + 2 + 60; // sAcc Offset=60
        long speedAcc = byte2hex(data, idx, 4);

        idx = IDX_LEN + 2 + 64; // headVeh Offset=64
        long headingAcc = byte2hex(data, idx, 4);

        fix.setLatitude((double) lat / 10000000);
        fix.setLongitude((double) lon / 10000000);

        if(enableAccuracyParam)
            fix.setAccuracy((float) acc / 1000);
        else
            fix.setAccuracy((float) 1);
        fix.setAltitude((double) height / 1000);

        if(enableSpeedParam) {
            fix.setSpeed((float) speed / 1000);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && enableAccuracyParam) {
                fix.setSpeedAccuracyMetersPerSecond((float)speedAcc / 1000);
            }
        }
        fix.setBearing((float) heading / 100000);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && enableAccuracyParam) {
            fix.setVerticalAccuracyMeters((float)vAcc / 1000);
            fix.setBearingAccuracyDegrees((float)headingAcc / 100000);
        }

        Bundle bundle = fix.getExtras();
        bundle.putFloat("PvtAccuracy", (float) (acc / 1000.0));
        bundle.putInt(FIX_STATUS_KEY, (int) status);
        //bundle.putInt(SBAS_STATUS_KEY, (int) flags & 0x02); DGNSSの状態とれない？
        bundle.putDouble("Speed", speed / 100.0);
        fix.setExtras(bundle);

        return true;
    }


    @Override
    public long parseUbxTime() {
        return parseUbxTime(data, IDX_LEN + 2 + 4);
    }

    private long parseUbxTime(byte[] ubx, int idx) {
        long timestamp = 0;

        // 年～ミリ秒
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%04d", byte2hex(ubx, idx, 2)));
        sb.append(String.format("%02d", byte2hex(ubx, idx+2, 1)));
        sb.append(String.format("%02d", byte2hex(ubx, idx+3, 1)));
        sb.append(String.format("%02d", byte2hex(ubx, idx+4, 1)));
        sb.append(String.format("%02d", byte2hex(ubx, idx+5, 1)));
        sb.append(String.format("%02d", byte2hex(ubx, idx+6, 1)));
        sb.append(String.format("%03d", (byte2hex(ubx, idx+8, 4) / 1000000)));

        String time = sb.toString();
        //log("time: " + time);

        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            if (time != null) {
                timestamp = fmt.parse(time).getTime();
            }
        } catch (ParseException e) {
            logError("Error while parsing UBX time", e);
        }
        log("Timestamp from gps = " + String.valueOf(timestamp) + " System clock says " + System.currentTimeMillis());
        return timestamp;
    }

    @Override
    public long getITow() {
        int idx = IDX_LEN + 2 + 0; // iTOW Offset=0
        long time = byte2hex(data, idx, 4);
        return time;
    }

    @Override
    public boolean isFix() {
        int idx = IDX_LEN + 2 + 16; // gpsFix Offset=16
        byte status = data[idx];
        return status != 0x00;
    }
}

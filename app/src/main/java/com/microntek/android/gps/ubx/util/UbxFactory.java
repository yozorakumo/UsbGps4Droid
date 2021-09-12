package com.microntek.android.gps.ubx.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.microntek.android.gps.ubx.data.*;
import com.microntek.android.gps.usb.provider.driver.USBGpsProviderService;

/**
 * UBXのバイナリデータを各メッセージに対応したインスタンスに変換
 *
 * @author Kamabokoz
 */
public class UbxFactory {
    private boolean enableHNR = false;
    private boolean enableSpeedParam = false;
    private boolean enableAccuracyParam = false;
    private Context context = null;

    public UbxFactory(Context context) {
        this.context = context;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        enableHNR = preferences.getBoolean(USBGpsProviderService.PREF_USE_HNR, false);
        enableSpeedParam = preferences.getBoolean(USBGpsProviderService.PREF_USE_SPEED, true);
        enableAccuracyParam = preferences.getBoolean(USBGpsProviderService.PREF_USE_ACCURACY, true);
    }

    public UbxData createBrokenUbx() {
        return new UbxBroken();
    }

    // byte配列を各UBXに変換
    public UbxData createUbx(byte[] data) {
        int cls = (int)UbxData.byte2hex(data, UbxData.IDX_CLASS, 1);
        int id = (int)UbxData.byte2hex(data, UbxData.IDX_ID, 1);

        switch(cls) {
            case 0x28: // UBX-HNR
                switch (id) {
                    case 0x00: // UBX-HNR-PVT
                        return new UbxHnrPvt(data, enableHNR, enableSpeedParam, enableAccuracyParam);
                }
                break;

            case 0x01: // UBX-NAV
                switch (id) {
                    case 0x07: //UBX-NAV-PVT
                        return new UbxNavPvt(data, enableSpeedParam, enableAccuracyParam);
                    case 0x09: //UBX-NAV-ODO
                        return new UbxNavOdo(data);
                    case 0x30: //UBX-NAV-SVINFO
                        return new UbxNavSvInfo(data, context);
                    case 0x42: //UBX-NAV-SLAS
                        return new UbxNavSlas(data);

                }
                break;

            case 0x10: // UBX-ESF
                switch (id) {
                    case 0x02: // UBX-ESF-MEAS
                        return new UbxEsfMeas(data);
                    case 0x10: // UBX-ESF-STATUS
                        return new UbxEsfStatus(data);
                    case 0x14: // UBX-ESF-ALG
                        return new UbxEsfAlg(data);

                }
                break;

            case 0x13: // UBX-MGA
                switch (id) {
                    case 0x60: // UBX-MGA-ACK
                        return new UbxMgaAck(data);

                }
                break;
        }
        // 未実装
        return new UbxNotImplement(data);
    }
}

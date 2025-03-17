package com.microntek.android.gps.usb.provider.driver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;

import com.microntek.android.gps.usb.provider.BuildConfig;
import com.microntek.android.gps.usb.provider.R;
import com.microntek.android.gps.usb.provider.ui.GpsInfoActivity;
import com.microntek.android.gps.usb.provider.ui.USBGpsSettingsFragment;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.Collections;

import static android.content.Intent.ACTION_SCREEN_OFF;

public class USBGpsProviderService extends Service implements USBGpsManager.UbxListener, LocationListener {

    private static final String TAG = USBGpsProviderService.class.getSimpleName();

    public static final String ACTION_START_TRACK_RECORDING = "com.microntek.android.gps.usb.provider.action.START_TRACK_RECORDING";
    public static final String ACTION_STOP_TRACK_RECORDING = "com.microntek.android.gps.usb.provider.action.STOP_TRACK_RECORDING";
    public static final String ACTION_START_GPS_PROVIDER = "com.microntek.android.gps.usb.provider.action.START_GPS_PROVIDER";
    public static final String ACTION_STOP_GPS_PROVIDER = "com.microntek.android.gps.usb.provider.action.STOP_GPS_PROVIDER";
    public static final String ACTION_CONFIGURE_SIRF_GPS = "com.microntek.android.gps.usb.provider.action.CONFIGURE_SIRF_GPS";
    public static final String ACTION_ENABLE_SIRF_GPS = "com.microntek.android.gps.usb.provider.action.ENABLE_SIRF_GPS";
    public static final String ACTION_RESET_ODO = "com.microntek.android.gps.usb.provider.action.RESET_ODO";
    public static final String ACTION_RESET_ALG = "com.microntek.android.gps.usb.provider.action.RESET_ALG";
    public static final String ACTION_SET_MOCK_LOCATION_METHOD = "com.microntek.android.gps.usb.provider.action.SET_MOCK_LOCATION_METHOD";
    public static final String EXTRA_MOCK_LOCATION_METHOD = "com.microntek.android.gps.usb.provider.extra.MOCK_LOCATION_METHOD";
    public static final String PREF_MOCK_LOCATION_METHOD = "mockLocationMethod";

    public static final String PREF_START_GPS_PROVIDER = "startGps";
    public static final String PREF_REPLACE_STD_GPS = "replaceStdtGps";
    public static final String PREF_MOCK_GPS_NAME = "mockGpsName";
    public static final String PREF_TRACK_RECORDING = "trackRecording";
    public static final String PREF_GPS_DEVICE_VENDOR_ID = "usbDeviceVendorId";
    public static final String PREF_GPS_DEVICE_PRODUCT_ID = "usbDeviceProductId";
    public static final String PREF_TOAST_LOGGING = "showToasts";
    public static final String PREF_TRACK_FILE_DIR = "trackFileDirectory";
    public static final String PREF_TRACK_FILE_PREFIX = "trackFilePrefix";
    public static final String PREF_GPS_DEVICE_SPEED = "gpsDeviceSpeed";
    public static final String PREF_SET_TIME = "setTime";
    public static final String PREF_SIRF_GPS = "sirfGps";
    public static final String PREF_FORCE_ENABLE_PROVIDER = "forceEnableProvider";
    public static final String PREF_SIRF_ENABLE_GGA = "sirfEnableGGA";
    public static final String PREF_SIRF_ENABLE_RMC = "sirfEnableRMC";
    public static final String PREF_SIRF_ENABLE_GLL = "sirfEnableGLL";
    public static final String PREF_SIRF_ENABLE_VTG = "sirfEnableVTG";
    public static final String PREF_SIRF_ENABLE_GSA = "sirfEnableGSA";
    public static final String PREF_SIRF_ENABLE_GSV = "sirfEnableGSV";
    public static final String PREF_SIRF_ENABLE_ZDA = "sirfEnableZDA";
    public static final String PREF_SIRF_ENABLE_STATIC_NAVIGATION = "sirfEnableStaticNavigation";
    public static final String PREF_SIRF_ENABLE_NMEA = "sirfEnableNMEA";
    public static final String PREF_SIRF_ENABLE_SBAS = "sirfEnableSBAS";
    public static final String PREF_USE_HNR = "useHNR";
    public static final String PREF_USE_SPEED = "useSpeed";
    public static final String PREF_USE_ACCURACY = "useAccuracy";
    public static final String PREF_UBX_HNR = "highNavRate";
    public static final String PREF_UBX_RESETODO = "resetOdo";
    public static final String PREF_UBX_ASSISTNOW_ONLINE = "AssistNowOnline";
    public static final String PREF_UBX_ASSISTNOW_OFFLINE = "AssistNowOffline";
    private boolean debugToasts = false;
    private String mockProvider = LocationManager.GPS_PROVIDER;
    private USBGpsManager gpsManager = null;
    private FileOutputStream writer = null;
    private boolean preludeWritten = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        debugToasts = sharedPreferences.getBoolean(PREF_TOAST_LOGGING, false);

        int vendorId = sharedPreferences.getInt(PREF_GPS_DEVICE_VENDOR_ID, USBGpsSettingsFragment.DEFAULT_GPS_VENDOR_ID);
        int productId = sharedPreferences.getInt(PREF_GPS_DEVICE_PRODUCT_ID, USBGpsSettingsFragment.DEFAULT_GPS_PRODUCT_ID);

        if (ACTION_START_GPS_PROVIDER.equals(intent.getAction())) {
            USBGpsManager gpsManager = null;
            if (gpsManager == null) {
                if (!sharedPreferences.getBoolean(PREF_REPLACE_STD_GPS, true)) {
                    mockProvider = sharedPreferences.getString(PREF_MOCK_GPS_NAME, getString(R.string.defaultMockGpsName));
                }
                gpsManager = new USBGpsManager(this, vendorId, productId, 3);
                boolean enabled = gpsManager.enable();

                if (enabled) {
                    String mockLocationMethod = sharedPreferences.getString(PREF_MOCK_LOCATION_METHOD, "legacy");
                    if (mockLocationMethod.equals("lsposed")) {
                        Set<String> allowedApps = sharedPreferences.getStringSet("lsposedAllowedApps", Collections.emptySet());
                        String currentPackageName = getApplicationContext().getPackageName();
                        if (!allowedApps.contains(currentPackageName)) {
                            log("LSposed: " + currentPackageName + " is not in the allowed list, not mocking location");
                            stopSelf();
                            return Service.START_NOT_STICKY;
                        }
                        log("LSposed: " + currentPackageName + " is in the allowed list, mocking location");
                        mockProvider = "lsposed";
                    }
                    gpsManager.enableMockLocationProvider(mockProvider);

                    PendingIntent launchIntent = PendingIntent.getActivity(this, 0, new Intent(this, GpsInfoActivity.class), PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    Notification notification = new NotificationCompat.Builder(this)
                            .setContentIntent(launchIntent)
                            .setSmallIcon(R.drawable.ic_stat_notify)
                            .setAutoCancel(true)
                            .setContentTitle(getString(R.string.foreground_service_started_notification_title))
                            .setContentText(getString(R.string.foreground_gps_provider_started_notification))
                            .build();

                    startForeground(R.string.foreground_gps_provider_started_notification, notification);
                    showToast(R.string.msg_gps_provider_started);

                    if (sharedPreferences.getBoolean(PREF_TRACK_RECORDING, false)) {
                        startTracking();
                    }
                } else {
                    stopSelf();
                }
            }
        } else if (ACTION_START_TRACK_RECORDING.equals(intent.getAction())) {
            startTracking();
        } else if (ACTION_STOP_TRACK_RECORDING.equals(intent.getAction())) {
            if (gpsManager != null) {
                gpsManager.removeNmeaListener(this);
                endTrack();
                showToast(this.getString(R.string.msg_nmea_recording_stopped));
            }
        } else if (ACTION_STOP_GPS_PROVIDER.equals(intent.getAction())) {
            stopSelf();
        } else if (ACTION_SET_MOCK_LOCATION_METHOD.equals(intent.getAction())) {
            String mockLocationMethod = intent.getStringExtra(EXTRA_MOCK_LOCATION_METHOD);
            log("Setting mock location method to: " + mockLocationMethod);

            if (gpsManager != null) {
                if (mockLocationMethod.equals("lsposed")) {
                    Set<String> allowedApps = sharedPreferences.getStringSet("lsposedAllowedApps", Collections.emptySet());
                    String currentPackageName = getApplicationContext().getPackageName();
                    if (!allowedApps.contains(currentPackageName)) {
                        log("LSposed: " + currentPackageName + " is not in the allowed list, not mocking location");
                        stopSelf();
                        return Service.START_NOT_STICKY;
                    }
                    log("LSposed: " + currentPackageName + " is in the allowed list, mocking location");
                    mockProvider = "lsposed";
                } else {
                    if (!sharedPreferences.getBoolean(PREF_REPLACE_STD_GPS, true)) {
                        mockProvider = sharedPreferences.getString(PREF_MOCK_GPS_NAME, getString(R.string.defaultMockGpsName));
                    } else {
                        mockProvider = LocationManager.GPS_PROVIDER;
                    }
                }
                gpsManager.disableMockLocationProvider();
                gpsManager.enableMockLocationProvider(mockProvider);
            }
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (gpsManager != null) {
            gpsManager.removeNmeaListener(this);
            gpsManager.disableMockLocationProvider();
            gpsManager.disable();
        }
        endTrack();
        super.onDestroy();
    }

    private void startTracking() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            beginTrack();
            gpsManager.addNmeaListener(this);
            showToast(R.string.msg_nmea_recording_started);
        } else {
            Toast.makeText(this, "UsbGps logger - No storage permission", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean hasPermission(String perm) {
        return (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, perm));
    }

    private void showToast(int messageId) {
        if (debugToasts) {
            Toast.makeText(this, messageId, Toast.LENGTH_SHORT).show();
        }
    }

    private void showToast(String message) {
        if (debugToasts) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private void beginTrack() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat fmt = new SimpleDateFormat("_yyyy-MM-dd_HH-mm-ss'.ubx'");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String trackDirName = sharedPreferences.getString(PREF_TRACK_FILE_DIR, this.getString(R.string.defaultTrackFileDirectory));
        String trackFilePrefix = sharedPreferences.getString(PREF_TRACK_FILE_PREFIX, this.getString(R.string.defaultTrackFilePrefix));
        java.io.File trackFile = new java.io.File(trackDirName, trackFilePrefix + fmt.format(new Date()));
        log("Writing the prelude of the NMEA file: " + trackFile.getAbsolutePath());
        java.io.File trackDir = trackFile.getParentFile();
        try {
            if ((!trackDir.mkdirs()) && (!trackDir.isDirectory())) {
                Log.e(TAG, "Error while creating parent dir of NMEA file: " + trackDir.getAbsolutePath());
            }
            writer = new FileOutputStream(trackFile);
            preludeWritten = true;
        } catch (IOException e) {
            Log.e(TAG, "Error while writing the prelude of the NMEA file: " + trackFile.getAbsolutePath(), e);
            stopSelf();
        }
    }

    private void endTrack() {
        if (writer != null) {
            log("Ending the NMEA file");
            preludeWritten = false;
            try {
                writer.close();
            } catch (IOException e) {
                log("IOException: " + e.getMessage());
            }
        }
    }

    private void addUbxLog(byte[] data) {
        if (!preludeWritten) {
            beginTrack();
        }
        if (writer != null) {
            try {
                writer.write(data);
            } catch (IOException e) {
                log("IOException: " + e.getMessage());
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        log("trying access IBinder");
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
        log("The GPS has been disabled.....stopping the NMEA tracker service.");
        stopSelf();
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onUbxReceived(byte[] data) {
        addUbxLog(data);
    }

    private void log(String message) {
        if (BuildConfig.DEBUG) Log.d(TAG, message);
    }

    public static class BootReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) && sharedPreferences.getBoolean(PREF_START_GPS_PROVIDER, false)) {
                Intent serviceIntent = new Intent(context, USBGpsProviderService.class);
                serviceIntent.setAction(ACTION_START_GPS_PROVIDER);
                context.startService(serviceIntent);
            } else if (ACTION_SCREEN_OFF.equals(intent.getAction())) {
                Intent serviceIntent = new Intent(context, USBGpsProviderService.class);
                serviceIntent.setAction(ACTION_STOP_GPS_PROVIDER);
                context.startService(serviceIntent);
            }
        }
    }
}

package com.microntek.android.gps.usb.provider.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.widget.SwitchCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.microntek.android.gps.ubx.data.UbxData;
import com.microntek.android.gps.ubx.util.UbxParser;
import com.microntek.android.gps.usb.provider.USBGpsApplication;
import com.microntek.android.gps.usb.provider.R;
import com.microntek.android.gps.usb.provider.driver.USBGpsProviderService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TreeMap;

import static java.lang.Integer.parseInt;

/**
 * Created by Oliver Bell 5/12/15
 *
 * This activity displays a log, as well as the GPS info. If the users device is
 * large enough and in landscape, the settings fragment will be shown alongside
 */

public class GpsInfoActivity extends USBGpsBaseActivity implements
        USBGpsApplication.ServiceDataListener {

    private SharedPreferences sharedPreferences;
    private static final String TAG = GpsInfoActivity.class.getSimpleName();

    private USBGpsApplication application;

    private SwitchCompat startSwitch;
    private TextView numSatellites;
    private TextView accuracyText;
    private TextView locationText;
    private TextView elevationText;
    private TextView fixText;
    private TextView slasText;
    private TextView sensorText;
    private TextView algText;
    private TextView courseText;
    private TextView odoText;

    //private TextView logText;
    private TextView timeText;
    private TextView mgaOnlineText;
    private TextView mgaOfflineText;
    private TextView errorText;


    private TextView esfMeasText;
    // ScrollView logTextScroller;

    private LinearLayout svinfoLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (isDoublePanel()) {
            savedInstanceState = null;
        }
        super.onCreate(savedInstanceState);

        if (isDoublePanel()) {
            setContentView(R.layout.activity_info_double);
        } else {
            setContentView(R.layout.activity_info);
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        application = (USBGpsApplication) getApplication();

        setupUI();

        if (isDoublePanel()) {
            showSettingsFragment(R.id.settings_holder, false);
        }
    }

    private void setupUI() {
        if (!isDoublePanel()) {
            startSwitch = (SwitchCompat) findViewById(R.id.service_start_switch);
            startSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    sharedPreferences
                            .edit()
                            .putBoolean(USBGpsProviderService.PREF_START_GPS_PROVIDER, isChecked)
                            .apply();
                }
            });
        }

        numSatellites = (TextView) findViewById(R.id.num_satellites_text);
        accuracyText = (TextView) findViewById(R.id.accuracy_text);
        locationText = (TextView) findViewById(R.id.location_text);
        elevationText = (TextView) findViewById(R.id.elevation_text);
        timeText = (TextView) findViewById(R.id.gps_time_text);
        mgaOnlineText = (TextView) findViewById(R.id.mga_online_time_text);
        mgaOfflineText = (TextView) findViewById(R.id.mga_offline_time_text);
        errorText = (TextView) findViewById(R.id.error_text);

        fixText = (TextView) findViewById(R.id.fix_status_text);
        slasText = (TextView) findViewById(R.id.slas_text);
        sensorText = (TextView) findViewById(R.id.sensor_text);
        algText = (TextView) findViewById(R.id.alg_text);
        courseText = (TextView) findViewById(R.id.course_text);
        odoText = (TextView) findViewById(R.id.odo_text);

        esfMeasText = (TextView) findViewById(R.id.esfmeas_text);

        svinfoLayout = (LinearLayout) findViewById(R.id.svinfo_layout);

//        logText = (TextView) findViewById(R.id.log_box);
//        logTextScroller = (ScrollView) findViewById(R.id.log_box_scroller);
    }

    private boolean isDoublePanel() {
        return false;
//        return (getResources().getConfiguration().screenLayout
//                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE &&
//                getResources()
//                        .getConfiguration()
//                        .orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    private void updateData() {
        try {
            boolean running =
                    sharedPreferences.getBoolean(USBGpsProviderService.PREF_START_GPS_PROVIDER, false);

            if (!isDoublePanel()) {
                startSwitch.setChecked(
                        running
                );
            }

            String accuracyValue = "N/A";
            String numSatellitesValue = "N/A";
            String slasStatus = "N/A";
            String fusionStatus = "N/A";
            String fixValue = "N/A";
            String odoValue1 = "N/A";
            String odoValue2 = "N/A";
            String lat = "N/A";
            String lon = "N/A";
            String elevation = "N/A";
            String course = "N/A";
            String gpsTime = "N/A";
            String systemTime = "N/A";
            String mgaOnline = sharedPreferences.getString(getString(R.string.last_mgaOnline_key), "N/A");
            String mgaOffline = sharedPreferences.getString(getString(R.string.last_mgaOffline_key), "N/A");
            String brokenCount = "N/A";

            String esfMeas = "N/A";
            String esfAlgStatus = "N/A";
            String esfAlgPitch = "N/A";
            String esfAlgRoll = "N/A";
            String esfAlgYaw = "N/A";

            Location location = application.getLastLocation();
            if (!running) {
                location = null;
            }

            if (location != null) {

                // Accuracy固定時に表示するため拡張情報から取得に変更
                //if(location.hasAccuracy())
                //    accuracyValue = String.format("%1$.3fm", location.getAccuracy());

                if (location.getExtras() != null) {
                    accuracyValue = String.format("%1$.3fm", location.getExtras().getFloat("PvtAccuracy"));

                    numSatellitesValue = String.valueOf(location.getExtras().getInt(UbxData.SATELLITE_KEY));
                    switch (location.getExtras().getInt(UbxData.FIX_STATUS_KEY)) {
                        case 1: fixValue = "DR only"; break;
                        case 2: fixValue = "2D-Fix"; break;
                        case 3: fixValue = "3D-Fix"; break;
                        case 4: fixValue = "3D-Fix+DR"; break;
                        case 5: fixValue = "Time only"; break;
                    }
                    int sbas = location.getExtras().getInt(UbxData.SBAS_STATUS_KEY);
                    if(sbas > 0)
                        fixValue += "/DGNSS";
                    int slas = location.getExtras().getInt(UbxData.SLAS_STATUS_KEY, -1);
                    if(slas > 0)
                        slasStatus = "QS" + String.valueOf(slas);

                    int fusion = location.getExtras().getInt(UbxData.FUSION_STATUS_KEY, -1);
                    switch (fusion) {
                        case 0: fusionStatus = "Initialization"; break;
                        case 1: fusionStatus = "Fusion"; break;
                        case 2: fusionStatus = "Suspended"; break;
                        case 3: fusionStatus = "Disabled"; break;
                    }

                    StringBuilder sb = new StringBuilder();
                    sb.append("GpsSpeed: ");
                    sb.append(String.format("%1$.1f", location.getExtras().getDouble("Speed", 0)));
                    sb.append("m/s (");
                    sb.append(String.format("%1$.1f", location.getExtras().getDouble("Speed", 0) * 60 * 60 / 1000.0));
                    sb.append("km/h)");
                    sb.append(" CarSpeed:");
                    sb.append(String.format("%1$.1f", location.getExtras().getDouble("ESF_MEAS_Speed", 0)));
                    sb.append("m/s (");
                    sb.append(String.format("%1$.1f", location.getExtras().getDouble("ESF_MEAS_Speed", 0) * 60 * 60 / 1000.0));
                    sb.append("km/h)");
                    sb.append(" LocalTTag:");
                    sb.append(location.getExtras().getInt("ESF_MEAS_Timetag1", -1));
                    sb.append(" CarTTag:");
                    sb.append(location.getExtras().getInt("ESF_MEAS_Timetag2", -1));
                    sb.append("\n");

                    int esfNum = location.getExtras().getInt("ESF_NUM", -1);
                    for(int i = 0; i < esfNum; i++) {
                        String pref_efs = "ESF" + i + "_";
                        int type = location.getExtras().getInt(pref_efs + "type", -1);

                        // スピード以外はフィルタ
                        if(!(type >= 8 && type <= 11))
                            continue;

                        switch (type) {
                            case 5: sb.append("z-axis gyroscope"); break;
                            case 6: sb.append("front-left wheel ticks"); break;
                            case 7: sb.append("front-right wheel ticks"); break;
                            case 8: sb.append("rear-left wheel ticks"); break;
                            case 9: sb.append("rear-right wheel ticks"); break;
                            case 10: sb.append("single tick (speed tick)"); break;
                            case 11: sb.append("speed"); break;
                            case 12: sb.append("gyroscope temperature"); break;
                            case 13: sb.append("y-axis gyroscope"); break;
                            case 14: sb.append("x-axis gyroscope"); break;
                            case 15: sb.append("x-axis accelerometer"); break;
                            case 16: sb.append("y-axis accelerometer"); break;
                            case 17: sb.append("z-axis accelerometer"); break;
                        }

                        int used = location.getExtras().getInt(pref_efs + "used", -1);
                        sb.append(" used :" + used);

                        int ready = location.getExtras().getInt(pref_efs + "ready", -1);
                        sb.append(" ready :" + ready);

                        int calibStatus = location.getExtras().getInt(pref_efs + "calibStatus", -1);
                        sb.append(" calibStatus :");
                        switch (calibStatus) {
                            case 0: sb.append("not calibrated"); break;
                            case 1: sb.append("calibrating"); break;
                            case 2:
                            case 3:
                                sb.append("calibrated");
                                if(type == 11)
                                  fusionStatus += "(+Speed)";
                                else if(type == 10)
                                    fusionStatus += "(+S_Tick)";
                                else if(type == 8 || type == 9)
                                    fusionStatus += "(+R_Ticks)";
                                break;
                        }

                        int timeStatus = location.getExtras().getInt(pref_efs + "timeStatus", -1);
                        sb.append(" timeStatus :");
                        switch (timeStatus) {
                            case 0: sb.append("No data"); break;
                            case 1: sb.append("first byte"); break;
                            case 2: sb.append("Event input"); break;
                            case 3: sb.append("Time tag"); break;
                        }

                        int badMeas = location.getExtras().getInt(pref_efs + "badMeas", -1);
                        if(badMeas > 0)
                            sb.append(" [badMeas]");
                        int badTTag = location.getExtras().getInt(pref_efs + "badTTag", -1);
                        if(badTTag > 0)
                            sb.append(" [badTTag]");
                        int missingMeas = location.getExtras().getInt(pref_efs + "missingMeas", -1);
                        if(missingMeas > 0)
                            sb.append(" [missingMeas]");
                        int noisyMeas = location.getExtras().getInt(pref_efs + "noisyMeas", -1);
                        if(noisyMeas > 0)
                            sb.append(" [noisyMeas]");
                        sb.append("\n");


                    }
                    esfMeas = sb.toString();

                    int odo1 = location.getExtras().getInt(UbxData.DISTANCE1_STATUS_KEY);
                    if(odo1 > 0) {
                        double val = (double) (odo1 / 1000.0);
                        odoValue1 = String.format("%1$.1f", val);
                    }

                    int odo2 = location.getExtras().getInt(UbxData.DISTANCE2_STATUS_KEY);
                    if(odo2 > 0) {
                        double val = (double) (odo2 / 1000.0);
                        odoValue2 = String.format("%1$.1f", val);
                    }

                    int alg_s = location.getExtras().getInt("ESFALG_status", -1);
                    switch (alg_s) {
                        case 0: esfAlgStatus = "fixed"; break;
                        case 1: esfAlgStatus = "ongoing1"; break;
                        case 2: esfAlgStatus = "ongoing2"; break;
                        case 3: esfAlgStatus = "coarse"; break;
                        case 4: esfAlgStatus = "fine"; break;
                    }

                    int alg_p = location.getExtras().getInt("ESFALG_pitch", -1);
                    esfAlgPitch = String.format("%1$.1f", (double)(alg_p / 100.0));

                    int alg_r = location.getExtras().getInt("ESFALG_roll", -1);
                    esfAlgRoll = String.format("%1$.1f", (double)(alg_r / 100.0));

                    int alg_y = location.getExtras().getInt("ESFALG_yaw", -1);
                    esfAlgYaw = String.format("%1$.1f", (double)(alg_y / 100.0));



                }

                lat = String.format("%1$.5f", location.getLatitude());
                lon = String.format("%1$.5f", location.getLongitude());

                if(location.hasAltitude())
                    elevation = String.format("%1$.3fm", location.getAltitude());

                if(location.hasBearing())
                    course = String.format("%1$.3f°", location.getBearing());

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
                gpsTime = sdf.format(new Date(location.getTime()));
                systemTime = sdf.format(new Date(location.getExtras().getLong(UbxParser.SYSTEM_TIME_FIX)));

                brokenCount = String.valueOf(location.getExtras().getInt("BrokenData", 0));
            }

            numSatellites.setText(getString(R.string.number_of_satellites_placeholder, numSatellitesValue));
            accuracyText.setText(getString(R.string.accuracy_placeholder, accuracyValue));
            locationText.setText(getString(R.string.location_placeholder, lat, lon));
            elevationText.setText(getString(R.string.elevation_placeholder, elevation));
            timeText.setText(getString(R.string.gps_time_placeholder, gpsTime, systemTime));
            mgaOnlineText.setText(getString(R.string.mga_online_time_placeholder, mgaOnline));
            mgaOfflineText.setText(getString(R.string.mga_offline_time_placeholder, mgaOffline));
            errorText.setText(getString(R.string.error_placeholder, brokenCount));

            fixText.setText(getString(R.string.fix_status_placeholder, fixValue));
            slasText.setText(getString(R.string.slas_placeholder, slasStatus));
            sensorText.setText(getString(R.string.sensor_placeholder, fusionStatus));
            algText.setText(getString(R.string.alg_placeholder, esfAlgStatus, esfAlgYaw, esfAlgPitch, esfAlgRoll));
            courseText.setText(getString(R.string.course_placeholder, course));
            odoText.setText(getString(R.string.odo_placeholder, odoValue1, odoValue2));

            esfMeasText.setText(esfMeas);
        } catch (Exception e) {
            Log.e("updateData", "updateDataError", e);
        }

        //updateSvInfo();
    }

//    public void updateLog() {
//
//        boolean atBottom = (
//                logText.getBottom() - (
//                        logTextScroller.getHeight() +
//                                logTextScroller.getScrollY()
//                )
//        ) == 0;
//
//        logText.setText(TextUtils.join("\n", application.getLogLines()));
//
//        if (atBottom) {
//            logText.post(new Runnable() {
//                @Override
//                public void run() {
//                    logTextScroller.fullScroll(View.FOCUS_DOWN);
//                }
//            });
//        }
//    }

    // 衛星の情報を反映する
    public void updateSvInfo() {

        try {
            TreeMap<Integer, HashMap<String, String>> svInfo = application.getSvInfo();
            View prevView = null;

            for (Integer key : svInfo.keySet()) {
                HashMap<String, String> rec = svInfo.get(key);

                int cno = parseInt(rec.get("cno"));
                boolean hidden = parseInt(rec.get("disableCnt")) > 9;
                View view = svinfoLayout.findViewById(key);

                // 一定期間受信できなかったものは非表示
                if(hidden) {
                    if(view != null) {
                        //svinfoLayout.removeView(view);
                        view.setVisibility(View.GONE);
                        prevView = view;
                    }
                    continue;
                }
                // 初回のみ生成
                if(view == null) {
                    // 受信レベル0の場合は生成しない
                    if(cno == 0)
                        continue;
                    view = getLayoutInflater().inflate(R.layout.svinfo_row, null);
                    view.setId(key);

                    // SvNo順に表示するので途中に挿入
                    if(prevView != null) {
                        int prevIndex = svinfoLayout.indexOfChild(prevView);
                        svinfoLayout.addView(view, prevIndex + 1);
                    }
                    else
                        svinfoLayout.addView(view, 0);

                    ImageView icon = (ImageView) view.findViewById(R.id.icon);
                    icon.setImageResource(parseInt(rec.get("icon")));
                    TextView svName = (TextView) view.findViewById(R.id.svName);
                    svName.setText(rec.get("svName"));
                    ProgressBar bar = (ProgressBar) view.findViewById(R.id.bar);
                    //bar.setMax(50);
                }
                view.setVisibility(View.VISIBLE);
                TextView cnoView = (TextView) view.findViewById(R.id.cno);
                cnoView.setText(rec.get("cno"));

                if(rec.get("ephFlag").equals("1")) {
                    cnoView.setPaintFlags(cnoView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
                else if((cnoView.getPaintFlags() & Paint.UNDERLINE_TEXT_FLAG) > 0)
                    cnoView.setPaintFlags(cnoView.getPaintFlags() - Paint.UNDERLINE_TEXT_FLAG);
                ProgressBar bar = (ProgressBar) view.findViewById(R.id.bar);
                bar.setProgress(cno);

                if(rec.get("useFlag").equals("1"))
                    bar.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
                else
                    bar.getProgressDrawable().setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);
                prevView = view;
            }
        } catch (Exception e) {
            Log.e("updateSvInfo", "updateSvInfoError", e);
        }
    }

    @Override
    public void onResume() {
        updateData();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        ((USBGpsApplication) getApplication()).registerServiceDataListener(this);
        // 設定画面で変更した情報がアクションバーに反映されないのでメニューを再構築
        invalidateOptionsMenu();
        super.onResume();
    }

    @Override
    public void onPause() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        ((USBGpsApplication) getApplication()).unregisterServiceDataListener(this);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!isDoublePanel()) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_main, menu);

            MenuItem menuRec = menu.findItem(R.id.action_rec);
            if(!sharedPreferences.getBoolean(USBGpsProviderService.PREF_TRACK_RECORDING, false)) {
                menuRec.setIcon(R.drawable.ic_rec);
            } else {
                menuRec.setIcon(R.drawable.ic_pause);
            }
        }
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_rec) {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            boolean enable = (!sharedPreferences.getBoolean(USBGpsProviderService.PREF_TRACK_RECORDING, false));
            edit.putBoolean(USBGpsProviderService.PREF_TRACK_RECORDING, enable);
            edit.apply();
            return true;
        } else if (id == R.id.action_reset_odo) {
            Intent intent = new Intent(this, USBGpsProviderService.class);
            intent.setAction(USBGpsProviderService.ACTION_RESET_ODO);
            startService(intent);
            return true;
        } else if (id == R.id.action_reset_alg) {
            Intent intent = new Intent(this, USBGpsProviderService.class);
            intent.setAction(USBGpsProviderService.ACTION_RESET_ALG);
            startService(intent);
            return true;
        } else if (id == R.id.action_debug_dr_enable) {
            Intent intent = new Intent(this, USBGpsProviderService.class);
            intent.setAction(USBGpsProviderService.ACTION_DEBUG_DR_ENABLE);
            startService(intent);
            return true;
        } else if (id == R.id.action_debug_dr_disable) {
            Intent intent = new Intent(this, USBGpsProviderService.class);
            intent.setAction(USBGpsProviderService.ACTION_DEBUG_DR_DISABLE);
            startService(intent);
            return true;
        } else if (id == R.id.action_mga_on) {
            Intent intent = new Intent(this, USBGpsProviderService.class);
            intent.setAction(USBGpsProviderService.ACTION_MGA_ON);
            startService(intent);
            return true;
        } else if (id == R.id.action_mga_off) {
            Intent intent = new Intent(this, USBGpsProviderService.class);
            intent.setAction(USBGpsProviderService.ACTION_MGA_OFF);
            startService(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onNewSentence(String sentence) {
//        updateLog();
        updateSvInfo();
    }

    @Override
    public void onNewSvInfo() {
        updateSvInfo();
    }

    @Override
    public void onLocationNotified(Location location) {
        updateData();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(USBGpsProviderService.PREF_START_GPS_PROVIDER)) {
            updateData();
        } else if (key.equals(USBGpsProviderService.PREF_TRACK_RECORDING)) {
            invalidateOptionsMenu();
        }

        super.onSharedPreferenceChanged(sharedPreferences, key);
    }
}
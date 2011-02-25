package com.erdfelt.android.buildinfo;

import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class BuildInfoActivity extends Activity {
    private static final String TAG      = "BuildInfo";
    private static final int    SHARE_ID = Menu.FIRST;
    private InfoModel           model;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        model = new InfoModel();

        addBuildInfo();
        addSystemInfo();
        addDisplayInfo();
        addTelephonyInfo();
        addNetworkInfo();
        addSystemProperties();

        addSensorInfo("Accelerometer", Sensor.TYPE_ACCELEROMETER);
        addSensorInfo("Gyroscope", Sensor.TYPE_GYROSCOPE);
        addSensorInfo("Light", Sensor.TYPE_LIGHT);
        addSensorInfo("Magnetic Field", Sensor.TYPE_MAGNETIC_FIELD);
        addSensorInfo("Orientation", Sensor.TYPE_ORIENTATION);
        addSensorInfo("Pressure", Sensor.TYPE_PRESSURE);
        addSensorInfo("Proximity", Sensor.TYPE_PROXIMITY);
        addSensorInfo("Temperature", Sensor.TYPE_TEMPERATURE);

        ListView list = (ListView) findViewById(R.id.list);
        InfoModelAdapter adapter = new InfoModelAdapter(getLayoutInflater(), model);
        list.setAdapter(adapter);
    }

    private void addNetworkInfo() {
        model.addHeader("Network");

        ConnectivityManager connmgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if(connmgr == null) {
            model.addDetail("Service not available", "");
            return;
        }
        
        NetworkInfo infos[] = connmgr.getAllNetworkInfo();
        
        model.addDetail("Network Devices", "" + infos.length);
        
        for(NetworkInfo info: infos) {
            String header = info.getTypeName();
            if(info.getSubtype() > 0) {
                header += " - " + info.getSubtypeName();
            }
            model.addHeader(header);
            
            model.addDetail("Type", String.valueOf(info.getType()) + " - " + info.getTypeName());
            String st = "" + info.getSubtype();
            if(info.getSubtype() > 0) {
                st += " - " + info.getSubtypeName();
            }
            model.addDetail("Sub Type", st);
            model.addDetail("Extra Info", info.getExtraInfo());
            model.addDetail("State", info.getState().name());
            model.addDetail("Reason", info.getReason());
            
            model.addDetail("Available", String.valueOf(info.isAvailable()));
            model.addDetail("Connected", String.valueOf(info.isConnected()));
            model.addDetail("Connected or Connecting", String.valueOf(info.isConnectedOrConnecting()));
            model.addDetail("Failover", String.valueOf(info.isFailover()));
            model.addDetail("Roaming", String.valueOf(info.isRoaming()));

            model.addDetail("Detailed State", info.getDetailedState().name());
        }
    }

    private void addSystemProperties() {
        model.addHeader("System Properties");

        Set<String> excluded = new HashSet<String>();
        // Noisy / Pointless
        excluded.add("line.separator");
        excluded.add("file.separator");
        excluded.add("path.separator");
        // Java Standard Properties not supported on android.
        excluded.add("java.compiler");
        excluded.add("java.ext.dirs");
        excluded.add("user.home");
        excluded.add("user.name");

        Properties props = System.getProperties();
        Map<String, String> sorted = new TreeMap<String, String>();
        @SuppressWarnings("unchecked")
        Enumeration<String> names = (Enumeration<String>) props.propertyNames();
        String name;
        while (names.hasMoreElements()) {
            name = names.nextElement();
            if (excluded.contains(name)) {
                continue; // skip (its excluded)
            }
            sorted.put(name, props.getProperty(name));
        }

        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            model.addDetail(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean ret = super.onCreateOptionsMenu(menu);
        MenuItem item = menu.add(0, SHARE_ID, 0, R.string.share_menu);
        item.setIcon(android.R.drawable.ic_menu_share);
        return ret;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case SHARE_ID:
            Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
            shareIntent.setType("text/plain");

            PackageManager pm = getPackageManager();
            Resources resources = getResources();

            StringBuilder subject = new StringBuilder();
            try {
                ApplicationInfo ai = pm.getApplicationInfo(getPackageName(), 0);
                subject.append(pm.getApplicationLabel(ai));
            } catch (NameNotFoundException e) {
                Log.w(TAG, "Unable to find name of application", e);
                subject.append(resources.getString(R.string.app_name));
            }

            try {
                PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);
                subject.append(" ").append(pi.versionName);
            } catch (NameNotFoundException e) {
                Log.w(TAG, "Unable to find pacakge info", e);
                subject.append(" - (Unknown Version)");
            }

            shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject.toString());
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, model.asText());

            startActivity(Intent.createChooser(shareIntent, "Share Android Info Details"));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addSensorInfo(String label, int sensorType) {
        model.addHeader("Sensor: " + label);

        SensorManager smgr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = smgr.getSensorList(sensorType);
        int len = sensors.size();
        if (len < 1) {
            model.addDetail("Hardware Not Present", "");
            return;
        }
        String prefix;
        for (int i = 0; i < len; i++) {
            Sensor sensor = sensors.get(i);
            prefix = "[" + (i + 1) + "] ";
            model.addDetail(prefix + "name", sensor.getName());
            model.addDetail(prefix + "vendor", sensor.getVendor());
            model.add(new InfoDetail(prefix + "type", sensor.getType()));
            model.add(new InfoDetail(prefix + "version", sensor.getVersion()));
            model.add(new InfoDetail(prefix + "power", sensor.getPower()));
            model.add(new InfoDetail(prefix + "resolution", sensor.getResolution()));
            model.add(new InfoDetail(prefix + "maximum-range", sensor.getMaximumRange()));
        }
    }

    private void addTelephonyInfo() {
        model.addHeader("Telephony");

        TelephonyManager tphony = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        String callstate = "Unknown";
        switch (tphony.getCallState()) {
        case TelephonyManager.CALL_STATE_IDLE:
            callstate = "Idle";
            break;
        case TelephonyManager.CALL_STATE_OFFHOOK:
            callstate = "Off-Hook";
            break;
        case TelephonyManager.CALL_STATE_RINGING:
            callstate = "Ringing";
            break;
        }

        String dataactivity = "Unknown";
        switch (tphony.getDataActivity()) {
        case TelephonyManager.DATA_ACTIVITY_DORMANT:
            dataactivity = "Dormant";
            break;
        case TelephonyManager.DATA_ACTIVITY_IN:
            dataactivity = "In";
            break;
        case TelephonyManager.DATA_ACTIVITY_INOUT:
            dataactivity = "In-Out";
            break;
        case TelephonyManager.DATA_ACTIVITY_NONE:
            dataactivity = "None";
            break;
        case TelephonyManager.DATA_ACTIVITY_OUT:
            dataactivity = "Out";
            break;
        }

        String datastate = "Unknown";
        switch (tphony.getDataState()) {
        case TelephonyManager.DATA_CONNECTED:
            datastate = "Connected";
            break;
        case TelephonyManager.DATA_CONNECTING:
            datastate = "Connecting";
            break;
        case TelephonyManager.DATA_DISCONNECTED:
            datastate = "Disconnected";
            break;
        case TelephonyManager.DATA_SUSPENDED:
            datastate = "Suspended";
            break;
        }

        String networktype = "Unknown";
        switch (tphony.getNetworkType()) {
        case TelephonyManager.NETWORK_TYPE_1xRTT:
            networktype = "1xRTT";
            break;
        case TelephonyManager.NETWORK_TYPE_CDMA:
            networktype = "CDMA";
            break;
        case TelephonyManager.NETWORK_TYPE_EDGE:
            networktype = "EDGE";
            break;
        case TelephonyManager.NETWORK_TYPE_EVDO_0:
            networktype = "EVDO_0";
            break;
        case TelephonyManager.NETWORK_TYPE_EVDO_A:
            networktype = "EVDO_A";
            break;
        case TelephonyManager.NETWORK_TYPE_GPRS:
            networktype = "GPRS";
            break;
        case TelephonyManager.NETWORK_TYPE_HSDPA:
            networktype = "HSDPA";
            break;
        case TelephonyManager.NETWORK_TYPE_HSPA:
            networktype = "HSPA";
            break;
        case TelephonyManager.NETWORK_TYPE_HSUPA:
            networktype = "HSUPA";
            break;
        case TelephonyManager.NETWORK_TYPE_UMTS:
            networktype = "UMTS";
            break;
        case TelephonyManager.NETWORK_TYPE_UNKNOWN:
            networktype = "UNKNOWN";
            break;
        }

        String phonetype = "Unknown";
        switch (tphony.getPhoneType()) {
        case TelephonyManager.PHONE_TYPE_CDMA:
            phonetype = "cdma";
            break;
        case TelephonyManager.PHONE_TYPE_GSM:
            phonetype = "gsm";
            break;
        case TelephonyManager.PHONE_TYPE_NONE:
            phonetype = "none";
            break;
        }

        String simstate = "Unknown";
        switch (tphony.getSimState()) {
        case TelephonyManager.SIM_STATE_ABSENT:
            simstate = "Absent";
            break;
        case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
            simstate = "Network Locked";
            break;
        case TelephonyManager.SIM_STATE_PIN_REQUIRED:
            simstate = "Pin Required";
            break;
        case TelephonyManager.SIM_STATE_PUK_REQUIRED:
            simstate = "Puk Required";
            break;
        case TelephonyManager.SIM_STATE_READY:
            simstate = "Ready";
            break;
        case TelephonyManager.SIM_STATE_UNKNOWN:
            simstate = "Unknown";
            break;
        }

        model.addDetail("Call State", callstate);
        model.addDetail("Data Activity", dataactivity);
        model.addDetail("Data State", datastate);
        model.addDetail("Device ID", tphony.getDeviceId());
        model.addDetail("Device Software Version", tphony.getDeviceSoftwareVersion());
        model.addDetail("Line 1 Number", tphony.getLine1Number());
        model.addDetail("Network Country Iso", tphony.getNetworkCountryIso());
        model.addDetail("Network Operator", tphony.getNetworkOperator());
        model.addDetail("Network Operator Name", tphony.getNetworkOperatorName());
        model.addDetail("Network Type", networktype);
        model.addDetail("Phone Type", phonetype);
        model.addDetail("Sim Country ISO", tphony.getSimCountryIso());
        model.addDetail("Sim Operator", tphony.getSimOperator());
        model.addDetail("Sim Operator Name", tphony.getSimOperatorName());
        model.addDetail("Sim Serial Number", tphony.getSimSerialNumber());
        model.addDetail("Sim State", simstate);
        model.addDetail("Subscriber ID", tphony.getSubscriberId());
        model.addDetail("Voice Mail Alpha Tag", tphony.getVoiceMailAlphaTag());
        model.addDetail("Voice Mail Number", tphony.getVoiceMailNumber());
    }

    private void addDisplayInfo() {
        model.addHeader("Display Info");

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        model.add(new InfoDetail("density", metrics.density));
        model.add(new InfoDetail("densityDpi", metrics.densityDpi));
        model.add(new InfoDetail("scaledDensity", metrics.scaledDensity));
        model.add(new InfoDetail("widthPixels", metrics.widthPixels));
        model.add(new InfoDetail("heightPixels", metrics.heightPixels));
        model.add(new InfoDetail("xdpi", metrics.xdpi));
        model.add(new InfoDetail("ydpi", metrics.ydpi));
    }

    private void addSystemInfo() {
        model.addHeader("System Info");

        ContentResolver contentResolver = getContentResolver();
        String id = android.provider.Settings.System.getString(contentResolver,
                android.provider.Settings.System.ANDROID_ID);
        if (id == null) {
            id = "<null> (on emulator?)";
        }

        model.addDetail("ANDROID_ID", id);

        PackageManager pm = getPackageManager();

        for (Field field : pm.getClass().getFields()) {
            if (field.getName().startsWith("FEATURE_")) {
                String featureId;
                try {
                    featureId = (String) field.get(pm);
                    boolean exists = pm.hasSystemFeature(featureId);
                    model.addDetail(field.getName(), exists ? "Exists" : "-");
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Feature Test", e);
                } catch (IllegalAccessException e) {
                    Log.e(TAG, "Feature Test", e);
                }
            }
        }

        model.addDetail("Env.DataDir", Environment.getDataDirectory().getAbsolutePath());
        model.addDetail("Env.DownloadCacheDir", Environment.getDownloadCacheDirectory().getAbsolutePath());
        model.addDetail("Env.ExternalStorageDir", Environment.getExternalStorageDirectory().getAbsolutePath());
        model.addDetail("Env.RootDir", Environment.getRootDirectory().getAbsolutePath());
    }

    private void addBuildInfo() {
        model.addHeader("Build Info");

        model.add(new InfoDetail("BOARD", Build.BOARD));
        model.add(new InfoDetail("BRAND", Build.BRAND));
        model.add(new InfoDetail("CPU_ABI", Build.CPU_ABI));
        model.add(new InfoDetail("DEVICE", Build.DEVICE));
        model.add(new InfoDetail("FINGERPRINT", Build.FINGERPRINT));
        model.add(new InfoDetail("HOST", Build.HOST));
        model.add(new InfoDetail("ID", Build.ID));
        model.add(new InfoDetail("MANUFACTURER", Build.MANUFACTURER));
        model.add(new InfoDetail("MODEL", Build.MODEL));
        model.add(new InfoDetail("PRODUCT", Build.PRODUCT));
        model.add(new InfoDetail("TAGS", Build.TAGS));
        model.add(new InfoDetail("TYPE", Build.TYPE));
        model.add(new InfoDetail("USER", Build.USER));
        model.add(new InfoDetail("VERSION.RELEASE", Build.VERSION.RELEASE));
        model.add(new InfoDetail("VERSION.SDK_INT", Build.VERSION.SDK_INT));
        model.add(new InfoDetail("VERSION.CODENAME", Build.VERSION.CODENAME));
    }
}
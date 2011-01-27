package com.erdfelt.android.buildinfo;

import java.lang.reflect.Field;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
            shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Android Info");
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
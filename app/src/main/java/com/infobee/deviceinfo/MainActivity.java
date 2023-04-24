package com.infobee.deviceinfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.LocationManager;
import android.opengl.GLES10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.BatteryManager;
import android.os.Bundle;
import android.Manifest;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;


public class MainActivity extends AppCompatActivity {


    TextView txManufacture, txGpu,txModal,txBuild, txRam, txStorage,txBattery,txAndroid,txProcessor, txCameraPixel,txcameraAperture,txCpu,txImei;
    private static final int REQUEST_PERMISSIONS_CODE = 123;
    private static final int CAMERA_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        txManufacture = findViewById(R.id.manufacturer_val);
        txModal = findViewById(R.id.model_val);
        txBuild =findViewById(R.id.build_val);
        txRam = findViewById(R.id.ram_val);
        txStorage =findViewById(R.id.storage_val);
        txBattery =  findViewById(R.id.battery_val);
        txAndroid = findViewById(R.id.android_val);
        txProcessor = findViewById(R.id.processor_val);
        txCameraPixel = findViewById(R.id.cameraMegapixels_val);
        txcameraAperture = findViewById(R.id.cameraAperture_val);
        txCpu = findViewById(R.id.cpu_val);
        txImei = findViewById(R.id.imei_val);
        //txGpu = findViewById(R.id.gpu_val);
        retrieveDeviceInfo();



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_PHONE_STATE}, REQUEST_PERMISSIONS_CODE);



            }
            else if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED){
                    // Request the permission
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            CAMERA_PERMISSION_CODE);

            }
            else {
                fetchDeviceInfo();
            }
        } else {
            fetchDeviceInfo();
        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, check camera permission
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Request camera permission
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            CAMERA_PERMISSION_CODE);
                } else {
                    // Camera permission granted, fetch device info
                    fetchDeviceInfo();
                }
            } else {
                // Permissions not granted, handle accordingly
            }
        } else if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted, fetch device info
                fetchDeviceInfo();
            } else {
                // Camera permission not granted, handle accordingly
            }
        }
    }

    private void fetchDeviceInfo() {


        // Camera information
        Camera camera = Camera.open();
        Camera.Parameters params = camera.getParameters();
        List<Camera.Size> sizes = params.getSupportedPictureSizes();
        Camera.Size size = sizes.get(0);
        for (int i = 1; i < sizes.size(); i++) {
            Camera.Size tmpSize = sizes.get(i);
            if (tmpSize.width * tmpSize.height > size.width * size.height) {
                size = tmpSize;
            }
        }
        int cameraMegapixels = size.width * size.height / 1000000;
        float cameraAperture = params.getHorizontalViewAngle();
        txCameraPixel.setText(String.valueOf(cameraMegapixels));
        txcameraAperture.setText(String.valueOf(cameraAperture));
        System.out.println("cameraMegapixels:"+cameraMegapixels);
        System.out.println("cameraAperture:"+cameraAperture);


        // Processor information
        String cpuInfo = null;
        try {
            FileReader fileReader = new FileReader("/proc/cpuinfo");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            cpuInfo = bufferedReader.readLine();
            bufferedReader.close();
            fileReader.close();

            txCpu.setText(cpuInfo);
            System.out.println("cpuInfo:"+cpuInfo);
        } catch (IOException e) {

            System.out.println(e);

        }




        String imei = getIMEINumber(getApplicationContext());
        System.out.println("IMEI"+imei);

        txImei.setText(imei);



    }


    public static String getIMEINumber(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            return telephonyManager.getImei();
        } else {
            // Permission not granted
            return null;
        }
    }

        private void retrieveDeviceInfo() {
        Context context = getApplicationContext();

        // Retrieve the manufacturer name
        String manufacturer = Build.MANUFACTURER;
        System.out.println("manufacturer:"+manufacturer);
        txManufacture.setText(manufacturer);

        // Retrieve the device model name and number
        String model = Build.MODEL;
        String deviceInfo = model + " (Build " + Build.ID + ")";
        String buildNumber = Build.DISPLAY;
        System.out.println("Build:"+deviceInfo);
        txModal.setText(model);
        txBuild.setText(buildNumber);

        // Retrieve the amount of RAM
        long totalRam = Runtime.getRuntime().totalMemory();
        long freeRam = Runtime.getRuntime().freeMemory();
        long usedRam = totalRam - freeRam;
        String ramInfo = formatStorageSize(usedRam) + " / " + formatStorageSize(totalRam);
        System.out.println("ram:"+ramInfo);
        txRam.setText("Total: "+ramInfo + "\nFree RAM: "+ formatStorageSize(freeRam)+ "\nUsed RAM:"+ formatStorageSize(usedRam));


        // Retrieve the amount of storage
        long totalStorage = getTotalStorageSpace(context);
        long freeStorage = getFreeStorageSpace(context);
        long usedStorage = totalStorage - freeStorage;
        String storageInfo = formatStorageSize(usedStorage) + " / " + formatStorageSize(totalStorage);
        System.out.println("storageInfo:"+storageInfo);
        txStorage.setText("Total: "+storageInfo + "\nFree Storage: "+ formatStorageSize(freeStorage)+ "\nUsed Storage:"+formatStorageSize( usedStorage));


        // Retrieve the battery information
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPercent = (level / (float) scale) * 100;
        System.out.println("batteryPercent:"+batteryPercent);
        txBattery.setText(String.valueOf(batteryPercent));


        // Retrieve the Android version
        String androidVersion = Build.VERSION.RELEASE;
        System.out.println("androidVersion:"+androidVersion);
        txAndroid.setText(androidVersion);

        // Retrieve the camera information
        // TODO: Implement camera retrieval code

        // Retrieve the processor information
        String processor = Build.HARDWARE;
        System.out.println("processor:"+processor);
        txProcessor.setText(processor);


//        String gpu_info = "Renderer: " + myGLRenderer.getGLRenderer() + "\nVendor:  " + myGLRenderer.getGLVendor() + "\nVersion:  "+myGLRenderer.getGLVersion() +"\nExtensions :  "+myGLRenderer.getGLExtensions();
//        txGpu.setText(gpu_info);

    }

    private String formatStorageSize(long size) {
        final int KB = 1024;
        final int MB = KB * KB;
        final int GB = MB * KB;

        if (size >= GB) {
            return String.format("%.1f GB", (float)size / GB);
        } else if (size >= MB) {
            return String.format("%.1f MB", (float)size / MB);
        } else if (size >= KB) {
            return String.format("%.1f KB", (float)size / KB);
        } else {
            return String.format("%d bytes", size);
        }
    }

    private long getTotalStorageSpace(Context context) {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        return blockSize * totalBlocks;
    }

    private long getFreeStorageSpace(Context context) {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return blockSize * availableBlocks;
    }


}

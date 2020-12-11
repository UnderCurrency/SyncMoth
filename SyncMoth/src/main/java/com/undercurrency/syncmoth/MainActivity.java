package com.undercurrency.syncmoth;

import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.undercurrency.audiomoth.usbhid.USBHidTool;
import com.undercurrency.audiomoth.usbhid.events.AudioMothConfigEvent;
import com.undercurrency.audiomoth.usbhid.events.AudioMothConfigReceiveEvent;
import com.undercurrency.audiomoth.usbhid.events.AudioMothPacketEvent;
import com.undercurrency.audiomoth.usbhid.events.AudioMothPacketReceiveEvent;
import com.undercurrency.audiomoth.usbhid.events.AudioMothSetDateEvent;
import com.undercurrency.audiomoth.usbhid.events.AudioMothSetDateReceiveEvent;
import com.undercurrency.audiomoth.usbhid.events.DeviceAttachedEvent;
import com.undercurrency.audiomoth.usbhid.events.DeviceDetachedEvent;
import com.undercurrency.audiomoth.usbhid.events.PrepareDevicesListEvent;
import com.undercurrency.audiomoth.usbhid.events.SelectDeviceEvent;
import com.undercurrency.audiomoth.usbhid.events.ShowDevicesListEvent;
import com.undercurrency.audiomoth.usbhid.model.DeviceInfo;
import com.undercurrency.audiomoth.usbhid.model.RecordingSettings;
import com.undercurrency.audiomoth.usbhid.model.TimePeriods;
import com.undercurrency.syncmoth.model.ConabioRecordingSettings;
import com.undercurrency.syncmoth.model.RecordingSettingsViewModel;
import com.undercurrency.syncmoth.ui.main.PreSettingsFragment;
import com.undercurrency.syncmoth.ui.main.SectionsPagerAdapter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.EventBusException;

public class MainActivity extends AppCompatActivity {
    public static final long SD32_GB = 32_212_254_720L;
    public static final String AUDIOMOTH_CONFIG = "AUDIOMOTH_CONFIG";
    public static final String AUDIOMOTH_DEVICE = "AUDIOMOTH_DEVICE";
    private static final String TAG = "MainActivity";
    private final boolean deviceReady = false;
    protected EventBus eventBus;
    NotificationManager notificationManager;
    private Intent usbhidService;
    private Date deviceDate=null;
    private RecordingSettingsViewModel rsViewModel = null;
    private RecordingSettings receivedRecordingSettings = null;
    private boolean deviceSelected = false;
    private TextView tvTopDeviceId;
    private ImageView ivStatus;


    private BottomNavigationView toolbar;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bottom_app_bar, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startEventBus();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        rsViewModel =  new ViewModelProvider(this).get(RecordingSettingsViewModel.class);
        loadConfiguration("default.json");
        tvTopDeviceId = (TextView)  findViewById(R.id.tv_top_device_id);
        ivStatus = (ImageView) findViewById(R.id.iv_status);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        toolbar = findViewById(R.id.bottom_navigation);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        toolbar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                //noinspection SimplifiableIfStatement
                if (id == R.id.mnu_connect) { //conectar
                    Log.v(TAG, "on menu connect  click");
                    eventBus.post(new PrepareDevicesListEvent());
                } else if (id == R.id.mnu_send) {//configurar
                    if (deviceSelected) {
                        deviceDate=new Date();
                        eventBus.post(new AudioMothSetDateEvent(new Date()));
                    }
                } else if(id == R.id.mnu_help){
                    String url = getString(R.string.url_conabio_syncmoth);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                } else if (id == R.id.mnu_share) {//compartir
                    if (receivedRecordingSettings != null) {
                        DeviceInfo deviceInfo = rsViewModel.getDeviceInfo();
                        receivedRecordingSettings.setDeviceInfo(deviceInfo);
                        SimpleDateFormat sdf= new SimpleDateFormat(getString(R.string.date_format));
                        Date internalDeviceDate = null;
                        try {
                            internalDeviceDate = sdf.parse(receivedRecordingSettings.getDeviceInfo().getDate());
                        } catch (ParseException e) {
                            Log.e("Error",e.getLocalizedMessage(),e);
                            internalDeviceDate = new Date();
                        }
                        String fileName = String.format(getString(R.string.FILE_NAME_FORMAT),
                                internalDeviceDate,
                                receivedRecordingSettings.getDeviceInfo().getDeviceId());
                        ConabioRecordingSettings crs = new ConabioRecordingSettings(receivedRecordingSettings);
                        shareConfig(crs, fileName);
                    }
                }
                return false;
            }
        });

    }


    private void startEventBus() {
        try {
            eventBus = EventBus.builder().logNoSubscriberMessages(false).sendNoSubscriberEvent(false).installDefaultEventBus();
        } catch (EventBusException e) {
            eventBus = EventBus.getDefault();
            Log.d(TAG, "startEventBus", e);
        }
    }

    private void startService() {
        usbhidService = new Intent(this, USBHidTool.class);
        usbhidService.putExtra("localTime",true);
        startService(usbhidService);
    }

    @Override
    protected void onStart() {
        try {
            super.onStart();
            startService();
            eventBus.register(this);
        } catch (Exception e) {
            Log.d(TAG, "onStart", e);
        }
    }

    @Override
    protected void onStop() {
        eventBus.unregister(this);
        //  deviceReady=false;
        super.onStop();
    }

    /**********************************************************/
    /*              AUDIOMOTH HANDLING EVENTS                 */
    /**********************************************************/
    public void onEvent(ShowDevicesListEvent event) {
        showListOfDevices(event.getCharSequenceArray());
    }

    public void onEvent(DeviceAttachedEvent event) {
    //    Toast.makeText(this, getString(R.string.DEVICE_ATTACHED), Toast.LENGTH_LONG).show();
        eventBus.post(new AudioMothPacketEvent());
        deviceSelected = true;
        Log.i(TAG, "Device Attached");
    }

    public void onEvent(DeviceDetachedEvent event) {
        Log.i(TAG, "Device Dettached");
        tvTopDeviceId.setText(R.string.device_id_empty);
        ivStatus.setImageDrawable(getDrawable(R.drawable.ic_frog_inactive));
    }

    private void showListOfDevices(CharSequence[] devicesName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (devicesName.length == 0) {
            builder.setTitle(getString(R.string.MESSAGE_CONNECT_YOUR_USB_HID_DEVICE));
        } else {
            builder.setTitle(getString(R.string.MESSAGE_SELECT_YOUR_USB_HID_DEVICE));
        }

        builder.setItems(devicesName, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                eventBus.post(new SelectDeviceEvent(which));
            }
        });
        builder.setCancelable(true);
        builder.show();
    }

    public void onEvent(AudioMothPacketReceiveEvent event) {
        Log.d(TAG, "AudioMothPacketReceiveEvent");
        if (event.getDeviceInfo() != null) {
            DeviceInfo deviceInfo = event.getDeviceInfo();
            rsViewModel.setDeviceInfo(deviceInfo);
            rsViewModel.getRecordingSettings().setDeviceInfo(deviceInfo);
            tvTopDeviceId.setText(deviceInfo.getDeviceId());
            ivStatus.setImageDrawable(getDrawable(R.drawable.ic_frog_active));
            Calendar now = Calendar.getInstance();
            TimeZone timeZone = now.getTimeZone();
            FragmentManager manager = getSupportFragmentManager();
            List<Fragment> fragments =  manager.getFragments();
            for(Fragment f : fragments){
                if(f instanceof PreSettingsFragment){
                   ((PreSettingsFragment) f).syncUI();
                }
            }
         //   fragment.syncUI();
            Log.d(TAG, "Battery " + deviceInfo.getBattery() + ", serial " + deviceInfo.getDeviceId() + ", firmware " + deviceInfo.getFirmwareVersion() + " date " + deviceInfo.getDate()+" timezone:"+timeZone.getRawOffset());
        }
    }

    public void onEvent(AudioMothConfigReceiveEvent event) {
        Log.d(TAG, "AudioMothConfigReceiveEvent");
        if (event.getRecordingSettings() != null) {
            receivedRecordingSettings = event.getRecordingSettings();
             MediaPlayer mp;
            if (rsViewModel.getRecordingSettings().equals(receivedRecordingSettings)) {
                Log.d(TAG, "Configuration sent and received Ok");
                mp = MediaPlayer.create(this, R.raw.frog_croak);
                mp.start();
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(getText(R.string.app_name))
                        .setIcon(R.drawable.ic_frog_active)
                        .setMessage(getText(R.string.CONFIG_SENT_SUCCESS))
                        .setNeutralButton(getText(R.string.DISMISS), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mp.stop();
                                mp.release();
                            }
                        })
                        .show();

            } else {
                Log.d(TAG, "Configuration sent and NOT received Ok");
                mp = MediaPlayer.create(this, R.raw.suspense);
                mp.start();
                 new AlertDialog.Builder(this)
                         .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(getText(R.string.app_name))
                        .setIcon(R.drawable.ic_frog_inactive)
                        .setMessage(getText(R.string.CONFIG_SENT_FAIL))
                        .setNeutralButton(getText(R.string.DISMISS), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mp.stop();
                                mp.release();
                            }
                        }).show();
            }
            ConabioRecordingSettings crs = new ConabioRecordingSettings(receivedRecordingSettings);
            copyToClipboard(crs);
        }
    }

    public void onEvent(AudioMothSetDateReceiveEvent event) {
        Log.d(TAG, "AudioMothSetDateReceiveEvent");
        if (event.getDate() != null) {
            boolean dateEqual= event.getDate().equals(deviceDate);
            SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.date_format));
            rsViewModel.getDeviceInfo().setDate(sdf.format(deviceDate));
            rsViewModel.getRecordingSettings().setDeviceInfo(rsViewModel.getDeviceInfo());
            eventBus.post(new ConabioRecordingSettings());
            eventBus.post(new AudioMothConfigEvent(rsViewModel.getRecordingSettings()));
        }
    }


    /**********************************************************/
    /*         END OF AUDIOMOTH HANDLING EVENTS               */
    /**********************************************************/


    public void loadConfiguration(String configurationNameFile) {
        String jsonConfig = getStringFromAssets(this, configurationNameFile);

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        RecordingSettings rsLoaded = gson.fromJson(jsonConfig, RecordingSettings.class);
        fixGsonDeserialization(rsLoaded);
        DeviceInfo deviceInfo = rsViewModel.getDeviceInfo();
        Log.e(TAG,"loadConfiguration "+configurationNameFile+" "+rsLoaded.toString());
        rsViewModel.setRecordingSettings(rsLoaded);
        rsViewModel.getRecordingSettings().setDeviceInfo(deviceInfo);

    }

    private void fixGsonDeserialization(RecordingSettings rsLoaded) {
        rsLoaded.setFirstRecordingEnable(rsLoaded.getFirstRecordingDate()!=null);
        rsLoaded.setLastRecordingEnable(rsLoaded.getLastRecordingDate()!=null);
        if(rsLoaded.isLocalTime()) {
            ArrayList<TimePeriods> tp = rsLoaded.getTimePeriods();
            for(TimePeriods t : tp){
                t.setLocalTime(true);
            }
        }
    }

    private String getStringFromAssets(Context ctx, String pathToJson) {
        InputStream rawInput;
        ByteArrayOutputStream rawOutput = null;
        try {
            rawInput = ctx.getAssets().open(pathToJson);
            byte[] buffer = new byte[rawInput.available()];
            rawInput.read(buffer);
            rawOutput = new ByteArrayOutputStream();
            rawOutput.write(buffer);
            rawOutput.close();
            rawInput.close();
        } catch (IOException e) {
            Log.e("Error", e.toString());
        }
        return rawOutput.toString();
    }

    /**
     * Shares a ConabioRecordingSettings from AudioMoth whith fileName
     *
     * @param crs - ConabioRecordingSettings object
     * @param fileName
     */
    private void shareConfig(ConabioRecordingSettings crs, String fileName) {
        File json = new File(new File(Environment.getExternalStorageDirectory(), "AudioMoth"), fileName);
        String jsonCRS = serializeConfigToJSON(crs);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(json);
            out.write(jsonCRS.getBytes());
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) out.close();
            } catch (IOException ignore) {
            }
        }
       /* final Intent fileIntent = new Intent(android.content.Intent.ACTION_SEND);
        fileIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(json));
        fileIntent.setType("text/plain");*/

        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getString(R.string.app_name), jsonCRS);

        Intent clipboardIntent = new Intent();
        clipboardIntent.setAction(Intent.ACTION_SEND);
        clipboardIntent.putExtra(Intent.EXTRA_TEXT, jsonCRS);
        clipboardIntent.setType("text/plain");
        Intent chooserIntent = Intent.createChooser(clipboardIntent, getString(R.string.share));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{clipboardIntent});
        startActivity(chooserIntent);
    }

    private void copyToClipboard(ConabioRecordingSettings crs) {
        try {
            String dataToCopy = serializeConfigToJSON(crs);
            ClipboardManager clipboard = (ClipboardManager)
                    getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(getString(R.string.app_name), dataToCopy);
            clipboard.setPrimaryClip(clip);
        } catch (Exception ex) {
            Log.e(TAG, "copyToClipboard", ex);
        }
    }

    private String serializeConfigToJSON(ConabioRecordingSettings crs){
        crs.setDeviceInfo(rsViewModel.getRecordingSettings().getDeviceInfo());
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().registerTypeAdapter(ConabioRecordingSettings.class,new JsonSerializer<ConabioRecordingSettings>(){

            @Override
            public JsonElement serialize(ConabioRecordingSettings obj, Type typeOfSrc, JsonSerializationContext context) {
                JsonObject json = new JsonObject();
                json.add("deviceInfo",context.serialize(obj.getDeviceInfo()));
                json.add("timePeriods", context.serialize(obj.getTimePeriods()));
                json.add("ledEnabled",context.serialize(obj.isLedEnabled()));
                json.add("lowVoltageCutoffEnabled",context.serialize(obj.isLowVoltageCutoffEnabled()));
                json.add("batteryLevelCheckEnabled",context.serialize(obj.isBatteryLevelCheckEnabled()));
                json.add("sampleRate",context.serialize(obj.getSampleRate()));
                json.add("gain",context.serialize(obj.getGain()));
                json.add("recordDuration",context.serialize(obj.getRecordDuration()));
                json.add("sleepDuration",context.serialize(obj.getSleepDuration()));
                json.add("localTime",context.serialize(obj.isLocalTime()));
                json.add("firstRecordingDate",context.serialize(obj.getFirstRecordingDate()));
                json.add("lastRecordingDate",context.serialize(obj.getLastRecordingDate()));
                json.add("dutyEnabled",context.serialize(obj.isDutyEnabled()));
                json.add("passFiltersEnabled",context.serialize(obj.isPassFiltersEnabled()));
                json.add("filterType",context.serialize(obj.getFilterType()));
                json.add("lowerFilter",context.serialize(obj.getLowerFilter()));
                json.add("higherFilter",context.serialize(obj.getHigherFilter()));
                json.add("amplitudeThresholdingEnable",context.serialize(obj.isAmplitudeThresholdingEnabled()));
                json.add("amplitudeThreshold",context.serialize(obj.getAmplitudeThreshold()));
                json.add("code",context.serialize(obj.getCode()));
                return json;
            }
        }).registerTypeAdapter(DeviceInfo.class,new JsonSerializer<DeviceInfo>(){
            @Override
            public JsonElement serialize(DeviceInfo obj, Type typeOfSrc, JsonSerializationContext context) {
                JsonObject json = new JsonObject();
                json.add("date", context.serialize(obj.getDate()));
                json.add("deviceId", context.serialize(obj.getDeviceId()));
                json.add("firmwareVersion", context.serialize(obj.getFirmwareVersion()));
                json.add("battery",context.serialize(obj.getBattery()));
                return json;
            }
        })
                .setDateFormat("yyyy-MM-dd").create();
        String jsonCRS = gson.toJson(crs);
        return jsonCRS;
    }

}
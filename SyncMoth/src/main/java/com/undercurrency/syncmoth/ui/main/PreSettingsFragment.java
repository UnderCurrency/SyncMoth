package com.undercurrency.syncmoth.ui.main;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.undercurrency.audiomoth.usbhid.events.AudioMothConfigReceiveEvent;
import com.undercurrency.audiomoth.usbhid.events.DeviceDetachedEvent;
import com.undercurrency.audiomoth.usbhid.model.DeviceInfo;
import com.undercurrency.audiomoth.usbhid.model.LifeSpan;
import com.undercurrency.audiomoth.usbhid.model.RecordingSettings;
import com.undercurrency.audiomoth.usbhid.model.TimePeriods;
import com.undercurrency.syncmoth.R;
import com.undercurrency.syncmoth.model.RecordingSettingsViewModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;

import static com.undercurrency.syncmoth.MainActivity.SD32_GB;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PreSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PreSettingsFragment extends Fragment {

    private static final String TAG ="PreSettingsFragment";

    private TextView tvDate;
    private TextView tvDeviceId;
    private TextView tvFirmwareVersion;
    private TextView tvBattery;
    private TextView tvEstimatedConsumption;
    private Button btnUltrasound;
    private Button btnAudible;
    protected DeviceInfo deviceInfo;
    private RecordingSettingsViewModel rsViewModel;



    public PreSettingsFragment(){}

    public static PreSettingsFragment newInstance() {
        PreSettingsFragment fragment = new PreSettingsFragment();
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        syncUI();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rsViewModel =  new ViewModelProvider(this.getActivity()).get(RecordingSettingsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View infl  = inflater.inflate(R.layout.fragment_pre_settings, container, false);
        return  infl;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUI(view,savedInstanceState);
        syncUI();
    }

    private void initUI(View view,Bundle savedState){

       tvDate = (TextView) view.findViewById(R.id.tv_date);
       tvDeviceId = (TextView) view.findViewById(R.id.tv_device_id);
       tvFirmwareVersion = (TextView) view.findViewById(R.id.tv_firmware_version);
       tvBattery = (TextView) view.findViewById(R.id.tv_battery);
       tvEstimatedConsumption = (TextView) view.findViewById(R.id.tv_ec_pre);
       btnUltrasound = (Button) view.findViewById(R.id.btn_ultrasound);
       btnUltrasound.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
                loadConfiguration("ultrasonic.json");
                syncUI();
               Toast.makeText(getContext(), getString(R.string.ultrasonic_loaded), Toast.LENGTH_LONG).show();
           }
       });
       btnAudible = (Button) view.findViewById(R.id.btn_audible);
       btnAudible.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               loadConfiguration("audible.json");
               syncUI();
               Toast.makeText(getContext(), getString(R.string.audible_loaded), Toast.LENGTH_LONG).show();
           }
       });
    }


    public void onEvent(DeviceDetachedEvent event) {
        resetUI();
    }

    public void onEvent(AudioMothConfigReceiveEvent event) {
        syncUI();
    }

    public void resetUI(){
        tvDate.setText(getText(R.string.date_format));
        tvDeviceId.setText(getText(R.string.device_id_empty));
        tvFirmwareVersion.setText(getText(R.string.firmware_version_empty));
        tvBattery.setText(getText(R.string.battery_empty));
    }
    public void syncUI(){
        if(rsViewModel.getRecordingSettings()!=null && rsViewModel.getRecordingSettings().getDeviceInfo()!=null && rsViewModel.getRecordingSettings().getDeviceInfo().getDate()!=null){
            tvDate.setText(rsViewModel.getRecordingSettings().getDeviceInfo().getDate());
            tvDeviceId.setText(rsViewModel.getRecordingSettings().getDeviceInfo().getDeviceId());
            tvFirmwareVersion.setText(rsViewModel.getRecordingSettings().getDeviceInfo().getFirmwareVersion());
            tvBattery.setText(rsViewModel.getRecordingSettings().getDeviceInfo().getBattery());
        }
        syncLifeSpan();
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

    public void loadConfiguration(String configurationNameFile) {
        String jsonConfig = getStringFromAssets(this.getActivity(), configurationNameFile);
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        DeviceInfo deviceInfo = rsViewModel.getRecordingSettings().getDeviceInfo();
        RecordingSettings rsLoaded = gson.fromJson(jsonConfig,RecordingSettings.class);
        fixGsonDeserialization(rsLoaded);
        rsViewModel.setRecordingSettings(rsLoaded);
        rsViewModel.getRecordingSettings().setDeviceInfo(deviceInfo);
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
            Log.e("Error", e.getLocalizedMessage(),e);
        }
        return rawOutput.toString();
    }


    private void syncLifeSpan() {
        LifeSpan ls = LifeSpan.getLifeSpan(rsViewModel.getRecordingSettings());
        String form = null;
        String plural = getString(R.string.PLURAL);
        String singular = getString(R.string.SINGULAR);
        String upToFile = getString(R.string.UP_TO_FILE);
        String upToTotal = "";
        long totalDays = ls.getFileSizeBytes() > 0 ? SD32_GB / (ls.getTotalRecCount() * ls.getFileSizeBytes()) : 0;
        String label = null;
        if (ls.isPlural()) {
            form = getString(R.string.estimated_data_current_consumption_duty_on);
            label = String.format(form, ls.getTotalRecCount(),
                    ls.isUpTo() ? upToFile : upToTotal,
                    ls.getFileSizeInUnits(),
                    ls.getTotalMBFiles(),
                    ls.getEnergyUsed(),
                    totalDays,
                    totalDays == 0 ? plural : totalDays > 1 ? plural : singular);

        } else {
            form = getString(R.string.estimated_data_current_consumption_duty_off);
            label = String.format(form, ls.getTotalRecCount(),
                    ls.isUpTo() ? upToFile : upToTotal,
                    ls.getFileSizeInUnits(),
                    ls.getEnergyUsed(),
                    totalDays,
                    totalDays == 0 ? plural : totalDays > 1 ? plural : singular);

        }

        tvEstimatedConsumption.setText(label);
    }
}

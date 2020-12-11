package com.undercurrency.syncmoth.ui.main;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.undercurrency.audiomoth.usbhid.model.LifeSpan;
import com.undercurrency.syncmoth.R;
import com.undercurrency.syncmoth.events.ConfigLoadedEvent;
import com.undercurrency.syncmoth.model.RecordingSettingsViewModel;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.EventBusException;

import static com.undercurrency.syncmoth.MainActivity.SD32_GB;

public class RecordingSettingsFragment extends Fragment {

    private RecordingSettingsViewModel rsViewModel;

    private CheckBox chkEnableDuty;
    private EditText edtxnuSleepDuration;
    private EditText edtxnuRecordingDuration;
    private CheckBox ckbEnableLed;
    private CheckBox ckbEnableLowVoltageCutOff;
    private CheckBox ckbEnableBatteryLevelIndication;
    private RadioGroup rdgSampleRate;
    private RadioGroup rdgGain;
    protected EventBus eventBus;
    private TextView tvEstimatedConsumption;




    public RecordingSettingsFragment() {
    }

    public static RecordingSettingsFragment newInstance() {
        RecordingSettingsFragment fragment = new RecordingSettingsFragment();
        return fragment;
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
        startEventBus();
        rsViewModel =  new ViewModelProvider(this.getActivity()).get(RecordingSettingsViewModel.class);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recording_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        edtxnuSleepDuration = (EditText) view.findViewById(R.id.edtxnuSleepDuration);
        edtxnuSleepDuration.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int sleepDuration = 0;
                try {
                    sleepDuration = Integer.parseInt(s.toString());
                } catch (Exception ignored) {

                }
                rsViewModel.getRecordingSettings().setSleepDuration(sleepDuration);
                syncLifeSpan();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edtxnuRecordingDuration = (EditText) view.findViewById(R.id.edtxnuRecordingDuration);
        edtxnuRecordingDuration.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int recordingDuration = 0;
                try {
                    recordingDuration = Integer.parseInt(s.toString());
                } catch (Exception ignored) {

                }
                rsViewModel.getRecordingSettings().setRecordDuration(recordingDuration);
                syncLifeSpan();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        chkEnableDuty = (CheckBox) view.findViewById(R.id.chk_enable_duty);
        chkEnableDuty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                rsViewModel.getRecordingSettings().setDutyEnabled(checked);
                edtxnuRecordingDuration.setEnabled(checked);
                edtxnuSleepDuration.setEnabled(checked);
                syncLifeSpan();
            }
        });
        ckbEnableLed = (CheckBox) view.findViewById(R.id.ckbEnableLed);
        ckbEnableLed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                rsViewModel.getRecordingSettings().setLedEnabled(checked);
                syncLifeSpan();
            }
        });
        ckbEnableLowVoltageCutOff = (CheckBox) view.findViewById(R.id.ckbEnableLowVoltageCutOff);
        ckbEnableLowVoltageCutOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                rsViewModel.getRecordingSettings().setLowVoltageCutoffEnabled(checked);
                syncLifeSpan();
            }
        });
        ckbEnableBatteryLevelIndication = (CheckBox) view.findViewById(R.id.ckbEnableBatteryLevelIndication);
        ckbEnableBatteryLevelIndication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                rsViewModel.getRecordingSettings().setBatteryLevelCheckEnabled(checked);
                syncLifeSpan();
            }
        });

        rdgSampleRate = (RadioGroup) view.findViewById(R.id.rdgSampleRate);
        rdgSampleRate.setOnCheckedChangeListener((group, checkedId) -> {
                switch (checkedId){
                    case R.id.rb8hz:
                        rsViewModel.getRecordingSettings().setSampleRate(8000);
                        break;
                    case R.id.rb16hz:
                        rsViewModel.getRecordingSettings().setSampleRate(16000);
                        break;
                    case R.id.rb32hz:
                        rsViewModel.getRecordingSettings().setSampleRate(32000);
                        break;
                    case R.id.rb48hz:
                        rsViewModel.getRecordingSettings().setSampleRate(48000);
                        break;
                    case R.id.rb96hz:
                        rsViewModel.getRecordingSettings().setSampleRate(96000);
                        break;
                    case R.id.rb192hz:
                        rsViewModel.getRecordingSettings().setSampleRate(192000);
                        break;
                    case R.id.rb250hz:
                        rsViewModel.getRecordingSettings().setSampleRate(250000);
                        break;
                    case R.id.rb384hz:
                        rsViewModel.getRecordingSettings().setSampleRate(384000);
                        break;
                }
            syncLifeSpan();
        });

        rdgGain = (RadioGroup) view.findViewById(R.id.rdgGain);

        rdgGain.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId){
                case R.id.rbZero:
                    rsViewModel.getRecordingSettings().setGain((byte)0);
                    break;
                case R.id.rbOne:
                    rsViewModel.getRecordingSettings().setGain((byte)1);
                    break;
                case R.id.rbTwo:
                    rsViewModel.getRecordingSettings().setGain((byte)2);
                    break;
                case R.id.rbThree:
                    rsViewModel.getRecordingSettings().setGain((byte)3);
                    break;
                case R.id.rbFour:
                    rsViewModel.getRecordingSettings().setGain((byte)4);
                    break;
            }
        });
        tvEstimatedConsumption = (TextView) view.findViewById(R.id.tv_ec_rec_set);
        syncLifeSpan();
    }

    /**
     * Takes values from the viewmodel and upgrades the UI
     * Never call this method inside onTextChanged
     *
     */
    private void syncUI() {
        String recordingDuration = Integer.toString(rsViewModel.getRecordingSettings().getRecordDuration());
        edtxnuRecordingDuration.setText(recordingDuration);
        String sleepDuration = Integer.toString(rsViewModel.getRecordingSettings().getSleepDuration());
        edtxnuSleepDuration.setText(sleepDuration);
        chkEnableDuty.setChecked(rsViewModel.getRecordingSettings().isDutyEnabled());
        edtxnuSleepDuration.setEnabled(rsViewModel.getRecordingSettings().isDutyEnabled());
        edtxnuRecordingDuration.setEnabled(rsViewModel.getRecordingSettings().isDutyEnabled());

        ckbEnableLed.setChecked(rsViewModel.getRecordingSettings().isLedEnabled());
        ckbEnableLowVoltageCutOff.setChecked(rsViewModel.getRecordingSettings().isLowVoltageCutoffEnabled());
        ckbEnableBatteryLevelIndication.setChecked(rsViewModel.getRecordingSettings().isBatteryLevelCheckEnabled());
        int sampleRate =(int) rsViewModel.getRecordingSettings().getSampleRate()/1000;
        switch(sampleRate){
            case 8:
                RadioButton rb8hz = (RadioButton) getView().findViewById(R.id.rb8hz);
                rb8hz.setChecked(true);
                break;
            case 16:
                RadioButton rb16hz = (RadioButton) getView().findViewById(R.id.rb16hz);
                rb16hz.setChecked(true);
                break;
            case 32:
                RadioButton rb32hz = (RadioButton) getView().findViewById(R.id.rb32hz);
                rb32hz.setChecked(true);
                break;
            case 48:
                RadioButton rb48hz = (RadioButton) getView().findViewById(R.id.rb48hz);
                rb48hz.setChecked(true);
                break;
            case 96:
                RadioButton rb96hz = (RadioButton) getView().findViewById(R.id.rb96hz);
                rb96hz.setChecked(true);
                break;
            case 192:
                RadioButton rb192hz = (RadioButton) getView().findViewById(R.id.rb192hz);
                rb192hz.setChecked(true);
                break;
            case 250:
                RadioButton rb250hz = (RadioButton) getView().findViewById(R.id.rb250hz);
                rb250hz.setChecked(true);
                break;
            case 384:
                RadioButton rb384hz = (RadioButton) getView().findViewById(R.id.rb384hz);
                rb384hz.setChecked(true);
                break;

        }
        int gain = rsViewModel.getRecordingSettings().getGain();
        switch(gain){
            case 0:
                RadioButton rbZero = (RadioButton) getView().findViewById(R.id.rbZero);
                rbZero.setChecked(true);
                break;
            case 1:
                RadioButton rbOne = (RadioButton) getView().findViewById(R.id.rbOne);
                rbOne.setChecked(true);
                break;
            case 2:
                RadioButton rbTwo = (RadioButton) getView().findViewById(R.id.rbTwo);
                rbTwo.setChecked(true);
                break;
            case 3:
                RadioButton rbThree = (RadioButton) getView().findViewById(R.id.rbThree);
                rbThree.setChecked(true);
                break;
            case 4:
                RadioButton rbFour = (RadioButton) getView().findViewById(R.id.rbFour);
                rbFour.setChecked(true);
                break;
        }

    }

    private void syncLifeSpan(){
        LifeSpan ls = LifeSpan.getLifeSpan(rsViewModel.getRecordingSettings());
        String form = null;
        String plural = getString(R.string.PLURAL);
        String singular =getString(R.string.SINGULAR);;
        String upToFile=getString(R.string.UP_TO_FILE);
        String upToTotal="";
        long totalDays = (long) (ls.getFileSizeBytes()>0? SD32_GB / (ls.getTotalRecCount()*ls.getFileSizeBytes()):0);
        String label = null;
        if(ls.isPlural()){
            form = getString(R.string.estimated_data_current_consumption_duty_on);
            label = String.format(form,ls.getTotalRecCount(),
                    ls.isUpTo()?upToFile:upToTotal,
                    ls.getFileSizeInUnits(),
                    ls.getTotalMBFiles(),
                    ls.getEnergyUsed(),
                    totalDays,
                    totalDays==0?plural:totalDays>1?plural:singular);

        } else {
            form = getString(R.string.estimated_data_current_consumption_duty_off);
            label = String.format(form,ls.getTotalRecCount(),
                    ls.isUpTo()?upToFile:upToTotal,
                    ls.getFileSizeInUnits(),
                    ls.getEnergyUsed(),
                    totalDays,
                    totalDays==0?plural:totalDays>1?plural:singular);

        }

        tvEstimatedConsumption.setText(label);
    }

    public void onEvent(ConfigLoadedEvent event){
      syncUI();
    }

    private void startEventBus() {
        try {
            eventBus = EventBus.builder().logNoSubscriberMessages(false).sendNoSubscriberEvent(false).installDefaultEventBus();
        } catch (EventBusException e) {
            eventBus = EventBus.getDefault();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        eventBus.register(this);
    }

    @Override
    public void onStop() {
        eventBus.unregister(this);
        super.onStop();
    }
}
package com.undercurrency.syncmoth.ui.main;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.rizlee.rangeseekbar.RangeSeekBar;
import com.undercurrency.audiomoth.usbhid.model.DeviceInfo;
import com.undercurrency.audiomoth.usbhid.model.FilterType;
import com.undercurrency.audiomoth.usbhid.model.LifeSpan;
import com.undercurrency.syncmoth.R;
import com.undercurrency.syncmoth.events.ConfigLoadedEvent;
import com.undercurrency.syncmoth.model.RecordingSettingsViewModel;

import java.util.Arrays;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.EventBusException;

import static com.undercurrency.syncmoth.MainActivity.SD32_GB;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdvancedSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdvancedSettingsFragment extends Fragment {
    protected EventBus eventBus;
    int[] amplitudeThresholdValues = {0, 2, 4, 6, 8, 10, 12, 14, 16,
            18, 20, 22, 24, 26, 28, 30, 32,
            36, 40, 44, 48, 52, 56, 60, 64, 72,
            80, 88, 96, 104, 112, 120, 128, 144, 160,
            176, 192, 208, 224, 240, 256, 288, 320, 352,
            384, 416, 448, 480, 512, 576, 640, 704, 768,
            832, 896, 960, 1024, 1152, 1280, 1408, 1536, 1664,
            1792, 1920, 2048, 2304, 2560, 2816, 3072, 3328, 3584,
            3840, 4096, 4608, 5120, 5632, 6144, 6656, 7168, 7680,
            8192, 9216, 10240, 11264, 12288, 13312, 14336, 15360, 16384,
            18432, 20480, 22528, 24576, 26624, 28672, 30720, 32768
    };
    private RecordingSettingsViewModel rsViewModel;
    private DeviceInfo deviceInfo;
    private CheckBox chkEnableFiltering;
    private RadioGroup rdgFilterType;
    private RadioButton rbLow;
    private RadioButton rbMed;
    private RadioButton rbHigh;
    private RangeSeekBar rsbFreqFilter;
    private CheckBox chkEnableAmplitudeThresholdRecording;
    private SeekBar sbrAmplitudeThreshold;
    private TextView tvAmplitudeThresholdExplain;
    private TextView tvRecordingFiltering;
    private TextView tvEstimatedConsumption;
    private float minRange = 0.0f;
    private float maxRange = 0.0f;

    public AdvancedSettingsFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AdvancedSettingsFragment.
     */

    public static AdvancedSettingsFragment newInstance() {
        AdvancedSettingsFragment fragment = new AdvancedSettingsFragment();
        return fragment;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        syncUI();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chkEnableFiltering = view.findViewById(R.id.chk_enable_filtering);
        rdgFilterType = view.findViewById(R.id.rdg_filter_type);
        rbLow = view.findViewById(R.id.rbLow);
        rbMed = view.findViewById(R.id.rbMed);
        rbHigh = view.findViewById(R.id.rbHigh);
        rsbFreqFilter = view.findViewById(R.id.rsbFreqFilter);
        chkEnableAmplitudeThresholdRecording = view.findViewById(R.id.chk_enable_amplitude_threshold);
        tvAmplitudeThresholdExplain = view.findViewById(R.id.tv_amplitude_threshold_explain);
        sbrAmplitudeThreshold = view.findViewById(R.id.sbr_amplitude_thresholding);
        tvRecordingFiltering = view.findViewById(R.id.tv_recording_filtering);
        rdgFilterType.setEnabled(false);
        tvEstimatedConsumption = view.findViewById(R.id.tv_ec_adv);


        rsbFreqFilter.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return !chkEnableFiltering.isChecked();
            }
        });

        sbrAmplitudeThreshold.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return !chkEnableAmplitudeThresholdRecording.isChecked();
            }
        });


        chkEnableFiltering.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                rbLow.setEnabled(checked);
                rbMed.setEnabled(checked);
                rbHigh.setEnabled(checked);
                rdgFilterType.setEnabled(checked);
                rsViewModel.getRecordingSettings().setPassFiltersEnabled(checked);
                updateFilter(rsViewModel.getRecordingSettings().getFilterType(), getLowerFilter(), getHigherFilter());
                syncLifeSpan();
            }
        });


        rdgFilterType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rbLow:
                        rsViewModel.getRecordingSettings().setFilterType(FilterType.LOW);
                        rsbFreqFilter.setCurrentValues(0.0f, maxRange);
                        updateRecordingLabel();
                        break;
                    case R.id.rbMed:
                        rsViewModel.getRecordingSettings().setFilterType(FilterType.BAND);
                        updateRecordingLabel();
                        break;
                    case R.id.rbHigh:
                        float min = rsbFreqFilter.getCurrentValues().getLeftValue();
                        rsViewModel.getRecordingSettings().setFilterType(FilterType.HIGH);
                        rsbFreqFilter.setCurrentValues(min, maxRange);
                        updateRecordingLabel();
                        break;
                }
                updateFilterValues();
            }
        });
        chkEnableAmplitudeThresholdRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTvAmplitudeThresholdExplain();
                syncLifeSpan();
            }
        });

        sbrAmplitudeThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateTvAmplitudeThresholdExplain();
                setAmplitudeThreshold(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                boolean checked = ((CheckBox) view.findViewById(R.id.chk_enable_amplitude_threshold)).isChecked();
                updateTvAmplitudeThresholdExplain();
            }
        });

        rsbFreqFilter.setListenerRealTime(
                new RangeSeekBar.OnRangeSeekBarRealTimeListener() {
                    @Override
                    public void onValuesChanging(float left, float right) {
                        updateRecordingLabelBand(left, right);
                    }

                    @Override
                    public void onValuesChanging(int i, int i1) {
                    }
                }
        );
        rsbFreqFilter.setListenerPost(new RangeSeekBar.OnRangeSeekBarPostListener() {
            @Override
            public void onValuesChanged(float left, float right) {
                updateFilter(rsViewModel.getRecordingSettings().getFilterType(), left, right);
                updateFilterValues();
            }


            @Override
            public void onValuesChanged(int i, int i1) {

            }
        });
        syncUI();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startEventBus();
        rsViewModel = new ViewModelProvider(this.getActivity()).get(RecordingSettingsViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_advanced_settings, container, false);
    }


    private String calculateAmplitudeThreshold(int index) {
        Integer ampThreshold = amplitudeThresholdValues[index];
        String amplitude = String.format(getString(R.string.audio_recordings_amplitude), ampThreshold);
        return amplitude;
    }

    private void updateTvAmplitudeThresholdExplain() {
        boolean checked = chkEnableAmplitudeThresholdRecording.isChecked();
        if (checked) {
            int progress = sbrAmplitudeThreshold.getProgress();
            setAmplitudeThreshold(progress);
            Integer amplitudeThreshold = getAmplitudeThreshold();
            tvAmplitudeThresholdExplain.setText(calculateAmplitudeThreshold(progress));
        } else {
            tvAmplitudeThresholdExplain.setText(getText(R.string.audio_recordings_sd));
        }
    }

    private void updateFilter(FilterType filterType, float left, float right) {
        switch (filterType) {
            case BAND:
                updateRecordingLabel();
                break;
            case LOW:
                rsbFreqFilter.setCurrentValues(0.0f, right);
                updateRecordingLabel();
                break;
            case HIGH:
                rsbFreqFilter.setCurrentValues(left, maxRange);
                updateRecordingLabel();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + rsViewModel.getRecordingSettings().getFilterType());
        }
    }

    private void updateFilterValues() {
        setLowerFilter(rsbFreqFilter.getCurrentValues().getLeftValue());
        setHigherFilter(rsbFreqFilter.getCurrentValues().getRightValue());
    }

    private void updateRecordingLabelBand(float left, float right) {
        tvRecordingFiltering.setText(String.format(getText(R.string.recording_filtering).toString(), left, right));
    }

    private void updateRecordingLabel() {
        float min = rsbFreqFilter.getCurrentValues().getLeftValue();
        float max = rsbFreqFilter.getCurrentValues().getRightValue();
        tvRecordingFiltering.setText(String.format(getText(R.string.recording_filtering).toString(), min, max));

    }



    /**
     * Restores the view state
     */
    private void syncUI() {

        maxRange = rsViewModel.getRecordingSettings().getSampleRate()/2000;
        fixFilter();

        boolean enableFiltering = rsViewModel.getRecordingSettings().isPassFiltersEnabled();
        chkEnableFiltering.setChecked(enableFiltering);
        //Check filter ranges not to exceed boundaries
       if(getHigherFilter()>maxRange) {
           setHigherFilter(maxRange);
       }
       if(getLowerFilter()>maxRange) {
          setLowerFilter(0);
       }
       rsbFreqFilter.setCurrentValues(getLowerFilter(), getHigherFilter());


        if (rsViewModel.getRecordingSettings().getFilterType() != null) {
            rbLow.setEnabled(enableFiltering);
            rbHigh.setEnabled(enableFiltering);
            rbMed.setEnabled(enableFiltering);
            rbLow.setChecked(rsViewModel.getRecordingSettings().getFilterType().equals(FilterType.LOW));
            rbMed.setChecked(rsViewModel.getRecordingSettings().getFilterType().equals(FilterType.BAND));
            rbHigh.setChecked(rsViewModel.getRecordingSettings().getFilterType().equals(FilterType.HIGH));

            if (rsViewModel.getRecordingSettings().getFilterType() == FilterType.BAND) {
                updateRecordingLabel();
            } else if (rsViewModel.getRecordingSettings().getFilterType() == FilterType.LOW) {
                updateRecordingLabel();
            } else if (rsViewModel.getRecordingSettings().getFilterType() == FilterType.HIGH) {
                updateRecordingLabel();
            }

        }
        int thresholdIndex = getAmplitudeThresholdIndex();
        sbrAmplitudeThreshold.setProgress(thresholdIndex);
        updateTvAmplitudeThresholdExplain();
        chkEnableAmplitudeThresholdRecording.setChecked(rsViewModel.getRecordingSettings().isAmplitudeThresholdingEnabled());
        syncLifeSpan();
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


    public void onEvent(ConfigLoadedEvent event) {
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

    private float getLowerFilter() {
        return rsViewModel.getRecordingSettings().getLowerFilter() / 1000f;
    }

    private void setLowerFilter(float lowerFilter) {
        rsViewModel.getRecordingSettings().setLowerFilter((int) lowerFilter * 1000);
    }

    private float getHigherFilter() {
        return rsViewModel.getRecordingSettings().getHigherFilter() / 1000f;
    }

    private void setHigherFilter(float higherFilter) {
        rsViewModel.getRecordingSettings().setHigherFilter((int) higherFilter * 1000);
    }

    private int getAmplitudeThreshold() {
        return rsViewModel.getRecordingSettings().getAmplitudeThreshold();
    }

    private void setAmplitudeThreshold(int index) {
        rsViewModel.getRecordingSettings().setAmplitudeThreshold(amplitudeThresholdValues[index]);
    }

    private int getAmplitudeThresholdIndex() {
        int threshold = rsViewModel.getRecordingSettings().getAmplitudeThreshold();
        return Arrays.binarySearch(amplitudeThresholdValues, threshold);
    }

    private void fixFilter(){
        rsbFreqFilter.setRange(minRange,maxRange,0.1f);
        Integer iMaxRange = (int)maxRange;
        String sMaxRange = String.format(getText(R.string.kHzMax).toString(),iMaxRange);
        rsbFreqFilter.setRightText(sMaxRange);
    }
}
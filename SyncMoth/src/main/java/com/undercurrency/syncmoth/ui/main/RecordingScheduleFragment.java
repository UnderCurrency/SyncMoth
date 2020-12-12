/*
 * (c)  Copyright 2020 Undercurrency
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.undercurrency.syncmoth.ui.main;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.rizlee.rangeseekbar.RangeSeekBar;
import com.undercurrency.audiomoth.usbhid.model.LifeSpan;
import com.undercurrency.audiomoth.usbhid.model.TimePeriods;
import com.undercurrency.syncmoth.R;
import com.undercurrency.syncmoth.events.ConfigLoadedEvent;
import com.undercurrency.syncmoth.model.RecordingSettingsViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.EventBusException;

import static com.undercurrency.syncmoth.MainActivity.SD32_GB;

public class RecordingScheduleFragment extends Fragment {
    private static final String TAG="RecordingScheduleFgmnt";

    final Calendar aCalendar = Calendar.getInstance();
    final Calendar aTimeCalendar = Calendar.getInstance();
    ArrayAdapter<TimePeriods> adapter;
    int itemCount = 0;
    int maxItemCount = 4;
    int itemAtPosition = -1;
    private RecordingSettingsViewModel rsViewModel;
    private TextView txvRecordingPeriod;
    private Button btnAddRecording;
    private Button btnRemoveSelected;
    private Button btnClearAll;
    private CheckBox chkbFirstRecordingDate;
    private EditText edtxFirstRecordingDate;
    private CheckBox chkbLastRecordingDate;
    private EditText edtxLastRecordingDate;
    private ListView lstvwRecordingSchedule;

    private TextView tvEstimatedConsumption;
    private TextView tvFirstRecordingDate;
    private TextView tvLastRecordingDate;
    private EditText edtxStartTime;
    private EditText edtxEndTime;

    protected EventBus eventBus;


    public RecordingScheduleFragment() {
    }

    public static RecordingScheduleFragment newInstance() {
        RecordingScheduleFragment fragment = new RecordingScheduleFragment();
        return fragment;

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
        return inflater.inflate(R.layout.fragment_recording_schedule, container, false);

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new ArrayAdapter<TimePeriods>(getActivity(),
                android.R.layout.simple_list_item_1,
                rsViewModel.getRecordingSettings().getTimePeriods());
       // rsbRecordingSchedule = (RangeSeekBar) view.findViewById(R.id.rsbRecordingSchedule);
         // txvRecordingPeriod.setText(recordingSchedule(rsbRecordingSchedule.getCurrentValues()));
        tvFirstRecordingDate = (TextView) view.findViewById(R.id.txv_first_recording_date_utc);
        tvFirstRecordingDate.setText(String.format(getString(R.string.first_recording_date_utc), getCurrentTimeZone()));
        tvLastRecordingDate = (TextView) view.findViewById(R.id.txv_last_recording_date_utc);
        tvLastRecordingDate.setText(String.format(getString(R.string.last_recording_date_utc),getCurrentTimeZone()));
        tvEstimatedConsumption = (TextView) view.findViewById(R.id.tv_estimated_consumption);
        lstvwRecordingSchedule = (ListView) view.findViewById(R.id.lstvwRecordingSchedule);
        lstvwRecordingSchedule.setAdapter(adapter);
        lstvwRecordingSchedule.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                v.setSelected(true);
                itemAtPosition = position;
            }
        });



        btnAddRecording = (Button) view.findViewById(R.id.btnAddRecording);
        btnAddRecording.setOnClickListener(v -> {
            if (itemCount < maxItemCount) {
                try {
                    int lStart = stringToMin(edtxStartTime);
                    int lEnd = stringToMin(edtxEndTime);
                    boolean localTime = rsViewModel.getRecordingSettings().isLocalTime();
                    formatAndAddTime(lStart,lEnd,localTime);
                    adapter.notifyDataSetChanged();
                    syncUI();
                } catch (Exception e) {
                    Log.d(TAG,"btnAddRecording",e);
                }

            }
        });
        btnRemoveSelected = (Button) view.findViewById(R.id.btnRemoveSelected);
        btnRemoveSelected.setOnClickListener(v -> {
            if (itemAtPosition >= 0 && itemAtPosition < 4) {
                lstvwRecordingSchedule.setItemChecked(itemAtPosition, false);
                rsViewModel.getRecordingSettings().getTimePeriods().remove(itemAtPosition);
                itemCount--;
                adapter.notifyDataSetChanged();
                adapter.notifyDataSetInvalidated();
                lstvwRecordingSchedule.setAdapter(adapter);
                itemAtPosition = -1;
                syncUI();
            }
        });

        btnClearAll = (Button) view.findViewById(R.id.btnClearAll);


        btnClearAll.setOnClickListener(v -> {
            rsViewModel.getRecordingSettings().getTimePeriods().clear();
            itemAtPosition = -1;
            syncUI();
        });

        edtxStartTime = (EditText) view.findViewById(R.id.edtx_start_time);
        TimePickerDialog.OnTimeSetListener startTimeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                aTimeCalendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
                aTimeCalendar.set(Calendar.MINUTE,minute);
                SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.hour_format));
                edtxStartTime.setText(sdf.format(aTimeCalendar.getTime()));
            }
        };
        edtxStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog tpd = new TimePickerDialog(getContext(),TimePickerDialog.THEME_HOLO_LIGHT,startTimeListener, 0,0,true);
                tpd.show();
            }
        });

        edtxEndTime = (EditText) view.findViewById(R.id.edtx_end_time);
        TimePickerDialog.OnTimeSetListener endTimeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                aTimeCalendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
                aTimeCalendar.set(Calendar.MINUTE,minute);
                SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.hour_format));
                edtxEndTime.setText(sdf.format(aTimeCalendar.getTime()));
            }
        };
        edtxEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog tpd = new TimePickerDialog(getContext(),TimePickerDialog.THEME_HOLO_LIGHT,endTimeListener, 0,0,true);
                tpd.show();
            }
        });

        edtxFirstRecordingDate = (EditText) view.findViewById(R.id.edtx_first_recording_date);
        DatePickerDialog.OnDateSetListener firstRecordingDate = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                aCalendar.set(Calendar.YEAR, year);
                aCalendar.set(Calendar.MONTH, monthOfYear);
                aCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                updateFirstRecordingDateLabel();
            }

        };

        edtxFirstRecordingDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dtp = new DatePickerDialog(getContext(), firstRecordingDate, aCalendar
                        .get(Calendar.YEAR), aCalendar.get(Calendar.MONTH),
                        aCalendar.get(Calendar.DAY_OF_MONTH));
                DatePicker dp = dtp.getDatePicker();
                dp.setMinDate(new Date().getTime());
                dtp.show();

            }
        });


        edtxLastRecordingDate = (EditText) view.findViewById(R.id.edtx_last_recording_date);
        DatePickerDialog.OnDateSetListener lastRecordingDate = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                aCalendar.set(Calendar.YEAR, year);
                aCalendar.set(Calendar.MONTH, monthOfYear);
                aCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLastRecordingDateLabel();
            }

        };

        edtxLastRecordingDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dtp =
                new DatePickerDialog(getContext(), lastRecordingDate, aCalendar
                        .get(Calendar.YEAR), aCalendar.get(Calendar.MONTH),
                        aCalendar.get(Calendar.DAY_OF_MONTH));
                DatePicker dp = dtp.getDatePicker();
                if( rsViewModel.getRecordingSettings().getFirstRecordingDate()!=null){
                    dp.setMinDate(rsViewModel.getRecordingSettings().getFirstRecordingDate().getTime());
                } else {
                    dp.setMinDate(new Date().getTime());
                }
                dtp.show();
            }
        });

        chkbFirstRecordingDate = (CheckBox) view.findViewById(R.id.chkb_first_recording_date);
        chkbFirstRecordingDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                    edtxFirstRecordingDate.setEnabled(checked);
                    rsViewModel.getRecordingSettings().setFirstRecordingEnable(checked);
            }
        });
        chkbLastRecordingDate = (CheckBox) view.findViewById(R.id.chkb_last_recording_date);
        chkbLastRecordingDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked =((CheckBox)v).isChecked();
                edtxLastRecordingDate.setEnabled(checked);
                rsViewModel.getRecordingSettings().setLastRecordingEnable(checked);
            }
        });
        syncUI();
    }

    private int stringToMin(EditText edtx) {
        String dtString = edtx.getText().toString();
        String split[] = dtString.split(":");
        int hours = Integer.parseInt(split[0]);
        int minutes = Integer.parseInt(split[1]);
        return hours*60+minutes;
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





    private void updateFirstRecordingDateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.schedule_date_format));
        edtxFirstRecordingDate.setText(sdf.format(aCalendar.getTime()));
        long timeWithMilliseconds = aCalendar.getTime().getTime();
        long dateWithOutMilliseconds = 1000L*(timeWithMilliseconds/1000L);
        Date dtWithOutMillis = new Date(dateWithOutMilliseconds);
        rsViewModel.getRecordingSettings().setFirstRecordingDate(dtWithOutMillis);
    }
    private void updateLastRecordingDateLabel(){
        SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.schedule_date_format));
        edtxLastRecordingDate.setText(sdf.format(aCalendar.getTime()));
        long timeWithMilliseconds = aCalendar.getTime().getTime();
        long dateWithOutMilliseconds = 1000L*(timeWithMilliseconds/1000L);
        Date dtWithOutMillis = new Date(dateWithOutMilliseconds);
        rsViewModel.getRecordingSettings().setLastRecordingDate(dtWithOutMillis);
    }

    private String floatToHour(float number) {
        int hour = (int) number;
        float fMinute = number - hour;
        int minute = (int) (60 * fMinute);
        return String.format(getString(R.string.format_hour), hour, minute);
    }

    private int floatToMin(float number){
        int hour =(int) number;
        float fMinute = number-hour;
        int minute = (int)(60*fMinute);
        int hourMin = hour*60;
        return hourMin+minute;
    }

    private String recordingSchedule(float start, float end){
        int hourStart = (int) start;
        float fMinStart = start - hourStart;
        int minStart = (int) (60 * fMinStart);
        int hourEnd = (int) end;
        float fMinEnd = end - hourEnd;
        int minEnd = (int) (60 * fMinEnd);
       return String.format(getString(R.string.format_shedule), hourStart, minStart, hourEnd, minEnd);
    }

    private String recordingSchedule(RangeSeekBar.Range range){
        return recordingSchedule(range.getLeftValue(),range.getRightValue());
    }

    private void syncUI(){
        itemCount = rsViewModel.getRecordingSettings().getTimePeriods().size();
        adapter.notifyDataSetChanged();
       SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.schedule_date_format));
       if(rsViewModel.getRecordingSettings().getFirstRecordingDate()!=null) {
           String firstRecordingDate = sdf.format(rsViewModel.getRecordingSettings().getFirstRecordingDate());
           edtxFirstRecordingDate.setText(firstRecordingDate);
       }
       if(rsViewModel.getRecordingSettings().getLastRecordingDate()!=null) {
           String lastRecordingDate = sdf.format(rsViewModel.getRecordingSettings().getLastRecordingDate());
           edtxFirstRecordingDate.setText(lastRecordingDate);
       }
       boolean isFirstRecordingDateEnabled = rsViewModel.getRecordingSettings().isFirstRecordingEnable();
       boolean isLastRecordingDateEnabled = rsViewModel.getRecordingSettings().isLastRecordingEnable();
       chkbFirstRecordingDate.setChecked(isFirstRecordingDateEnabled);
       chkbLastRecordingDate.setChecked(isLastRecordingDateEnabled);
       edtxFirstRecordingDate.setEnabled(isFirstRecordingDateEnabled);
       edtxLastRecordingDate.setEnabled(isLastRecordingDateEnabled);
        tvFirstRecordingDate.setText(String.format(getString(R.string.first_recording_date_utc), getCurrentTimeZone()));
        tvLastRecordingDate.setText(String.format(getString(R.string.last_recording_date_utc),getCurrentTimeZone()));
       syncLifeSpan();
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

    private int getCurrentTimeZone(){
        TimeZone tzdata = TimeZone.getDefault();
        int tzInt = tzdata.getOffset(Calendar.ZONE_OFFSET);
        int offset = tzInt / 3600000;
        return offset;
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

    private boolean between (int i, int min, int max){
        return i >= min && i <= max;
    }

    private boolean overlaps(int startTime1, int endTime1, int startTime2, int endTime2){
        return (startTime1 <= endTime2 && endTime1 >= startTime2);
    }


    private int convertTimetoUTC (int time) {


        /* Offset is given as UTC - local time in minutes */
        TimeZone tzdata = TimeZone.getDefault();
        int tzInt = tzdata.getOffset(Calendar.ZONE_OFFSET);
        int timezoneOffset = tzInt / 60000;


        time = (time - timezoneOffset) % 1440;

        /* If time zone offset move time over midnight */

        if (time < 0) {

            time += 1440;

        }

        return time;

    }

    private boolean formatAndAddTime(int startMin, int endMin, boolean localtime){
        boolean added = false;
        int startTimestamp, endTimestamp;
        if(localtime) {
             startTimestamp = convertTimetoUTC(startMin);
             endTimestamp = convertTimetoUTC(endMin);
        } else {
            startTimestamp = startMin %1440;
            endTimestamp = endMin %1440;

        }

        if (endTimestamp < startTimestamp) {
            endTimestamp += 1440;
        }
        if (endTimestamp == startTimestamp) {
            added = addTime(0, 1440,localtime);
        } else if (endTimestamp > 1440) {
            /* Split time period into two periods either side of midnight */
            added = addTime(startTimestamp, 1440,localtime);
            if (rsViewModel.getRecordingSettings().getTimePeriods().size() < maxItemCount) {
                added = addTime(0, endTimestamp - 1440,localtime) && added;
            }
        } else {
            added = addTime(startTimestamp, endTimestamp, localtime);
        }
        Collections.sort(rsViewModel.getRecordingSettings().getTimePeriods());
        return added;
    }


    private boolean addTime(int startMins, int endMins, boolean localtime){
         ArrayList<TimePeriods> timePeriods = (ArrayList<TimePeriods>)rsViewModel.getRecordingSettings().getTimePeriods();
        for(int i=0; i<timePeriods.size();i++){
            if(overlaps(timePeriods.get(i).getStartMins(),timePeriods.get(i).getEndMins(),startMins,endMins)){
                if(timePeriods.size()<maxItemCount){
                    int newStart = Math.min(startMins,timePeriods.get(i).getStartMins());
                    int newEnd = Math.max(endMins,timePeriods.get(i).getEndMins());
                    timePeriods.remove(i);
                    rsViewModel.getRecordingSettings().setTimePeriods(timePeriods);
                    return addTime(newStart,newEnd,localtime);
                }
                return false;
            }
        }
        rsViewModel.getRecordingSettings().getTimePeriods().add(new TimePeriods(startMins,endMins,localtime));
        return true;

    }
}
/*
 *
 *  (c)  Copyright 2020 Undercurrency
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
 *
 */

package com.undercurrency.syncmoth.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.undercurrency.audiomoth.usbhid.model.DeviceInfo;
import com.undercurrency.audiomoth.usbhid.model.FilterType;
import com.undercurrency.audiomoth.usbhid.model.RecordingSettings;
import com.undercurrency.audiomoth.usbhid.model.TimePeriods;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;


/**
 * RecordingSettings a POJO holding all the AudioMoth settings
 */
public class ConabioRecordingSettings implements Serializable {

    private static final long serialVersionUID = 8799656478674716777L;

    private static final int MAX_PERIODS = 5;
    private static final int SECONDS_IN_DAY = 86400;
    private static final int UINT16_MAX = 0xFFFF;
    private static final int UINT32_MAX = 0xFFFFFFFF;
    boolean amplitudeThresholdingEnabled;
    private DeviceInfo deviceInfo;
    private ArrayList<TimePeriods> timePeriods = new ArrayList(5);
    private boolean ledEnabled;
    private boolean lowVoltageCutoffEnabled;
    private boolean batteryLevelCheckEnabled;
    private int sampleRate;
    private byte gain;
    private int recordDuration;
    private int sleepDuration;
    private boolean localTime;
    private boolean dutyEnabled;
    private boolean passFiltersEnabled;
    private FilterType filterType;
    private int lowerFilter;
    private int higherFilter;
    private int amplitudeThreshold;
    private Date firstRecordingDate;
    private Date lastRecordingDate;
    private String code;



    public ConabioRecordingSettings() {
    }

    public ConabioRecordingSettings(RecordingSettings rs){
     setDeviceInfo(rs.getDeviceInfo());
        calculateCode(rs);
        this.timePeriods = new ArrayList<TimePeriods>(rs.getTimePeriods());
        this.ledEnabled = rs.isLedEnabled();
        this.lowVoltageCutoffEnabled = rs.isLowVoltageCutoffEnabled();
        this.batteryLevelCheckEnabled = rs.isBatteryLevelCheckEnabled();
        this.sampleRate = rs.getSampleRate();
        this.gain = rs.getGain();
        this.recordDuration = rs.getRecordDuration();
        this.sleepDuration =rs.getSleepDuration();
        this.localTime = rs.isLocalTime();
        this.dutyEnabled = rs.isDutyEnabled();
        this.passFiltersEnabled = rs.isPassFiltersEnabled();
        this.filterType = rs.getFilterType();
        this.lowerFilter = rs.getLowerFilter();
        this.higherFilter = rs.getHigherFilter();
        this.amplitudeThresholdingEnabled = rs.getAmplitudeThreshold()>0;
        this.amplitudeThreshold = rs.getAmplitudeThreshold();
        this.firstRecordingDate = rs.getFirstRecordingDate();
        this.lastRecordingDate = rs.getLastRecordingDate();
    }


    private  String getSha1Hex(String clearString)
    {
        try
        {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(clearString.getBytes("UTF-8"));
            byte[] bytes = messageDigest.digest();
            StringBuilder buffer = new StringBuilder();
            for (byte b : bytes)
            {
                buffer.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return buffer.toString();
        }
        catch (Exception ignored)
        {
            ignored.printStackTrace();
            return null;
        }
    }


    private void calculateCode(RecordingSettings rs){
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
        String jsonRecordingSettings= gson.toJson(rs);
           this.setCode(getSha1Hex(jsonRecordingSettings));
    }

    public ArrayList<TimePeriods> getTimePeriods() {
        return timePeriods;
    }

    public void setTimePeriods(ArrayList<TimePeriods> timePeriods) {
        this.timePeriods = timePeriods;
    }

    public boolean isLedEnabled() {
        return ledEnabled;
    }

    public void setLedEnabled(boolean ledEnabled) {
        this.ledEnabled = ledEnabled;
    }

    public boolean isLowVoltageCutoffEnabled() {
        return lowVoltageCutoffEnabled;
    }

    public void setLowVoltageCutoffEnabled(boolean lowVoltageCutoffEnabled) {
        this.lowVoltageCutoffEnabled = lowVoltageCutoffEnabled;
    }

    public boolean isBatteryLevelCheckEnabled() {
        return batteryLevelCheckEnabled;
    }

    public void setBatteryLevelCheckEnabled(boolean batteryLevelCheckEnabled) {
        this.batteryLevelCheckEnabled = batteryLevelCheckEnabled;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public byte getGain() {
        return gain;
    }

    public void setGain(byte gain) {
        this.gain = gain;
    }

    public int getRecordDuration() {
        return recordDuration;
    }

    public void setRecordDuration(int recordDuration) {
        this.recordDuration = recordDuration;
    }

    public int getSleepDuration() {
        return sleepDuration;
    }

    public void setSleepDuration(int sleepDuration) {
        this.sleepDuration = sleepDuration;
    }

    public boolean isLocalTime() {
        return localTime;
    }

    public void setLocalTime(boolean localTime) {
        this.localTime = localTime;
    }

    public boolean isDutyEnabled() {
        return dutyEnabled;
    }

    public void setDutyEnabled(boolean dutyEnabled) {
        this.dutyEnabled = dutyEnabled;
    }

    public boolean isPassFiltersEnabled() {
        return passFiltersEnabled;
    }

    public void setPassFiltersEnabled(boolean passFiltersEnabled) {
        this.passFiltersEnabled = passFiltersEnabled;
    }

    public FilterType getFilterType() {
        return filterType;
    }

    public void setFilterType(FilterType filterType) {
        this.filterType = filterType;
    }

    public int getLowerFilter() {
        return lowerFilter;
    }

    public void setLowerFilter(int lowerFilter) {
        this.lowerFilter = lowerFilter;
    }

    public int getHigherFilter() {
        return higherFilter;
    }

    public void setHigherFilter(int higherFilter) {
        this.higherFilter = higherFilter;
    }

    public boolean isAmplitudeThresholdingEnabled() {
        return amplitudeThresholdingEnabled;
    }

    public void setAmplitudeThresholdingEnabled(boolean amplitudeThresholdingEnabled) {
        this.amplitudeThresholdingEnabled = amplitudeThresholdingEnabled;
    }

    public int getAmplitudeThreshold() {
        return amplitudeThreshold;
    }

    public void setAmplitudeThreshold(int amplitudeThreshold) {
        this.amplitudeThreshold = amplitudeThreshold;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;


    }

    public Date getFirstRecordingDate() {
        return firstRecordingDate;
    }

    public void setFirstRecordingDate(Date firstRecordingDate) {
        this.firstRecordingDate = firstRecordingDate;
    }

    public Date getLastRecordingDate() {
        return lastRecordingDate;
    }

    public void setLastRecordingDate(Date lastRecordingDate) {
        this.lastRecordingDate = lastRecordingDate;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}


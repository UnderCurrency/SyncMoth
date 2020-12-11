package com.undercurrency.syncmoth.model;

import androidx.lifecycle.ViewModel;

import com.undercurrency.audiomoth.usbhid.model.DeviceInfo;
import com.undercurrency.audiomoth.usbhid.model.RecordingSettings;

public class RecordingSettingsViewModel extends ViewModel {

    private RecordingSettings recordingSettings;
    private DeviceInfo deviceInfo;

    public RecordingSettingsViewModel() {
        this.recordingSettings = new RecordingSettings();
        recordingSettings.setLocalTime(true);
    }

    public RecordingSettings getRecordingSettings() {
        return recordingSettings;
    }

    public void setRecordingSettings(RecordingSettings recordingSettings) {
        this.recordingSettings = recordingSettings;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

}

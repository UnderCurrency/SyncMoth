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

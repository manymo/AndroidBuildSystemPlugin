/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.manymo;

import com.android.annotations.NonNull;
import com.android.builder.testing.api.DeviceConnector;
import com.android.builder.testing.api.DeviceException;
import com.android.builder.testing.api.DeviceProvider;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.utils.ILogger;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 */
public class ManymoDeviceProvider extends DeviceProvider {

    @NonNull
    private final String adbExe;
    @NonNull
    private final ManymoExtension manymoExtension;
    @NonNull
    private final CommandHelper commandHelper;
    private AndroidDebugBridge bridge;

    ManymoDeviceProvider(@NonNull String adbExe, @NonNull ManymoExtension manymoExtension,
                         @NonNull ILogger logger) {
        this.adbExe = adbExe;
        this.manymoExtension = manymoExtension;
        commandHelper = new CommandHelper(adbExe, logger);
    }

    @Override
    public String getName() {
        return ManymoPlugin.PLUGIN_NAME;
    }

    @Override
    public void init() throws DeviceException {
        AndroidDebugBridge.initIfNeeded(false /*clientSupport*/);

        bridge = AndroidDebugBridge.createBridge(
                adbExe, false /*forceNewBridge*/);

        if (bridge == null) {
            throw new RuntimeException("Unable to connect to adb");
        }
    }

    @Override
    public void terminate() throws DeviceException {
        // nothing to do here for now.
    }

    @Override
    @NonNull
    public List<? extends DeviceConnector> getDevices() {
        try {
            List<String> deviceListData = commandHelper.runCommand(null, "list");
            Map<String, DeviceData> deviceDataMap = new ListParser().parseDevices(deviceListData);

            Collection<String> deviceIds = manymoExtension.getDevices();
            if (deviceIds.isEmpty()) {
                deviceIds = deviceDataMap.keySet();
            }

            List<ManymoDeviceConnector> devices = Lists.newArrayListWithCapacity(deviceIds.size());

            for (String deviceId : deviceIds) {
                DeviceData deviceData = deviceDataMap.get(deviceId);
                if (deviceData == null) {
                    throw new RuntimeException("Failed to find device: " + deviceId);
                }

                ManymoDeviceConnector device = new ManymoDeviceConnector(deviceId, deviceData,
                        commandHelper, bridge);
                devices.add(device);
            }

            return devices;
        } catch (InterruptedException ignored) {
        }

        return Collections.emptyList();
    }

    @Override
    public int getTimeout() {
        return manymoExtension.getTimeOut() * 1000;
    }

    @Override
    public int getMaxThreads() {
        return manymoExtension.getMaxThreads();
    }

    @Override
    public boolean isConfigured() {
        String userHome = System.getProperty("user.home");
        if (userHome == null) {
            return false;
        }

        File manymo = new File(userHome, ".manymo");
        if (!manymo.isDirectory()) {
            return false;
        }

        File auth_token = new File(manymo, "auth_token");
        if (!auth_token.isFile()) {
            return false;
        }

        return true;
    }
}

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
import com.android.ddmlib.*;
import com.android.utils.ILogger;
import com.google.common.base.Joiner;
import java.util.concurrent.TimeUnit;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 */
public class ManymoDeviceConnector extends DeviceConnector {

    @NonNull
    private final String deviceId;
    @NonNull
    private final DeviceData deviceData;
    @NonNull
    private final CommandHelper commandHelper;
    @NonNull
    private final AndroidDebugBridge bridge;

    private IDevice device;

    public ManymoDeviceConnector(@NonNull String deviceId, @NonNull DeviceData deviceData,
                                 @NonNull CommandHelper commandHelper,
                                 @NonNull AndroidDebugBridge bridge) {
        checkArgument(deviceId.equals(deviceData.getName()));
        this.deviceId = deviceId;
        this.deviceData = deviceData;
        this.commandHelper = commandHelper;
        this.bridge = bridge;
    }

    @Override
    public String getName() {
        return deviceId;
    }

    @Override
    public void connect(int timeOut, ILogger logger) throws TimeoutException {
        try {
            List<String> stdout;

            DdmPreferences.setTimeOut(30000);

            stdout = commandHelper.runCommand(
                    String.format("Connecting to '%s'", deviceId),
                    "launch", deviceId);

            // parse output to get serial number
            String serialNumber = parseSerial(stdout);

            logger.verbose("FOUND %s << %s", serialNumber, deviceId);

            int sleepTime = 500;
            mainLoop: while (device == null && timeOut > 0) {
                IDevice[] devices = bridge.getDevices();
                for (IDevice d : devices) {
                    if (serialNumber.equals(d.getSerialNumber())) {
                        device = d;
                        break mainLoop;
                    }
                }
                Thread.sleep(sleepTime);
                timeOut -= sleepTime;
            }

            if (device == null) {
                throw new TimeoutException(String.format("TimeOut: Unable to find device '%s' with serial number '%s'", deviceId, serialNumber));
            }

            // then wait for it to show up as online
            while (!device.isOnline() && timeOut > 0) {
                Thread.sleep(sleepTime);
                timeOut -= sleepTime;
            }

            if (!device.isOnline()) {
                throw new TimeoutException(String.format("TimeOut: Device '%s' with serial number '%s' has not come up online.", deviceId, serialNumber));
            }

        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public void disconnect(int timeOut, ILogger logger) throws TimeoutException {
        if (device == null) {
            return;
        }

        try {
            commandHelper.runCommand(
                    "Shuting down " + deviceId,
                    "shutdown", device.getSerialNumber());
        } catch (InterruptedException ignored) {
        } finally {
            device = null;
        }
    }

    @Override
    public void installPackage(@NonNull File apkFile, int timeout, ILogger logger) throws DeviceException {
        checkNotNull(device, "device is null. connect() probably not called.");
        try {
            device.installPackage(apkFile.getAbsolutePath(), true /*reinstall*/);
        } catch (InstallException e) {
            throw new DeviceException(e);
        }
    }

    @Override
    public void uninstallPackage(@NonNull String packageName, int timeout, ILogger logger) throws DeviceException {
        checkNotNull(device, "device is null. connect() probably not called.");
        try {
            device.uninstallPackage(packageName);
        } catch (InstallException e) {
            throw new DeviceException(e);
        }
    }

  @Override
  public void pullFile(String remote, String local) throws IOException {
    try {
      device.pullFile(remote, local);
    } catch (AdbCommandRejectedException e) {
      e.printStackTrace();
    } catch (TimeoutException e) {
      e.printStackTrace();
    } catch (SyncException e) {
      e.printStackTrace();
    }
  }

  @Override
    public int getApiLevel() {
        return deviceData.getApiLevel();
    }

  @Override
  public String getApiCodeName() {
    return null;
  }

  @Override
  public List<String> getAbis() {
    return deviceData.getAbis();
  }

  @Override
    public int getDensity() {
        return deviceData.getDensity();
    }

    @Override
    public int getHeight() {
        return deviceData.getHeight();
    }

    @Override
    public int getWidth() {
        return deviceData.getWidth();
    }

    @Override
    public void executeShellCommand(String command, IShellOutputReceiver receiver,
                                    long maxTimeToOutputResponse, TimeUnit maxTimeUnits)
            throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException,
            IOException {
        checkNotNull(device, "device is null. connect() probably not called.");

        device.executeShellCommand(command, receiver, maxTimeToOutputResponse, maxTimeUnits);
    }

    private String parseSerial(List<String> stdout) {
        for (String line : stdout) {
            if (line.startsWith("Emulator launched; local serial number is: ")) {
                return line.substring("Emulator launched; local serial number is: ".length());
            }
        }

        // did not find the serial number!
        throw new RuntimeException(Joiner.on('\n').join(stdout));
    }
}

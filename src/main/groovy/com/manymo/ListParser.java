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
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 */
public class ListParser {

    public ListParser() {

    }

    @NonNull
    Map<String, DeviceData> parseDevices(String data) {
        return parseDevices(Splitter.on('\n').split(data));
    }

    @NonNull
    Map<String, DeviceData> parseDevices(@NonNull Iterable<String> lines) {
        Map<String, DeviceData> map = Maps.newHashMap();

        // TODO: this code is ugly.

        String name = null;
        int apiLevel = 0;
        int density = 0;
        int height = 0;
        int width = 0;
        String abi = null;

        for (String line : lines) {

            if (line.isEmpty()) {
                continue;
            }

            if (line.charAt(0) == '\t') {
                line = line.trim();
                if (line.startsWith("sdk_version:")) {
                    apiLevel = Integer.valueOf(line.substring("sdk_version:".length()).trim());
                } else if (line.startsWith("device_display_density:")) {
                    density = Integer.valueOf(line.substring("device_display_density:".length()).trim());
                } else if (line.startsWith("device_display_height:")) {
                    height = Integer.valueOf(line.substring("device_display_height:".length()).trim());
                } else if (line.startsWith("device_display_width:")) {
                    width = Integer.valueOf(line.substring("device_display_width:".length()).trim());
                } else if (line.startsWith("abi:")) {
                    abi = line.substring("abi:".length()).trim();
                }
            } else if (line.startsWith("name:")) {
                if (name != null) {
                    map.put(name, new DeviceData(name, apiLevel, density, height, width, abi));
                }

                name = line.substring("name:".length()).trim();
            }
        }

        if (name != null) {
            map.put(name, new DeviceData(name, apiLevel, density, height, width, abi));
        }

        return map;
    }
}

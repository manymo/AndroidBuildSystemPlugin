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

package com.manymo

import com.android.annotations.NonNull
import com.google.common.collect.Lists

/**
 */
class ManymoExtension {

    int timeOut
    int maxThreads = 3

    @NonNull
    final List<String> devices = Lists.newArrayList()

    ManymoExtension() {
    }

    void device(String deviceId) {
        devices.add(deviceId)
    }

    void devices(String... deviceIds) {
        devices.addAll(deviceIds)
    }

    void setDevices(String... deviceIds) {
        devices.clear()
        devices.addAll(deviceIds)
    }
}

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

import java.util.ArrayList;
import java.util.List;

/**
 */
public class DeviceData {

    @NonNull
    private final String name;
    private final int apiLevel;
    private final int density;
    private final int height;
    private final int width;
    @NonNull
    private final List<String> abis;

    public DeviceData(@NonNull String name, int apiLevel,
                      int density, int height, int width,
                      @NonNull String abi) {
        this.name = name;
        this.apiLevel = apiLevel;
        this.density = density;
        this.height = height;
        this.width = width;
        this.abis = new ArrayList<String>();
        this.abis.add(abi);
    }

    @NonNull
    public String getName() {
        return name;
    }

    public int getApiLevel() {
        return apiLevel;
    }

    public int getDensity() {
        return density;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    @NonNull
    public List<String> getAbis() {
      return abis;
    }
}

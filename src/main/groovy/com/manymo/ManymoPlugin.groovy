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

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 */
class ManymoPlugin implements Plugin<Project> {

    public static final String PLUGIN_NAME = 'manymo'

    private ManymoExtension extension

    @Override
    void apply(Project project) {

        if (!project.plugins.hasPlugin('android') &&
                !project.plugins.hasPlugin('android-library')) {
            throw new GradleException('The android or android-library has not been applied yet')
        }

        extension = project.extensions.create(PLUGIN_NAME, ManymoExtension)

        project.android.deviceProvider(
                new ManymoDeviceProvider(project.android.adbExe.absolutePath, extension, project.android.logger))
    }
}

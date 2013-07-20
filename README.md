Gradle plugin for running instrumentation tests on Manymo cloud emulators from the Android SDK build system.

You'll need to install and configure the [manymo command line tool](https://www.manymo.com/pages/documentation/manymo-command-line-tool) before using the plugin.

To configure Gradle to run your Android Instrumentation tests in the cloud using Manymo, edit the build.gradle file to include the dependencies, apply the plugin and configure which devices to test on as illustrated in the following example:

    buildscript {
        repositories {
            mavenCentral()
        }
        dependencies {
            classpath 'com.android.tools.build:gradle:0.4'
            classpath 'com.manymo:gradle:1.0'
        }
    }
    
    apply plugin: 'android'
    apply plugin: 'manymo'
    
    android {
        //...
    }
    
    manymo {
        // list of device to test on. type "manymo list" for a complete list
        // not providing devices will test on all available devices that are compatible with
        // the app (based on minSdkVersion)
        devices "17_WXGA800-7in_x86", "17_WXGA720_x86"
    
        timeOut 30
    }

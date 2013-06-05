Gradle plugin to deploys tests to Manymo cloud emulators from the Android SDK build system.

To use, this requires installing the [manymo command line tool](https://www.manymo.com/pages/documentation/manymo-command-line-tool), as well as
installing the authentication token.

A typical project build.gradle will look like this:

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
        // list of device to test on. Not providing devices will test on all
        // available devices available on manymo.com that are compatible with
        // the app (based on minSdkVersion)
        devices "17_WXGA800-7in_x86", "17_WXGA720_x86"
    
        timeOut 30
    }

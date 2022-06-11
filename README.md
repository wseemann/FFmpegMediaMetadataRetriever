FFmpegMediaMetadataRetriever
============================

View the project page <a href=http://wseemann.github.io/FFmpegMediaMetadataRetriever/>here</a>.

Donations
------------

Donations can be made via PayPal:

**This project needs you!** If you would like to support this project's further development, the creator of this project or the continuous maintenance of this project, **feel free to donate**. Donations are highly appreciated. Thank you!

**PayPal**

- [**Choose what you want to donate**](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=2BDTFVEW9LFZY), all donations are awesome!

Overview
--------

FFmpegMediaMetadataRetriever is a reimplementation of Android's MediaMetadataRetriever class. The FFmpegMediaMetadataRetriever class provides a unified interface for retrieving frame and meta data from an input media file and uses FFmpeg as its backend.

This project uses FFmpeg version 4.2.2.

Key Features:
* ARMv7, x86, x86_64 and ARM_64 support (Note: ARM and MIPS aren't supported as of version 1.0.14)
* Support for API 12+
* URL support (Unlike MediaMetadataRetriever, see: http://code.google.com/p/android/issues/detail?id=35794)

Supported protocols:
* file, http, https, mms, mmsh and rtmp

Supported formats (audio and video):
* aac, acc+, avi, flac, mp2, mp3, mp4, ogg, 3gp and more!

Additional support for:
* ICY Metadata (SHOUTcast metadata)

Demo Application
------------

If you would like to try FFmpegMediaMetadataRetriever you can do so using the [Demo Application](https://github.com/wseemann/FFmpegMediaMetadataRetriever/blob/master/FMMRDemo.apk)

Using FMMR in your application (Android Studio)
------------

Add the following maven dependency to your project's `build.gradle` file:

    dependencies {
        implementation 'com.github.wseemann:FFmpegMediaMetadataRetriever-core:1.0.16'
        implementation 'com.github.wseemann:FFmpegMediaMetadataRetriever-native:1.0.16'
    }

Optionally, to support individual ABIs:

    dependencies {
        implementation 'com.github.wseemann:FFmpegMediaMetadataRetriever-core:1.0.16'
        implementation 'com.github.wseemann:FFmpegMediaMetadataRetriever-native-armeabi-v7a:1.0.16'
        implementation 'com.github.wseemann:FFmpegMediaMetadataRetriever-native-x86:1.0.16'
        implementation 'com.github.wseemann:FFmpegMediaMetadataRetriever-native-x86_64:1.0.16'
        implementation 'com.github.wseemann:FFmpegMediaMetadataRetriever-native-arm64-v8a:1.0.16'
    }

or, if your application supports individual architectures extract the appropriate AAR file into you projects "libs" folder:

[Prebuilt AARs](https://github.com/wseemann/FFmpegMediaMetadataRetriever/releases/download/v1.0.16/prebuilt-aars.zip)

Demo Application
------------

A sample application that makes use of FFmpegMediaMetadataRetriever can be downloaded [here](https://github.com/wseemann/FFmpegMediaMetadataRetriever/blob/master/FMMRDemo.apk?raw=true). Note: The sample application is compiled with support for ALL available formats. This results in a larger library and APK. FFmpeg can be recompiled with a subset of codecs enabled for those wanting a smaller size.

Installation
------------

FFmpegMediaMetadataRetriever relies on FFmpeg and native code. The build process
is complex and may be confusing for those unfamiliar the Android NDK. For this
reason I've precompiled AARs created by the build process and checked them
in [here](https://github.com/wseemann/FFmpegMediaMetadataRetriever/releases/download/v1.0.16/prebuilt-aars.zip).
The modules are also included with the library. If you don't want to build the modules
you can simple unzip the prebuilt ones and copy them to your projects "libs" folder. (Note:
copy them to YOUR projects "libs" folder, NOT the "libs" folder located in
FFmpegMediaMetadataRetriever/fmmr-library. Once this step is complete you can use the
library (See: Installation in Eclipse (Kepler)). If you want to compile the modules yourself
follow the Ant instructions listed below before attempting to use the library.

Download and install the [Android SDK](http://developer.android.com/sdk/index.html).
Download the [Android NDK](http://developer.android.com/tools/sdk/ndk/index.html).
Clone/Download/Fork the repo through GitHub or via (read-only)

    git clone https://github.com/wseemann/FFmpegMediaMetadataRetriever.git

### Android Studio (Gradle)

Note: The build instructions and scripts assume you are running Unix or Linux. Building
on other operating systems is currently not supported.

Execute the following in the root project directory (assuming /path/to/android_sdk/tools is in your PATH):

    android update project --path .

Open the newly created local.properties file and add the following lines:

    ndk.dir=<path to NDK>

where <path to NDK> is the path to your Android NDK, for example:

    ndk.dir=/home/wseemann/Android/android-ndk-r20

To compile the library in Android Studio, highlight `core` in the project explorer and run Build->Make Module 'core'. This will also build the native FFmpeg binaries.

Usage
------------

Sample code:

    FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
    mmr.setDataSource(mUri);
    mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM);
    mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST);
    Bitmap b = mmr.getFrameAtTime(2000000, FFmpegMediaMetadataRetriever.OPTION_CLOSEST); // frame at 2 seconds
    byte [] artwork = mmr.getEmbeddedPicture();
    
    mmr.release();

FFmpeg
-----------
This software uses code of <a href=http://ffmpeg.org>FFmpeg</a> licensed under the <a href=http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html>LGPLv2.1</a> and its source can be downloaded <a href=https://www.ffmpeg.org/developer.html>here</a>.

License
------------

```
FFmpegMediaMetadataRetriever: A unified interface for retrieving frame 
and meta data from an input media file.

Copyright 2022 William Seemann

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

# ffmpeg-android-maker

[![Build Status](https://travis-ci.org/Javernaut/ffmpeg-android-maker.svg?branch=master)](https://travis-ci.org/Javernaut/ffmpeg-android-maker)
[![Android Weekly #378](https://androidweekly.net/issues/issue-378/badge)](https://androidweekly.net/issues/issue-378)
[![Android Weekly #396](https://androidweekly.net/issues/issue-396/badge)](https://androidweekly.net/issues/issue-396)

Here is a script that downloads the source code of [FFmpeg](https://www.ffmpeg.org) library and assembles it for Android. The script produces shared libraries as well as header files. The output structure looks like this:

<img src="https://github.com/Javernaut/ffmpeg-android-maker/blob/master/images/output_structure.png" width="200">

The actual content of all this directories depends on how the FFmpeg was configured before assembling. For my purpose I enabled only *libavcodec*, *libavformat*, *libavutil* and *libswscale*, but you can set your own configuration to make the FFmpeg you need.

The version of FFmpeg here by default is **4.2.2** (but can be overridden). And the script expects to use **at least** Android NDK **r19** (both *r20* and *r21* also work ok).

The details of how this script is implemented are described in this series of posts:
* [Part 1](https://proandroiddev.com/a-story-about-ffmpeg-in-android-part-i-compilation-898e4a249422)
* [Part 2](https://proandroiddev.com/a-story-about-ffmpeg-in-android-part-ii-integration-55fb217251f0)
* [Part 3](https://proandroiddev.com/a-story-about-ffmpeg-on-android-part-iii-extension-71025444896e)

The [WIKI](https://github.com/Javernaut/ffmpeg-android-maker/wiki) contains a lot of useful information.

Actual Android app that uses the output of the script can be found [here](https://github.com/Javernaut/WhatTheCodec).

## Supported Android ABIs

* armeabi-v7a (with NEON)
* arm64-v8a
* x86
* x86_64

## Supported host OS

On **macOS** or **Linux** just execute the script in terminal.

~~It is also possible to execute this script on a **Windows** machine with [MSYS2](https://www.msys2.org). You also need to install specific packages to it: *make*, *git*, *diffutils* and *tar*. The script supports both 32-bit and 64-bit versions of Windows. Also see Prerequisites section for necessary software.~~

Since v2.0.0 the **Windows** support is temporary absent.

## Prerequisites

You have to define two environment variables:
* `ANDROID_SDK_HOME` - path to your Android SDK
* `ANDROID_NDK_HOME` - path to your Android NDK

Also, if you want to build **libaom**, then you have to install the cmake;3.10.2.4988404 package via Android SDK.

For **libdav1d** building you also need to install *ninja* and *meson 0.52.1* tools.

## Features

**Add an arbitrary external library that FFMpeg supports to the building process**. Just specify how the source code needs to be downloaded and how to perform the build operation. More about this is [here](https://github.com/Javernaut/ffmpeg-android-maker/wiki/External-libraries-integration).

**Setting your own FFmpeg version and origin**. You can actually override the version of FFmpeg used by the script. See details [here](https://github.com/Javernaut/ffmpeg-android-maker/wiki/Setting-the-FFmpeg-version).

**Test your script in a cloud**. This repository has CI integration and you can use it too for your own configurations. See details [here](https://github.com/Javernaut/ffmpeg-android-maker/wiki/Build-automation).

**Text relocations monitoring**. After an assembling is finished you can look into stats/text-relocations.txt file. That file lists all *.so files that were built and reports if any of them have text relocations. If you don't see any mentioning of 'TEXTREL' in the file, you are good. Otherwise, you will see exact binaries that have this problem.

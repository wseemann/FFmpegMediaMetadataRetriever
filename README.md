FFmpegMediaMetadataRetriever
============================

Overview
--------

FFmpegMediaMetadataRetriever is a reimplementation of Android's MediaMetadataRetriever class. The FFmpegMediaMetadataRetriever class provides a unified interface for retrieving frame and meta data from an input media file and uses FFmpeg as its backend.

Key Features:
* Support for API 7+ (Not just API level 10+ like MediaMetadataRetriever)
* URL support (Unlike MediaMetadataRetriever, see: http://code.google.com/p/android/issues/detail?id=35794)

Supported protocols:
* file, http, https and mmsh

Supported formats (audio and video):
* aac, flac, mp3, ogg, 3gp and more!

Usage
------------

To use FFmpegMediaMetadataRetriever in your project simply add the library to your project and copy the prebuilt libraries found at https://github.com/wseemann/FFmpegMediaMetadataRetriever/blob/master/library/prebuilt-libs.tar.gz into your project's "libs" folder.

Sample code:

    FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
    mmr.setDataSource(mUri);
    mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM);
    mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST);
    byte [] artwork = mmr.getEmbeddedPicture();
    mmr.release();

License
------------

    FFmpegMediaMetadataRetriever: A unified interface for retrieving frame 
    and meta data from an input media file.

    Copyright 2013 William Seemann

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.



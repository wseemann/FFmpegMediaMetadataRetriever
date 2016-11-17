/*
 * FFmpegMediaMetadataRetriever: A unified interface for retrieving frame 
 * and meta data from an input media file.
 *
 * Copyright 2016 William Seemann
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

package wseemann.media;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.Surface;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * FFmpegMediaMetadataRetriever class provides a unified interface for retrieving
 * frame and meta data from an input media file.
 */
public class FFmpegMediaMetadataRetriever
{
	private final static String TAG = "FFmpegMediaMetadataRetriever";
	
	/**
	 * User defined bitmap configuration. A bitmap configuration describes how pixels are
	 * stored. This affects the quality (color depth) as well as the ability to display
	 * transparent/translucent colors. 
	 */
	public static Bitmap.Config IN_PREFERRED_CONFIG;
	
	/*@SuppressLint("SdCardPath")
	private static final String LIBRARY_PATH = "/data/data/";
	
	private static final String [] JNI_LIBRARIES = {
		"libavutil.so",
		"libswscale.so",
		"libavcodec.so",
		"libavformat.so",
		"libffmpeg_mediametadataretriever_jni.so"		
	};
	
	static {
    	StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
    	
    	StringBuffer path = null;
    	File file = null;
    	boolean foundLibs = false;
    	
    	for (int j = 0; j < stackTraceElements.length; j++) {
    		String libraryPath = stackTraceElements[j].getClassName();
    	
    		String [] packageFragments = libraryPath.trim().split("\\.");
    	
    		path = new StringBuffer(LIBRARY_PATH);
    	
    		for (int i = 0; i < packageFragments.length; i++) {
    			if (i > 0) {
    				path.append(".");
    			}
    		
    			path.append(packageFragments[i]);
    			try {
    				//System.load(path.toString() + "/lib/" + JNI_LIBRARIES[0]);
    				file = new File(path.toString() + "/lib/" + JNI_LIBRARIES[0]);
    				if (file.exists()) {
    					path.append("/lib/");
    					foundLibs = true;
    					break;
    				}
    			} catch (UnsatisfiedLinkError ex) {
    			}
    		}
    		
    		if (foundLibs) {
    			break;
    		}
    	}
    	
    	if (!foundLibs) {
    		Log.e(TAG, TAG + " libraries not found. Did you forget to add them to your libs folder?");
    		throw new UnsatisfiedLinkError();
    	}
    	
    	for (int i = 0; i < JNI_LIBRARIES.length; i++) {
    		System.load(path.toString() + JNI_LIBRARIES[i]);
    	}
    	
        native_init();
    }*/
	
	private static final String [] JNI_LIBRARIES = {
		"avutil",
		"swscale",
		"avcodec",
		"avformat",
		"ffmpeg_mediametadataretriever_jni"		
	};
	
	static {
    	for (int i = 0; i < JNI_LIBRARIES.length; i++) {
    		System.loadLibrary(JNI_LIBRARIES[i]);
    	}
    	
        native_init();
    }

    // The field below is accessed by native methods
    private long mNativeContext;
    
    public FFmpegMediaMetadataRetriever() {
    	native_setup();
    }

    /**
     * Sets the data source (file pathname) to use. Call this
     * method before the rest of the methods in this class. This method may be
     * time-consuming.
     * 
     * @param path The path of the input media file.
     * @throws IllegalArgumentException If the path is invalid.
     */
    public native void setDataSource(String path) throws IllegalArgumentException;
    
    /**
     * Sets the data source (URI) to use. Call this
     * method before the rest of the methods in this class. This method may be
     * time-consuming.
     *
     * @param uri The URI of the input media.
     * @param headers the headers to be sent together with the request for the data
     * @throws IllegalArgumentException If the URI is invalid.
     */
    public void setDataSource(String uri,  Map<String, String> headers)
            throws IllegalArgumentException {
        int i = 0;
        String[] keys = new String[headers.size()];
        String[] values = new String[headers.size()];
        for (Map.Entry<String, String> entry: headers.entrySet()) {
            keys[i] = entry.getKey();
            values[i] = entry.getValue();
            ++i;
        }
        _setDataSource(uri, keys, values);
    }

    private native void _setDataSource(
        String uri, String[] keys, String[] values)
        throws IllegalArgumentException;

    /**
     * Sets the data source (FileDescriptor) to use.  It is the caller's
     * responsibility to close the file descriptor. It is safe to do so as soon
     * as this call returns. Call this method before the rest of the methods in
     * this class. This method may be time-consuming.
     * 
     * @param fd the FileDescriptor for the file you want to play
     * @param offset the offset into the file where the data to be played starts,
     * in bytes. It must be non-negative
     * @param length the length in bytes of the data to be played. It must be
     * non-negative.
     * @throws IllegalArgumentException if the arguments are invalid
     */
    public native void setDataSource(FileDescriptor fd, long offset, long length)
            throws IllegalArgumentException;
    
    /**
     * Sets the data source (FileDescriptor) to use. It is the caller's
     * responsibility to close the file descriptor. It is safe to do so as soon
     * as this call returns. Call this method before the rest of the methods in
     * this class. This method may be time-consuming.
     * 
     * @param fd the FileDescriptor for the file you want to play
     * @throws IllegalArgumentException if the FileDescriptor is invalid
     */
    public void setDataSource(FileDescriptor fd)
            throws IllegalArgumentException {
        // intentionally less than LONG_MAX
        setDataSource(fd, 0, 0x7ffffffffffffffL);
    }
    
    /**
     * Sets the data source as a content Uri. Call this method before 
     * the rest of the methods in this class. This method may be time-consuming.
     * 
     * @param context the Context to use when resolving the Uri
     * @param uri the Content URI of the data you want to play
     * @throws IllegalArgumentException if the Uri is invalid
     * @throws SecurityException if the Uri cannot be used due to lack of
     * permission.
     */
    public void setDataSource(Context context, Uri uri)
        throws IllegalArgumentException, SecurityException {
        if (uri == null) {
            throw new IllegalArgumentException();
        }
        
        String scheme = uri.getScheme();
        if(scheme == null || scheme.equals("file")) {
            setDataSource(uri.getPath());
            return;
        }

        AssetFileDescriptor fd = null;
        try {
            ContentResolver resolver = context.getContentResolver();
            try {
                fd = resolver.openAssetFileDescriptor(uri, "r");
            } catch(FileNotFoundException e) {
                throw new IllegalArgumentException();
            }
            if (fd == null) {
                throw new IllegalArgumentException();
            }
            FileDescriptor descriptor = fd.getFileDescriptor();
            if (!descriptor.valid()) {
                throw new IllegalArgumentException();
            }
            // Note: using getDeclaredLength so that our behavior is the same
            // as previous versions when the content provider is returning
            // a full file.
            if (fd.getDeclaredLength() < 0) {
                setDataSource(descriptor);
            } else {
                setDataSource(descriptor, fd.getStartOffset(), fd.getDeclaredLength());
            }
            return;
        } catch (SecurityException ex) {
        } finally {
            try {
                if (fd != null) {
                    fd.close();
                }
            } catch(IOException ioEx) {
            }
        }
        setDataSource(uri.toString());
    }
    
    /**
     * Call this method after setDataSource(). This method retrieves the 
     * meta data value associated with the keyCode.
     * 
     * The keyCode currently supported is listed below as METADATA_XXX
     * constants. With any other value, it returns a null pointer.
     * 
     * @param keyCode One of the constants listed below at the end of the class.
     * @return The meta data value associate with the given keyCode on success; 
     * null on failure.
     */
    public native String extractMetadata(String key);

    /**
     * Call this method after setDataSource(). This method retrieves the 
     * meta data value associated with the keyCode from a specific chapter,
     * if available.
     * 
     * The keyCode currently supported is listed below as METADATA_XXX
     * constants. With any other value, it returns a null pointer.
     * 
     * @param keyCode One of the constants listed below at the end of the class.
     * @param chapter The chapter from where the metadata will be retrieved.
     * @return The meta data value associate with the given keyCode on success; 
     * null on failure.
     */
    public native String extractMetadataFromChapter(String key, int chapter);
    
    /**
     * Gets the media metadata.
     *
     //* @param update_only controls whether the full set of available
     //* metadata is returned or just the set that changed since the
     //* last call. See {@see #METADATA_UPDATE_ONLY} and {@see
     //* #METADATA_ALL}.
     //*
     //* @param apply_filter if true only metadata that matches the
     //* filter is returned. See {@see #APPLY_METADATA_FILTER} and {@see
     //* #BYPASS_METADATA_FILTER}.
     //*
     * @return The metadata, possibly empty. null if an error occured.
     */    
    public Metadata getMetadata() {//final boolean update_only,
    	//	final boolean apply_filter) {
    	boolean update_only = false;
    	boolean apply_filter = false;
    	
    	Metadata data = new Metadata();
    	HashMap<String, String> metadata = null;
        if ((metadata = native_getMetadata(update_only, apply_filter, metadata)) == null) {
            return null;
        }

        // Metadata takes over the parcel, don't recycle it unless
        // there is an error.
    	if (!data.parse(metadata)) {
    		return null;
    	}
    	return data;
    }
    
    /*
     * @param update_only If true fetch only the set of metadata that have
     *                    changed since the last invocation of getMetadata.
     *                    The set is built using the unfiltered
     *                    notifications the native player sent to the
     *                    MediaPlayerService during that period of
     *                    time. If false, all the metadatas are considered.
     * @param apply_filter  If true, once the metadata set has been built based on
     *                     the value update_only, the current filter is applied.
     * @param reply[out] On return contains the serialized
     *                   metadata. Valid only if the call was successful.
     * @return The status code.
     */
    private native final HashMap<String, String> native_getMetadata(boolean update_only,
                                                    boolean apply_filter,
                                                    HashMap<String, String> reply);
    
    /**
     * Call this method after setDataSource(). This method finds a
     * representative frame close to the given time position by considering
     * the given option if possible, and returns it as a bitmap. This is
     * useful for generating a thumbnail for an input data source or just
     * obtain and display a frame at the given time position.
     *
     * @param timeUs The time position where the frame will be retrieved.
     * When retrieving the frame at the given time position, there is no
     * guarantee that the data source has a frame located at the position.
     * When this happens, a frame nearby will be returned. If timeUs is
     * negative, time position and option will ignored, and any frame
     * that the implementation considers as representative may be returned.
     *
     * @param option a hint on how the frame is found. Use
     * {@link #OPTION_PREVIOUS_SYNC} if one wants to retrieve a sync frame
     * that has a timestamp earlier than or the same as timeUs. Use
     * {@link #OPTION_NEXT_SYNC} if one wants to retrieve a sync frame
     * that has a timestamp later than or the same as timeUs. Use
     * {@link #OPTION_CLOSEST_SYNC} if one wants to retrieve a sync frame
     * that has a timestamp closest to or the same as timeUs. Use
     * {@link #OPTION_CLOSEST} if one wants to retrieve a frame that may
     * or may not be a sync frame but is closest to or the same as timeUs.
     * {@link #OPTION_CLOSEST} often has larger performance overhead compared
     * to the other options if there is no sync frame located at timeUs.
     *
     * @return A Bitmap containing a representative video frame, which 
     *         can be null, if such a frame cannot be retrieved.
     */
    public Bitmap getFrameAtTime(long timeUs, int option) {
        if (option < OPTION_PREVIOUS_SYNC ||
            option > OPTION_CLOSEST) {
            throw new IllegalArgumentException("Unsupported option: " + option);
        }

    	Bitmap b = null;
    	
        BitmapFactory.Options bitmapOptionsCache = new BitmapFactory.Options();
        //bitmapOptionsCache.inPreferredConfig = getInPreferredConfig();
        bitmapOptionsCache.inDither = false;
    	
        byte [] picture = _getFrameAtTime(timeUs, option);
        
        if (picture != null) {
        	b = BitmapFactory.decodeByteArray(picture, 0, picture.length, bitmapOptionsCache);
        }
        
        return b;
    }

    /**
     * Call this method after setDataSource(). This method finds a
     * representative frame close to the given time position if possible,
     * and returns it as a bitmap. This is useful for generating a thumbnail
     * for an input data source. Call this method if one does not care
     * how the frame is found as long as it is close to the given time;
     * otherwise, please call {@link #getFrameAtTime(long, int)}.
     *
     * @param timeUs The time position where the frame will be retrieved.
     * When retrieving the frame at the given time position, there is no
     * guarentee that the data source has a frame located at the position.
     * When this happens, a frame nearby will be returned. If timeUs is
     * negative, time position and option will ignored, and any frame
     * that the implementation considers as representative may be returned.
     *
     * @return A Bitmap containing a representative video frame, which
     *         can be null, if such a frame cannot be retrieved.
     *
     * @see #getFrameAtTime(long, int)
     */
    public Bitmap getFrameAtTime(long timeUs) {
    	Bitmap b = null;
    	
        BitmapFactory.Options bitmapOptionsCache = new BitmapFactory.Options();
        //bitmapOptionsCache.inPreferredConfig = getInPreferredConfig();
        bitmapOptionsCache.inDither = false;
    	
        byte [] picture = _getFrameAtTime(timeUs, OPTION_CLOSEST_SYNC);
        
        if (picture != null) {
        	b = BitmapFactory.decodeByteArray(picture, 0, picture.length, bitmapOptionsCache);
        }
        
        return b;
    }
    
    /**
     * Call this method after setDataSource(). This method finds a
     * representative frame at any time position if possible,
     * and returns it as a bitmap. This is useful for generating a thumbnail
     * for an input data source. Call this method if one does not
     * care about where the frame is located; otherwise, please call
     * {@link #getFrameAtTime(long)} or {@link #getFrameAtTime(long, int)}
     *
     * @return A Bitmap containing a representative video frame, which
     *         can be null, if such a frame cannot be retrieved.
     *
     * @see #getFrameAtTime(long)
     * @see #getFrameAtTime(long, int)
     */
    public Bitmap getFrameAtTime() {
        return getFrameAtTime(-1, OPTION_CLOSEST_SYNC);
    }
    
    private native byte [] _getFrameAtTime(long timeUs, int option);

    /**
     * Call this method after setDataSource(). This method finds a
     * representative frame close to the given time position by considering
     * the given option if possible, and returns it as a bitmap. This is
     * useful for generating a thumbnail for an input data source or just
     * obtain and display a frame at the given time position.
     *
     * @param timeUs The time position where the frame will be retrieved.
     * When retrieving the frame at the given time position, there is no
     * guarantee that the data source has a frame located at the position.
     * When this happens, a frame nearby will be returned. If timeUs is
     * negative, time position and option will ignored, and any frame
     * that the implementation considers as representative may be returned.
     *
     * @param option a hint on how the frame is found. Use
     * {@link #OPTION_PREVIOUS_SYNC} if one wants to retrieve a sync frame
     * that has a timestamp earlier than or the same as timeUs. Use
     * {@link #OPTION_NEXT_SYNC} if one wants to retrieve a sync frame
     * that has a timestamp later than or the same as timeUs. Use
     * {@link #OPTION_CLOSEST_SYNC} if one wants to retrieve a sync frame
     * that has a timestamp closest to or the same as timeUs. Use
     * {@link #OPTION_CLOSEST} if one wants to retrieve a frame that may
     * or may not be a sync frame but is closest to or the same as timeUs.
     * {@link #OPTION_CLOSEST} often has larger performance overhead compared
     * to the other options if there is no sync frame located at timeUs.
     *
     * @return A Bitmap containing a representative video frame, which
     *         can be null, if such a frame cannot be retrieved.
     */
    public Bitmap getScaledFrameAtTime(long timeUs, int option, int width, int height) {
        if (option < OPTION_PREVIOUS_SYNC ||
                option > OPTION_CLOSEST) {
            throw new IllegalArgumentException("Unsupported option: " + option);
        }

        Bitmap b = null;

        BitmapFactory.Options bitmapOptionsCache = new BitmapFactory.Options();
        //bitmapOptionsCache.inPreferredConfig = getInPreferredConfig();
        bitmapOptionsCache.inDither = false;

        byte [] picture = _getScaledFrameAtTime(timeUs, option, width, height);

        if (picture != null) {
            b = BitmapFactory.decodeByteArray(picture, 0, picture.length, bitmapOptionsCache);
        }

        return b;
    }

    /**
     * Call this method after setDataSource(). This method finds a
     * representative frame close to the given time position if possible,
     * and returns it as a bitmap. This is useful for generating a thumbnail
     * for an input data source. Call this method if one does not care
     * how the frame is found as long as it is close to the given time;
     * otherwise, please call {@link #getScaledFrameAtTime(long, int)}.
     *
     * @param timeUs The time position where the frame will be retrieved.
     * When retrieving the frame at the given time position, there is no
     * guarentee that the data source has a frame located at the position.
     * When this happens, a frame nearby will be returned. If timeUs is
     * negative, time position and option will ignored, and any frame
     * that the implementation considers as representative may be returned.
     *
     * @return A Bitmap containing a representative video frame, which
     *         can be null, if such a frame cannot be retrieved.
     *
     * @see #getScaledFrameAtTime(long, int)
     */
    public Bitmap getScaledFrameAtTime(long timeUs, int width, int height) {
        Bitmap b = null;

        BitmapFactory.Options bitmapOptionsCache = new BitmapFactory.Options();
        //bitmapOptionsCache.inPreferredConfig = getInPreferredConfig();
        bitmapOptionsCache.inDither = false;

        byte [] picture = _getScaledFrameAtTime(timeUs, OPTION_CLOSEST_SYNC, width, height);

        if (picture != null) {
            b = BitmapFactory.decodeByteArray(picture, 0, picture.length, bitmapOptionsCache);
        }

        return b;
    }

    private native byte [] _getScaledFrameAtTime(long timeUs, int option, int width, int height);

    /**
     * Call this method after setDataSource(). This method finds the optional
     * graphic or album/cover art associated associated with the data source. If
     * there are more than one pictures, (any) one of them is returned.
     * 
     * @return null if no such graphic is found.
     */
    public native byte[] getEmbeddedPicture();
    
    /**
     * Call it when one is done with the object. This method releases the memory
     * allocated internally.
     */
    public native void release();
    private native void native_setup();
    private static native void native_init();

    private native final void native_finalize();

    @Override
    protected void finalize() throws Throwable {
        try {
            native_finalize();
        } finally {
            super.finalize();
        }
    }

    /*private Bitmap.Config getInPreferredConfig() {
    	if (IN_PREFERRED_CONFIG != null) {
    		return IN_PREFERRED_CONFIG;
    	}
    	
    	return Bitmap.Config.RGB_565;
    }*/

    /**
     * Sets the {@link Surface} to be used as the sink for the video portion of
     * the media. This is similar to {@link #setDisplay(SurfaceHolder)}, but
     * does not support {@link #setScreenOnWhilePlaying(boolean)}.  Setting a
     * Surface will un-set any Surface or SurfaceHolder that was previously set.
     * A null surface will result in only the audio track being played.
     *
     * If the Surface sends frames to a {@link SurfaceTexture}, the timestamps
     * returned from {@link SurfaceTexture#getTimestamp()} will have an
     * unspecified zero point.  These timestamps cannot be directly compared
     * between different media sources, different instances of the same media
     * source, or multiple runs of the same program.  The timestamp is normally
     * monotonically increasing and is unaffected by time-of-day adjustments,
     * but it is reset when the position is set.
     *
     * @param surface The {@link Surface} to be used for the video portion of
     * the media.
     */
    public native void setSurface(Object surface);

    /**
     * Option used in method {@link #getFrameAtTime(long, int)} to get a
     * frame at a specified location.
     *
     * @see #getFrameAtTime(long, int)
     */
    /* Do not change these option values without updating their counterparts
     * in jni/metadata/ffmpeg_mediametadataretriever.h!
     */
    /**
     * This option is used with {@link #getFrameAtTime(long, int)} to retrieve
     * a sync (or key) frame associated with a data source that is located
     * right before or at the given time.
     *
     * @see #getFrameAtTime(long, int)
     */
    public static final int OPTION_PREVIOUS_SYNC    = 0x00;
    /**
     * This option is used with {@link #getFrameAtTime(long, int)} to retrieve
     * a sync (or key) frame associated with a data source that is located
     * right after or at the given time.
     *
     * @see #getFrameAtTime(long, int)
     */
    public static final int OPTION_NEXT_SYNC        = 0x01;
    /**
     * This option is used with {@link #getFrameAtTime(long, int)} to retrieve
     * a sync (or key) frame associated with a data source that is located
     * closest to (in time) or at the given time.
     *
     * @see #getFrameAtTime(long, int)
     */
    public static final int OPTION_CLOSEST_SYNC     = 0x02;
    /**
     * This option is used with {@link #getFrameAtTime(long, int)} to retrieve
     * a frame (not necessarily a key frame) associated with a data source that
     * is located closest to or at the given time.
     *
     * @see #getFrameAtTime(long, int)
     */
    public static final int OPTION_CLOSEST          = 0x03;

    /**
     * The metadata key to retrieve the name of the set this work belongs to.
     */
    public static final String METADATA_KEY_ALBUM = "album";
    /**
     * The metadata key to retrieve the main creator of the set/album, if different 
     * from artist. e.g. "Various Artists" for compilation albums.
     */
    public static final String METADATA_KEY_ALBUM_ARTIST = "album_artist";
    /**
     * The metadata key to retrieve the main creator of the work.
     */
    public static final String METADATA_KEY_ARTIST = "artist";
    /**
     * The metadata key to retrieve the any additional description of the file.
     */
    public static final String METADATA_KEY_COMMENT = "comment";
    /**
     * The metadata key to retrieve the who composed the work, if different from artist.
     */
    public static final String METADATA_KEY_COMPOSER = "composer";
    /**
     * The metadata key to retrieve the name of copyright holder.
     */
    public static final String METADATA_KEY_COPYRIGHT = "copyright";
    /**
     * The metadata key to retrieve the date when the file was created, preferably in ISO 8601.
     */
    public static final String METADATA_KEY_CREATION_TIME = "creation_time";
    /**
     * The metadata key to retrieve the date when the work was created, preferably in ISO 8601.
     */
    public static final String METADATA_KEY_DATE = "date";
    /**
     * The metadata key to retrieve the number of a subset, e.g. disc in a multi-disc collection.
     */
    public static final String METADATA_KEY_DISC = "disc";
    /**
     * The metadata key to retrieve the name/settings of the software/hardware that produced the file.
     */
    public static final String METADATA_KEY_ENCODER = "encoder";
    /**
     * The metadata key to retrieve the person/group who created the file.
     */
    public static final String METADATA_KEY_ENCODED_BY = "encoded_by";
    /**
     * The metadata key to retrieve the original name of the file.
     */
    public static final String METADATA_KEY_FILENAME = "filename";
    /**
     * The metadata key to retrieve the genre of the work.
     */
    public static final String METADATA_KEY_GENRE = "genre";
    /**
     * The metadata key to retrieve the main language in which the work is performed, preferably
     * in ISO 639-2 format. Multiple languages can be specified by separating them with commas.
     */
    public static final String METADATA_KEY_LANGUAGE = "language";
    /**
     * The metadata key to retrieve the artist who performed the work, if different from artist.
     * E.g for "Also sprach Zarathustra", artist would be "Richard Strauss" and performer "London 
     * Philharmonic Orchestra".
     */
    public static final String METADATA_KEY_PERFORMER = "performer";
    /**
     * The metadata key to retrieve the name of the label/publisher.
     */
    public static final String METADATA_KEY_PUBLISHER = "publisher";
    /**
     * The metadata key to retrieve the name of the service in broadcasting (channel name).
     */
    public static final String METADATA_KEY_SERVICE_NAME = "service_name";
    /**
     * The metadata key to retrieve the name of the service provider in broadcasting.
     */
    public static final String METADATA_KEY_SERVICE_PROVIDER = "service_provider";
    /**
     * The metadata key to retrieve the name of the work.
     */
    public static final String METADATA_KEY_TITLE = "title";
    /**
     * The metadata key to retrieve the number of this work in the set, can be in form current/total.
     */
    public static final String METADATA_KEY_TRACK = "track";
    /**
     * The metadata key to retrieve the total bitrate of the bitrate variant that the current stream 
     * is part of.
     */
    public static final String METADATA_KEY_VARIANT_BITRATE = "bitrate";
    /**
     * The metadata key to retrieve the duration of the work in milliseconds.
     */
    public static final String METADATA_KEY_DURATION = "duration";
    /**
     * The metadata key to retrieve the audio codec of the work.
     */
    public static final String METADATA_KEY_AUDIO_CODEC = "audio_codec";
    /**
     * The metadata key to retrieve the video codec of the work.
     */
    public static final String METADATA_KEY_VIDEO_CODEC = "video_codec";
    /**
     * This key retrieves the video rotation angle in degrees, if available.
     * The video rotation angle may be 0, 90, 180, or 270 degrees.
     */
    public static final String METADATA_KEY_VIDEO_ROTATION = "rotate";
    /**
     * The metadata key to retrieve the main creator of the work.
     */
    public static final String METADATA_KEY_ICY_METADATA = "icy_metadata";
    /**
     * The metadata key to retrieve the main creator of the work.
     */
    //private static final String METADATA_KEY_ICY_ARTIST = "icy_artist";
    /**
     * The metadata key to retrieve the name of the work.
     */
    //private static final String METADATA_KEY_ICY_TITLE = "icy_title";
    /**
     * This metadata key retrieves the average framerate (in frames/sec), if available.
     */
    public static final String METADATA_KEY_FRAMERATE = "framerate";
    /**
     * The metadata key to retrieve the chapter start time in milliseconds.
     */
    public static final String METADATA_KEY_CHAPTER_START_TIME = "chapter_start_time";
    /**
     * The metadata key to retrieve the chapter end time in milliseconds.
     */
    public static final String METADATA_KEY_CHAPTER_END_TIME = "chapter_end_time";
    /**
     * The metadata key to retrieve the chapter count.
     */
    public static final String METADATA_CHAPTER_COUNT = "chapter_count";
    /**
     * The metadata key to retrieve the file size in bytes.
     */
    public static final String METADATA_KEY_FILESIZE = "filesize";
    /**
     * The metadata key to retrieve the video width.
     */
    public static final String METADATA_KEY_VIDEO_WIDTH = "video_width";
    /**
     * The metadata key to retrieve the video height.
     */
    public static final String METADATA_KEY_VIDEO_HEIGHT = "video_height";

    /**
     Class to hold the media's metadata.  Metadata are used
     for human consumption and can be embedded in the media (e.g
     shoutcast) or available from an external source. The source can be
     local (e.g thumbnail stored in the DB) or remote.

     Metadata is like a Bundle. It is sparse and each key can occur at
     most once. The key is an integer and the value is the actual metadata.

     The caller is expected to know the type of the metadata and call
     the right get* method to fetch its value.

     //@hide
     //@deprecated Use {@link MediaMetadata}.
     */
    public class Metadata
    {
        // The metadata are keyed using integers rather than more heavy
        // weight strings. We considered using Bundle to ship the metadata
        // between the native layer and the java layer but dropped that
        // option since keeping in sync a native implementation of Bundle
        // and the java one would be too burdensome. Besides Bundle uses
        // String for its keys.
        // The key range [0 8192) is reserved for the system.
        //
        // We manually serialize the data in Parcels. For large memory
        // blob (bitmaps, raw pictures) we use MemoryFile which allow the
        // client to make the data purge-able once it is done with it.
        //

        /**
         * {@hide}
         */
        public static final int STRING_VAL     = 1;
        /**
         * {@hide}
         */
        public static final int INTEGER_VAL    = 2;
        /**
         * {@hide}
         */
        public static final int BOOLEAN_VAL    = 3;
        /**
         * {@hide}
         */
        public static final int LONG_VAL       = 4;
        /**
         * {@hide}
         */
        public static final int DOUBLE_VAL     = 5;
        /**
         * {@hide}
         */
        public static final int DATE_VAL       = 6;
        /**
         * {@hide}
         */
        public static final int BYTE_ARRAY_VAL = 7;
        // FIXME: misses a type for shared heap is missing (MemoryFile).
        // FIXME: misses a type for bitmaps.
        //private static final int LAST_TYPE = 7;

        //private static final String TAG = "media.Metadata";

        // After a successful parsing, set the parcel with the serialized metadata.
        //private Parcel mParcel;
        private HashMap<String, String> mParcel;

        /**
         * Check a parcel containing metadata is well formed. The header
         * is checked as well as the individual records format. However, the
         * data inside the record is not checked because we do lazy access
         * (we check/unmarshall only data the user asks for.)
         *
         * Format of a metadata parcel:
         <pre>
         1                   2                   3
         0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
         +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
         |                     metadata total size                       |
         +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
         |     'M'       |     'E'       |     'T'       |     'A'       |
         +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
         |                                                               |
         |                .... metadata records ....                     |
         |                                                               |
         +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
         </pre>
         *
         * @param parcel With the serialized data. Metadata keeps a
         *               reference on it to access it later on. The caller
         *               should not modify the parcel after this call (and
         *               not call recycle on it.)
         * @return false if an error occurred.
         * {@hide}
         */
        public boolean parse(HashMap<String, String> metadata) {
            if (metadata == null) {
                return false;
            } else {
                mParcel = metadata;
                return true;
            }
        }

        /**
         * @return true if a value is present for the given key.
         */
        public boolean has(final String metadataId) {
            if (!checkMetadataId(metadataId)) {
                throw new IllegalArgumentException("Invalid key: " + metadataId);
            }
            return mParcel.containsKey(metadataId);
        }

        // Accessors.
        // Caller must make sure the key is present using the {@code has}
        // method otherwise a RuntimeException will occur.

        /**
         * @return a map containing all of the available metadata.
         */
        public HashMap<String, String> getAll() {
            return mParcel;
        }

        /**
         * {@hide}
         */
        public String getString(final String key) {
            checkType(key, STRING_VAL);
            return String.valueOf(mParcel.get(key));
        }

        /**
         * {@hide}
         */
        public int getInt(final String key) {
            checkType(key, INTEGER_VAL);
            return Integer.valueOf(mParcel.get(key));
        }

        /**
         * Get the boolean value indicated by key
         */
        public boolean getBoolean(final String key) {
            checkType(key, BOOLEAN_VAL);
            return Integer.valueOf(mParcel.get(key)) == 1;
        }

        /**
         * {@hide}
         */
        public long getLong(final String key) {
            checkType(key, LONG_VAL);
            return Long.valueOf(mParcel.get(key));
        }

        /**
         * {@hide}
         */
        public double getDouble(final String key) {
            checkType(key, DOUBLE_VAL);
            return Double.valueOf(mParcel.get(key));
        }

        /**
         * {@hide}
         */
        public byte[] getByteArray(final String key) {
            checkType(key, BYTE_ARRAY_VAL);
            return mParcel.get(key).getBytes();
        }

        /**
         * {@hide}
         */
        public Date getDate(final String key) {
            checkType(key, DATE_VAL);
            final long timeSinceEpoch = Long.valueOf(mParcel.get(key));
            final String timeZone = mParcel.get(key);

            if (timeZone.length() == 0) {
                return new Date(timeSinceEpoch);
            } else {
                TimeZone tz = TimeZone.getTimeZone(timeZone);
                Calendar cal = Calendar.getInstance(tz);

                cal.setTimeInMillis(timeSinceEpoch);
                return cal.getTime();
            }
        }

        /**
         * Check val is either a system id or a custom one.
         * @param val Metadata key to test.
         * @return true if it is in a valid range.
         **/
        private boolean checkMetadataId(final String val) {
        /*if (val <= ANY || (LAST_SYSTEM < val && val < FIRST_CUSTOM)) {
            Log.e(TAG, "Invalid metadata ID " + val);
            return false;
        }*/
            return true;
        }

        /**
         * Check the type of the data match what is expected.
         */
        private void checkType(final String key, final int expectedType) {
            String type = mParcel.get(key);

            if (type == null) {
                //if (type != expectedType) {
                throw new IllegalStateException("Wrong type " + expectedType + " but got " + type);
            }
        }
    }
}

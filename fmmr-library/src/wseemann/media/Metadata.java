/*
 * FFmpegMediaMetadataRetriever: A unified interface for retrieving frame 
 * and meta data from an input media file.
 *
 * Copyright 2014 William Seemann
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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

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

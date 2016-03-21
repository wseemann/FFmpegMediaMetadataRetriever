/*
 * FFmpegMediaMetadataRetriever: A unified interface for retrieving frame 
 * and meta data from an input media file.
 *
 * Copyright 2015 William Seemann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wseemann.media.demo;

import java.util.ArrayList;
import java.util.List;

import wseemann.media.FFmpegMediaMetadataRetriever;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import android.widget.Toast;

/**
 * A custom Loader that loads all of the installed applications.
 */
public class MetadataLoader extends AsyncTaskLoader<List<Metadata>> {
    // TODO fix this
	//final InterestingConfigChanges mLastConfig = new InterestingConfigChanges();

	private String mUri;
    private List<Metadata> mMetadata;

    public MetadataLoader(Context context, Bundle args) {
        super(context);
        mUri = args.getString("uri");
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override public List<Metadata> loadInBackground() {
    	// Retrieve all metadata.
    	List<Metadata> metadata = new ArrayList<Metadata>();
    	
    	if (mUri == null) {
    		return metadata;
    	}
    	
    	FFmpegMediaMetadataRetriever fmmr = new FFmpegMediaMetadataRetriever();
    	try {
            if (FMMRFragment.mFinalSurface != null) {
                fmmr.setSurface(FMMRFragment.mFinalSurface);
            }

            fmmr.setDataSource(mUri);

    		for (int i = 0; i < Constants.METADATA_KEYS.length; i++) {
    			String key = Constants.METADATA_KEYS[i];
    			String value = fmmr.extractMetadata(key);
    		
    			if (value != null) {
    				metadata.add(new Metadata(key, value));
    				Log.i(MetadataLoader.class.getName(), "Key: " + key + " Value: " + value);
    			}
    		}

            // also extract metadata for chapters
            String count = fmmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_CHAPTER_COUNT);

            if (count != null) {
                int chapterCount = Integer.parseInt(count);

                for (int j = 0; j < chapterCount; j++) {
                    for (int i = 0; i < Constants.METADATA_KEYS.length; i++) {
                        String key = Constants.METADATA_KEYS[i];
                        String value = fmmr.extractMetadataFromChapter(key, j);

                        if (value != null) {
                            metadata.add(new Metadata(key, value));
                            Log.i(MetadataLoader.class.getName(), "Key: " + key + " Value: " + value);
                        }
                    }
                }
            }

    		Bitmap b = fmmr.getFrameAtTime();

    		if (b != null) {
    			Bitmap b2 = fmmr.getFrameAtTime(4000000, FFmpegMediaMetadataRetriever.OPTION_CLOSEST_SYNC);
    			if (b2 != null) {
    				b = b2;
    			}
    		}
    		
    		if (b != null) {
    			metadata.add(new Metadata("image", b));
    			Log.i(MetadataLoader.class.getName(), "Extracted frame");
    		} else {
    			Log.e(MetadataLoader.class.getName(), "Failed to extract frame");
    		}
    	} catch (IllegalArgumentException ex) {
    		ex.printStackTrace();
    	} finally {
    		fmmr.release();
    	}
    	
        // Sort the list.
        //Collections.sort(entries, ALPHA_COMPARATOR);

        // Done!
        return metadata;
    }

    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override public void deliverResult(List<Metadata> metadata) {
    	if (metadata.size() == 0) {
    		Toast.makeText(getContext(),
    				getContext().getString(R.string.error_message),
    				Toast.LENGTH_SHORT).show();
    	}
    	
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (metadata != null) {
                onReleaseResources(metadata);
            }
        }
        List<Metadata> oldMetadata = metadata;
        mMetadata = metadata;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(metadata);
        }

        // At this point we can release the resources associated with
        // 'oldMetadata' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldMetadata != null) {
            onReleaseResources(oldMetadata);
        }
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override protected void onStartLoading() {
        if (mMetadata != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mMetadata);
        }

        // TODO fix this
        if (takeContentChanged() || mMetadata == null) { //|| configChange) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    /**
     * Handles a request to cancel a load.
     */
    @Override public void onCanceled(List<Metadata> metadata) {
        super.onCanceled(metadata);

        // At this point we can release the resources associated with 'metadata'
        // if needed.
        onReleaseResources(metadata);
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'metadata'
        // if needed.
        if (mMetadata != null) {
            onReleaseResources(mMetadata);
            mMetadata = null;
        }
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(List<Metadata> metadata) {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    }
}
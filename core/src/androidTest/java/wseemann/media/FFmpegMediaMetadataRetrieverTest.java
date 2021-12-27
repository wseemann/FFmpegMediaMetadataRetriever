package wseemann.media;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class FFmpegMediaMetadataRetrieverTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context context = InstrumentationRegistry.getTargetContext();
        Assert.assertEquals("wseemann.media.test", context.getPackageName());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetDataSourceWithInvalidUri() throws Exception {
        String uri = null;

        FFmpegMediaMetadataRetriever retriever = new FFmpegMediaMetadataRetriever();
        retriever.setDataSource(uri);
    }

    @Test(expected=IllegalStateException.class)
    public void testReleasedRetriever() throws Exception {
        String uri = null;

        FFmpegMediaMetadataRetriever retriever = new FFmpegMediaMetadataRetriever();
        retriever.release();

        retriever.setDataSource(uri);
    }

    @Test
    public void testExtractMetadataFromUrl() throws Exception {
        FFmpegMediaMetadataRetriever retriever = new FFmpegMediaMetadataRetriever();

        retriever.setDataSource("http://download.blender.org/peach/bigbuckbunny_movies/big_buck_bunny_480p_stereo.ogg");

        Assert.assertEquals("vorbis", retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_AUDIO_CODEC));
        Assert.assertEquals("theora", retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_CODEC));

        retriever.release();
    }

    @Test
    public void testExtractMetadataFromFileDescriptor() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();

        /*AssetManager assetManager = context.getAssets();
        AssetFileDescriptor fd = assetManager.openFd("test_audio_file_one");*/

        AssetFileDescriptor fd = context.getResources().openRawResourceFd(R.raw.song);

        Log.d("------>", "Start offset: " + fd.getStartOffset() + " Length: " + fd.getLength());

        FFmpegMediaMetadataRetriever retriever = new FFmpegMediaMetadataRetriever();

        retriever.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());

        Assert.assertEquals("Stuck On You", retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_TITLE));
        Assert.assertEquals("Meiko", retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST));

        retriever.release();
    }
}
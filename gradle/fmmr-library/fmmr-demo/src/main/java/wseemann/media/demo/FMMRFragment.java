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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wseemann.media.demo;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import android.net.Uri;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

public class FMMRFragment extends ListFragment
		implements LoaderManager.LoaderCallbacks<List<Metadata>> {

	private int mId = 0;
	private ImageView mImage;

	private SurfaceView mmSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	public static Surface mFinalSurface;
	
	// This is the Adapter being used to display the list's data.
    private MetadataListAdapter mAdapter;
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setRetainInstance(true);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View layout = super.onCreateView(inflater, container, savedInstanceState);
    	ListView lv = (ListView) layout.findViewById(android.R.id.list);
    	ViewGroup parent = (ViewGroup) lv.getParent();
    	
    	View v = inflater.inflate(R.layout.fragment, container, false);
    	
    	// Remove ListView and add my view in its place
        int lvIndex = parent.indexOfChild(lv);
        parent.removeViewAt(lvIndex);
        parent.addView(v, lvIndex, lv.getLayoutParams());
    	
    	final EditText uriText = (EditText) v.findViewById(R.id.uri);
    	// Uncomment for debugging
		//uriText.setText("http://...");
    	//uriText.setText("http://distribution.bbb3d.renderfarming.net/video/mp4/bbb_sunflower_1080p_60fps_stereo_abl.mp4");
		//https://ia700401.us.archive.org/19/items/ksnn_compilation_master_the_internet/ksnn_compilation_master_the_internet_512kb.mp4

    	Intent intent = getActivity().getIntent();
    	
    	// Populate the edit text field with the intent uri, if available
    	Uri uri = intent.getData();
    	
    	if (intent.getExtras() != null &&
    			intent.getExtras().getCharSequence(Intent.EXTRA_TEXT) != null) {
			uri = Uri.parse(intent.getExtras().getCharSequence(Intent.EXTRA_TEXT).toString());
		}
    	
    	if (uri != null) {
    		try {
    			uriText.setText(URLDecoder.decode(uri.toString(), "UTF-8"));
    		} catch (UnsupportedEncodingException e1) {
    		}
    	}
    	
		getActivity().setIntent(null);
		
    	Button goButton = (Button) v.findViewById(R.id.go_button);
    	goButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Clear the error message
				uriText.setError(null);

				// Hide the keyboard
				InputMethodManager imm = (InputMethodManager)
						FMMRFragment.this.getActivity().getSystemService(
								Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(uriText.getWindowToken(), 0);

				String uri = uriText.getText().toString();

				if (uri.equals("")) {
					uriText.setError(getString(R.string.uri_error));
					return;
				}

				// Start out with a progress indicator.
				setListShown(false);

				String uriString = uriText.getText().toString();

				Bundle bundle = new Bundle();
				try {
					bundle.putString("uri", URLDecoder.decode(uriString, "UTF-8"));
					mId++;
					FMMRFragment.this.getLoaderManager().initLoader(mId, bundle, FMMRFragment.this);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				//	mAdapter.add(new Metadata("test", "test"));
				//	mAdapter.notifyDataSetChanged();
			}
		});
    	
    	return layout;
    }
    
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

		// Give some text to display if there is no data.  In a real
        // application this would come from a resource.
        setEmptyText(getString(R.string.no_metadata));

    	View header = getLayoutInflater(savedInstanceState).inflate(R.layout.header, null);
    	mImage = (ImageView) header.findViewById(R.id.image);

		// set up the Surface video sink
		mmSurfaceView = (SurfaceView) header.findViewById(R.id.surfaceview);
		mSurfaceHolder = mmSurfaceView.getHolder();

		mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {

			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
				Log.v("TAG", "surfaceChanged format=" + format + ", width=" + width + ", height="
						+ height);
			}

			public void surfaceCreated(SurfaceHolder holder) {
				mFinalSurface = holder.getSurface();

				final EditText uriText = (EditText) getView().findViewById(R.id.uri);

				// Clear the error message
				uriText.setError(null);

				// Hide the keyboard
				InputMethodManager imm = (InputMethodManager)
						FMMRFragment.this.getActivity().getSystemService(
								Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(uriText.getWindowToken(), 0);

				String uri = uriText.getText().toString();

				if (uri.equals("")) {
					uriText.setError(getString(R.string.uri_error));
					return;
				}

				String view = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("pref_displaytype", "ImageView");

				// Start out with a progress indicator.
				setListShown(false);

				String uriString = uriText.getText().toString();

				Bundle bundle = new Bundle();
				try {
					bundle.putString("uri", URLDecoder.decode(uriString, "UTF-8"));
					mId++;
					//FMMRFragment.this.getLoaderManager().initLoader(mId, bundle, FMMRFragment.this);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}

			public void surfaceDestroyed(SurfaceHolder holder) {
				Log.v("TAG", "surfaceDestroyed");
			}

		});

		getListView().addHeaderView(header);

        if (mAdapter == null) {
        	// Create an empty adapter we will use to display the loaded data.
        	mAdapter = new MetadataListAdapter(getActivity());
        	setListAdapter(mAdapter);
        } else {
        	setListAdapter(mAdapter);

        	// Start out with a progress indicator.
			setListShown(false);

            // Prepare the loader.  Either re-connect with an existing one,
            // or start a new one.
            getLoaderManager().initLoader(mId, new Bundle(), this);
        }
    }
	
	@Override
	public void onDestroyView() {
	    super.onDestroyView();
	    setListAdapter(null);
	}

	@Override
	public Loader<List<Metadata>> onCreateLoader(int arg0, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader with one argument, so it is simple.
		return new MetadataLoader(getActivity(), args);
	}

	@Override
	public void onLoadFinished(Loader<List<Metadata>> loader, List<Metadata> metadata) {
		if (metadata.size() == 0) {
	        // Set the new metadata in the adapter.
	        mAdapter.setMetadata(metadata);
			setListShown(true);
			return;
		}
		
		Bitmap b = null;
		int imageIndex = -1;
		
		mImage.setImageResource(0);
		
		for (int i = 0; i < metadata.size(); i++) {
			if (metadata.get(i).getKey().equals("image")) {
				imageIndex = i;
				
				b = (Bitmap) metadata.get(i).getValue();
				if (b != null) {
					float density = getResources().getDisplayMetrics().density;
					int scale = (int) (200 * density);
					Bitmap bm = Bitmap.createScaledBitmap(b, scale, scale, true);
					mImage.setImageBitmap(bm);
				}
			}
		}
		
		if (imageIndex != -1) {
			metadata.remove(imageIndex);
		}
		
        // Set the new metadata in the adapter.
        mAdapter.setMetadata(metadata);

        if (b != null) {
        	metadata.add(new Metadata("image", b));
        }
        
        // The list should now be shown.
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
	}

	@Override
	public void onLoaderReset(Loader<List<Metadata>> metadata) {
        // Clear the metadata in the adapter.
        mAdapter.setMetadata(null);
	}
	
	private static class MetadataListAdapter extends ArrayAdapter<Metadata> {
	    private final LayoutInflater mInflater;

	    public MetadataListAdapter(Context context) {
	        super(context, android.R.layout.simple_list_item_2);
	        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    }

	    public void setMetadata(List<Metadata> metadata) {
	        clear();
	        if (metadata != null) {
	        	for (int i = 0; i < metadata.size(); i++) {
	        		add(metadata.get(i));
	        	}
	        }
	    }

	    /**
	     * Populate new items in the list.
	     */
	    @Override public View getView(int position, View convertView, ViewGroup parent) {
	        View view;

	        if (convertView == null) {
	            view = mInflater.inflate(android.R.layout.simple_list_item_2, parent, false);
	        } else {
	            view = convertView;
	        }

	        Metadata item = getItem(position);
	        ((TextView)view.findViewById(android.R.id.text2)).setText(item.getKey());
	        ((TextView)view.findViewById(android.R.id.text1)).setText((String) item.getValue());

	        return view;
	    }
	}
}

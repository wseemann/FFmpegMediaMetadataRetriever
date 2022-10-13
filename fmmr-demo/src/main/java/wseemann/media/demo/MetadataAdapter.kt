/*
 * FFmpegMediaMetadataRetriever: A unified interface for retrieving frame
 * and meta data from an input media file.
 *
 * Copyright 2022 William Seemann
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

package wseemann.media.demo

import android.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class MetadataListAdapter(
    context: Context
) : ArrayAdapter<Metadata?>(context, R.layout.simple_list_item_2) {

    fun setMetadata(metadata: List<Metadata?>?) {
        clear()
        if (metadata != null) {
            for (i in metadata.indices) {
                add(metadata[i])
            }
        }
    }

    /**
     * Populate new items in the list.
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.simple_list_item_2, parent, false)
        val item = getItem(position)
        (view.findViewById<View>(R.id.text2) as TextView).text = item!!.key
        (view.findViewById<View>(R.id.text1) as TextView).text = item.value as String
        return view
    }
}
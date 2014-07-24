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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>
#include <libavutil/opt.h>
#include <ffmpeg_mediametadataretriever.h>

#include <stdio.h>

const int TARGET_IMAGE_FORMAT = PIX_FMT_RGB24;
const int TARGET_IMAGE_CODEC = CODEC_ID_PNG;

const char *DURATION = "duration";
const char *AUDIO_CODEC = "audio_codec";
const char *VIDEO_CODEC = "video_codec";
const char *ICY_METADATA = "icy_metadata";
//const char *ICY_ARTIST = "icy_artist";
//const char *ICY_TITLE = "icy_title";
const char *ROTATE = "rotate";
const char *FRAMERATE = "framerate";

const int SUCCESS = 0;
const int FAILURE = -1;

void convert_image(AVCodecContext *pCodecCtx, AVFrame *pFrame, AVPacket *avpkt, int *got_packet_ptr);

int is_supported_format(int codec_id) {
	if (codec_id == CODEC_ID_PNG ||
			codec_id == CODEC_ID_MJPEG ||
	        codec_id == CODEC_ID_BMP) {
		return 1;
	}
	
	return 0;
}

size_t first_char_pos(const char *value, const char ch) {
    char *chptr = strchr(value, ch);
    return chptr - value;
}

size_t last_char_pos(const char *value, const char ch) {
    char *chptr = strrchr(value, ch);
    return chptr - value;
}

void get_shoutcast_metadata(AVFormatContext *ic) {
    char *value = NULL;
    
    if (av_opt_get(ic, "icy_metadata_packet", 1, (uint8_t **) &value) < 0) {
        value = NULL;
    }
	
    if (value && value[0]) {
    	av_dict_set(&ic->metadata, ICY_METADATA, value, 0);
    	
    	/*int first_pos = first_char_pos(value, '\'');
        int last_pos = first_char_pos(value, ';') - 2;
        int pos = last_pos - first_pos;
        
        if (pos == 0) {
        	return;
        }
        
        char temp[pos];
        memcpy(temp, value + first_pos + 1 , pos);
        temp[pos] = '\0';
        value = temp;

    	first_pos = first_char_pos(value, '-') - 1;
        
        char artist[first_pos];
        memcpy(artist, value, first_pos);
        artist[first_pos] = '\0';
        
		av_dict_set(&ic->metadata, ICY_ARTIST, artist, 0);
        
        pos = strlen(value) - first_char_pos(value, '-') + 2;
         
        char album[pos];
        memcpy(album, value + first_char_pos(value, '-') + 2, pos);
        album[pos] = '\0';
        
		av_dict_set(&ic->metadata, ICY_TITLE, album, 0);*/
    }
}

void get_duration(AVFormatContext *ic, char * value) {
	int duration = 0;

	if (ic) {
		if (ic->duration != AV_NOPTS_VALUE) {
			duration = ((ic->duration / AV_TIME_BASE) * 1000);
		}
	}

	sprintf(value, "%d", duration); // %i
}

void set_codec(AVFormatContext *ic, int i) {
    const char *codec_type = av_get_media_type_string(ic->streams[i]->codec->codec_type);

	if (!codec_type) {
		return;
	}

    const char *codec_name = avcodec_get_name(ic->streams[i]->codec->codec_id);

	if (strcmp(codec_type, "audio") == 0) {
		av_dict_set(&ic->metadata, AUDIO_CODEC, codec_name, 0);
    } else if (codec_type && strcmp(codec_type, "video") == 0) {
	   	av_dict_set(&ic->metadata, VIDEO_CODEC, codec_name, 0);
	}
}

void set_rotation(State *s) {
    
	if (!extract_metadata(&s, ROTATE) && s->video_st && s->video_st->metadata) {
		AVDictionaryEntry *entry = av_dict_get(s->video_st->metadata, ROTATE, NULL, AV_DICT_IGNORE_SUFFIX);
        
        if (entry && entry->value) {
            av_dict_set(&s->pFormatCtx->metadata, ROTATE, entry->value, 0);
        }
	}
}

void set_framerate(State *s) {
	char value[30] = "0";
	
	if (s->video_st && s->video_st->avg_frame_rate.den && s->video_st->avg_frame_rate.num) {
		double d = av_q2d(s->video_st->avg_frame_rate);
		uint64_t v = lrintf(d * 100);
		if (v % 100) {
			sprintf(value, "%3.2f", d);
		} else if (v % (100 * 1000)) {
			sprintf(value,  "%1.0f", d);
		} else {
			sprintf(value, "%1.0fk", d / 1000);
		}
		
	    av_dict_set(&s->pFormatCtx->metadata, FRAMERATE, value, 0);
	}
}

int stream_component_open(State *s, int stream_index) {
	AVFormatContext *pFormatCtx = s->pFormatCtx;
	AVCodecContext *codecCtx;
	AVCodec *codec;

	if (stream_index < 0 || stream_index >= pFormatCtx->nb_streams) {
		return FAILURE;
	}

	// Get a pointer to the codec context for the stream
	codecCtx = pFormatCtx->streams[stream_index]->codec;

	printf("avcodec_find_decoder %s\n", codecCtx->codec_name);

	// Find the decoder for the audio stream
	codec = avcodec_find_decoder(codecCtx->codec_id);

	if(codec == NULL) {
	    printf("avcodec_find_decoder() failed to find audio decoder\n");
	    return FAILURE;
	}

	// Open the codec
    if (!codec || (avcodec_open2(codecCtx, codec, NULL) < 0)) {
	  	printf("avcodec_open2() failed\n");
		return FAILURE;
	}

	switch(codecCtx->codec_type) {
		case AVMEDIA_TYPE_AUDIO:
			s->audio_stream = stream_index;
		    s->audio_st = pFormatCtx->streams[stream_index];
			break;
		case AVMEDIA_TYPE_VIDEO:
			s->video_stream = stream_index;
		    s->video_st = pFormatCtx->streams[stream_index];
			break;
		default:
			break;
	}

	return SUCCESS;
}

int set_data_source_l(State **ps, const char* path) {
	printf("set_data_source\n");
	int audio_index = -1;
	int video_index = -1;
	int i;

	State *state = *ps;
	
	char duration[30] = "0";

    printf("Path: %s\n", path);

    AVDictionary *options = NULL;
    av_dict_set(&options, "icy", "1", 0);
    av_dict_set(&options, "user-agent", "FFmpegMediaMetadataRetriever", 0);
    
    if (state->headers) {
        av_dict_set(&options, "headers", state->headers, 0);
    }
    
    if (state->offset > 0) {
        state->pFormatCtx = avformat_alloc_context();
        state->pFormatCtx->skip_initial_bytes = state->offset;
    }
    
    if (avformat_open_input(&state->pFormatCtx, path, NULL, &options) != 0) {
	    printf("Metadata could not be retrieved\n");
		*ps = NULL;
    	return FAILURE;
    }

	if (avformat_find_stream_info(state->pFormatCtx, NULL) < 0) {
	    printf("Metadata could not be retrieved\n");
	    avformat_close_input(&state->pFormatCtx);
		*ps = NULL;
    	return FAILURE;
	}

	get_duration(state->pFormatCtx, duration);
	av_dict_set(&state->pFormatCtx->metadata, DURATION, duration, 0);
	
	get_shoutcast_metadata(state->pFormatCtx);

	//av_dump_format(state->pFormatCtx, 0, path, 0);
	
    // Find the first audio and video stream
	for (i = 0; i < state->pFormatCtx->nb_streams; i++) {
		if (state->pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO && video_index < 0) {
			video_index = i;
		}

		if (state->pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO && audio_index < 0) {
			audio_index = i;
		}

		set_codec(state->pFormatCtx, i);
	}

	if (audio_index >= 0) {
		stream_component_open(state, audio_index);
	}

	if (video_index >= 0) {
		stream_component_open(state, video_index);
	}

	/*if(state->video_stream < 0 || state->audio_stream < 0) {
	    avformat_close_input(&state->pFormatCtx);
		*ps = NULL;
		return FAILURE;
	}*/

    set_rotation(state);
    set_framerate(state);
    
	/*printf("Found metadata\n");
	AVDictionaryEntry *tag = NULL;
	while ((tag = av_dict_get(state->pFormatCtx->metadata, "", tag, AV_DICT_IGNORE_SUFFIX))) {
    	printf("Key %s: \n", tag->key);
    	printf("Value %s: \n", tag->value);
    }*/
	
	*ps = state;
	return SUCCESS;
}

void init(State **ps) {
	State *state = *ps;

	if (state && state->pFormatCtx) {
		avformat_close_input(&state->pFormatCtx);
	}

	if (state && state->fd != -1) {
		close(state->fd);
	}
	
	if (!state) {
		state = av_mallocz(sizeof(State));
	}

	state->pFormatCtx = NULL;
	state->audio_stream = -1;
	state->video_stream = -1;
	state->audio_st = NULL;
	state->video_st = NULL;
	state->fd = -1;
	state->offset = 0;
	state->headers = NULL;

	*ps = state;
}

int set_data_source_uri(State **ps, const char* path, const char* headers) {
	State *state = *ps;
	
	init(&state);
	
	state->headers = headers;
	
	*ps = state;
	
	return set_data_source_l(ps, path);
}

int set_data_source_fd(State **ps, int fd, int64_t offset, int64_t length) {
    char path[256] = "";

	State *state = *ps;
	
	init(&state);
    	
    int myfd = dup(fd);

    char str[20];
    sprintf(str, "pipe:%d", myfd);
    strcat(path, str);
    
    state->fd = myfd;
    state->offset = offset;
    
	*ps = state;
    
    return set_data_source_l(ps, path);
}

const char* extract_metadata(State **ps, const char* key) {
	printf("extract_metadata\n");
    char* value = NULL;
	
	State *state = *ps;
    
	if (!state || !state->pFormatCtx) {
		return value;
	}

	if (key) {
		if (av_dict_get(state->pFormatCtx->metadata, key, NULL, AV_DICT_IGNORE_SUFFIX)) {
			value = av_dict_get(state->pFormatCtx->metadata, key, NULL, AV_DICT_IGNORE_SUFFIX)->value;
		} else if (state->audio_st && av_dict_get(state->audio_st->metadata, key, NULL, AV_DICT_IGNORE_SUFFIX)) {
			value = av_dict_get(state->audio_st->metadata, key, NULL, AV_DICT_IGNORE_SUFFIX)->value;
		} else if (state->video_st && av_dict_get(state->video_st->metadata, key, NULL, AV_DICT_IGNORE_SUFFIX)) {
			value = av_dict_get(state->video_st->metadata, key, NULL, AV_DICT_IGNORE_SUFFIX)->value;
		}
	}

	return value;
}

int get_embedded_picture(State **ps, AVPacket *pkt) {
	printf("get_embedded_picture\n");
	int i = 0;
	int got_packet = 0;
	AVFrame *frame = NULL;
	
	State *state = *ps;

	if (!state || !state->pFormatCtx) {
		return FAILURE;
	}

    // read the format headers
    if (state->pFormatCtx->iformat->read_header(state->pFormatCtx) < 0) {
    	printf("Could not read the format header\n");
    	return FAILURE;
    }

    // find the first attached picture, if available
    for (i = 0; i < state->pFormatCtx->nb_streams; i++) {
        if (state->pFormatCtx->streams[i]->disposition & AV_DISPOSITION_ATTACHED_PIC) {
        	printf("Found album art\n");
        	*pkt = state->pFormatCtx->streams[i]->attached_pic;
        	
        	// Is this a packet from the video stream?
        	if (pkt->stream_index == state->video_stream) {
        		int codec_id = state->video_st->codec->codec_id;
        		
        		// If the image isn't already in a supported format convert it to one
        		if (!is_supported_format(codec_id)) {
        			int got_frame = 0;
        			
                    av_init_packet(pkt);
        			
   			        frame = avcodec_alloc_frame();
        			    	
   			        if (!frame) {
   			        	break;
        			}
   			        
        			if (avcodec_decode_video2(state->video_st->codec, frame, &got_frame, pkt) <= 0) {
        				break;
        			}

        			// Did we get a video frame?
        			if (got_frame) {
        				AVPacket packet;
        	            av_init_packet(&packet);
        	            packet.data = NULL;
        	            packet.size = 0;
        				convert_image(state->video_st->codec, frame, &packet, &got_packet);
        				*pkt = packet;
        				break;
        			}
        		} else {
                	av_init_packet(pkt);
                	pkt->data = state->pFormatCtx->streams[i]->attached_pic.data;
                	pkt->size = state->pFormatCtx->streams[i]->attached_pic.size;
        			
        			got_packet = 1;
        			break;
        		}
        	}
        }
    }

	av_free(frame);

	if (got_packet) {
		return SUCCESS;
	} else {
		return FAILURE;
	}
}

void convert_image(AVCodecContext *pCodecCtx, AVFrame *pFrame, AVPacket *avpkt, int *got_packet_ptr) {
	AVCodecContext *codecCtx;
	AVCodec *codec;
	AVPicture dst_picture;
	AVFrame *frame;
	
	*got_packet_ptr = 0;

	codec = avcodec_find_encoder(TARGET_IMAGE_CODEC);
	if (!codec) {
	    printf("avcodec_find_decoder() failed to find decoder\n");
		goto fail;
	}

    codecCtx = avcodec_alloc_context3(codec);
	if (!codecCtx) {
		printf("avcodec_alloc_context3 failed\n");
		goto fail;
	}

	codecCtx->bit_rate = pCodecCtx->bit_rate;
	codecCtx->width = pCodecCtx->width;
	codecCtx->height = pCodecCtx->height;
	codecCtx->pix_fmt = TARGET_IMAGE_FORMAT;
	codecCtx->codec_type = AVMEDIA_TYPE_VIDEO;
	codecCtx->time_base.num = pCodecCtx->time_base.num;
	codecCtx->time_base.den = pCodecCtx->time_base.den;

	if (!codec || avcodec_open2(codecCtx, codec, NULL) < 0) {
	  	printf("avcodec_open2() failed\n");
		goto fail;
	}

	frame = avcodec_alloc_frame();
	
	if (!frame) {
		goto fail;
	}
	
    avpicture_alloc(&dst_picture,
    		TARGET_IMAGE_FORMAT,
    		codecCtx->width,
    		codecCtx->height);
    
    /* copy data and linesize picture pointers to frame */
    *((AVPicture *)frame) = dst_picture;
    
	struct SwsContext *scalerCtx = sws_getContext(pCodecCtx->width, 
			pCodecCtx->height, 
			pCodecCtx->pix_fmt, 
			pCodecCtx->width, 
			pCodecCtx->height, 
			TARGET_IMAGE_FORMAT, 
	        SWS_FAST_BILINEAR, 0, 0, 0);
	
	if (!scalerCtx) {
		printf("sws_getContext() failed\n");
		goto fail;
	}
    
    sws_scale(scalerCtx,
    		(const uint8_t * const *) pFrame->data,
    		pFrame->linesize,
    		0,
    		pFrame->height,
    		frame->data,
    		frame->linesize);
	
	int ret = avcodec_encode_video2(codecCtx, avpkt, frame, got_packet_ptr);
	
	if (ret < 0) {
		*got_packet_ptr = 0;
	}
	
    av_free(dst_picture.data[0]);
	
	// TODO is this right?
	fail:
    av_free(frame);
	
	if (codecCtx) {
		avcodec_close(codecCtx);
	    av_free(codecCtx);
	}
	
	if (scalerCtx) {
		sws_freeContext(scalerCtx);
	}
	
	if (ret < 0 || !*got_packet_ptr) {
		av_free_packet(avpkt);
	}
}

void decode_frame(State *state, AVPacket *pkt, int *got_frame, int64_t desired_frame_number) {
	// Allocate video frame
	AVFrame *frame = avcodec_alloc_frame();

	*got_frame = 0;
	
	if (!frame) {
	    return;
	}
	
	// Read frames and return the first one found
	while (av_read_frame(state->pFormatCtx, pkt) >= 0) {

		// Is this a packet from the video stream?
		if (pkt->stream_index == state->video_stream) {
			int codec_id = state->video_st->codec->codec_id;
			        		
			// If the image isn't already in a supported format convert it to one
			if (!is_supported_format(codec_id)) {
	            *got_frame = 0;
	            
				// Decode video frame
				if (avcodec_decode_video2(state->video_st->codec, frame, got_frame, pkt) <= 0) {
					*got_frame = 0;
					break;
				}

				// Did we get a video frame?
				if (*got_frame) {
					if (desired_frame_number == -1 ||
							(desired_frame_number != -1 && frame->pkt_pts >= desired_frame_number)) {
					    av_init_packet(pkt);
						pkt->data = NULL;
      	            	pkt->size = 0;
						convert_image(state->video_st->codec, frame, pkt, got_frame);
						break;
					}
				}
			} else {
				*got_frame = 1;
	        	break;
			}
		}
	}
	
	// Free the frame
	avcodec_free_frame(&frame);
}

int get_frame_at_time(State **ps, int64_t timeUs, int option, AVPacket *pkt) {
	printf("get_frame_at_time\n");
	int got_packet = 0;
    int64_t desired_frame_number = -1;
	
    State *state = *ps;

    Options opt = option;
    
	if (!state || !state->pFormatCtx || state->video_stream < 0) {
		return FAILURE;
	}
	
    if (timeUs != -1) {
        int stream_index = state->video_stream;
        int64_t seek_time = av_rescale_q(timeUs, AV_TIME_BASE_Q, state->pFormatCtx->streams[stream_index]->time_base);
        int64_t seek_stream_duration = state->pFormatCtx->streams[stream_index]->duration;

        int flags = 0;
        int ret = -1;
        
        // For some reason the seek_stream_duration is sometimes a negative value,
        // make sure to check that it is greater than 0 before adjusting the
        // seek_time
        if (seek_stream_duration > 0 && seek_time > seek_stream_duration) {
        	seek_time = seek_stream_duration;
        }
        
        if (seek_time < 0) {
        	return FAILURE;
       	}
        
        if (opt == OPTION_CLOSEST) {
        	desired_frame_number = seek_time;
        	flags = AVSEEK_FLAG_BACKWARD; 
        } else if (opt == OPTION_CLOSEST_SYNC) {
        	flags = 0;
        } else if (opt == OPTION_NEXT_SYNC) {
        	flags = 0;
        } else if (opt == OPTION_PREVIOUS_SYNC) {
        	flags = AVSEEK_FLAG_BACKWARD;
        }
        
        ret = av_seek_frame(state->pFormatCtx, stream_index, seek_time, flags);
        
    	if (ret < 0) {
    		return FAILURE;
    	} else {
            if (state->audio_stream >= 0) {
            	avcodec_flush_buffers(state->audio_st->codec);
            }
    		
            if (state->video_stream >= 0) {
            	avcodec_flush_buffers(state->video_st->codec);
            }
    	}
    }
    
    decode_frame(state, pkt, &got_packet, desired_frame_number);
    
    if (got_packet) {
    	//const char *filename = "/Users/wseemann/Desktop/one.png";
    	//FILE *picture = fopen(filename, "wb");
    	//fwrite(pkt->data, pkt->size, 1, picture);
    	//fclose(picture);
    }
    
	if (got_packet) {
		return SUCCESS;
	} else {
		return FAILURE;
	}
}

void release(State **ps) {
	printf("release\n");

	State *state = *ps;
	
    if (state) {
    	if (state->pFormatCtx) {
    		avformat_close_input(&state->pFormatCtx);
    	}
    	
    	if (state->fd != -1) {
    		close(state->fd);
    	}
    	
    	av_freep(&state);
        ps = NULL;
    }
}

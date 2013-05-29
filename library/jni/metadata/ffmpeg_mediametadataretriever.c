/*
 * FFmpegMediaMetadataRetriever: A unified interface for retrieving frame 
 * and meta data from an input media file.
 *
 * Copyright 2013 William Seemann
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
#include <ffmpeg_mediametadataretriever.h>

const char *DURATION = "duration";
const char *AUDIO_CODEC = "audio_codec";
const char *VIDEO_CODEC = "video_codec";

const int SUCCESS = 0;
const int FAILURE = -1;

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

int set_data_source(State **ps, const char* path) {
	printf("set_data_source\n");
	int audio_index = -1;
	int video_index = -1;
	int i;

	State *state = *ps;
	
	if (state && state->pFormatCtx) {
		avformat_close_input(&state->pFormatCtx);
	}

	if (!state) {
		state = av_mallocz(sizeof(State));
	}

	state->pFormatCtx = NULL;
	state->audio_stream = -1;
	state->video_stream = -1;
	state->audio_st = NULL;
	state->video_st = NULL;
	
	char duration[30] = "0";

    printf("Path: %s\n", path);

    if (avformat_open_input(&state->pFormatCtx, path, NULL, NULL) != 0) {
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

	/*if (audio_index >= 0) {
		stream_component_open(state, audio_index);
	}*/

	if (video_index >= 0) {
		stream_component_open(state, video_index);
	}

	/*if(state->video_stream < 0 || state->audio_stream < 0) {
	    avformat_close_input(&state->pFormatCtx);
		*ps = NULL;
		return FAILURE;
	}*/

	/*printf("Found metadata\n");
	AVDictionaryEntry *tag = NULL;
	while ((tag = av_dict_get(state->pFormatCtx->metadata, "", tag, AV_DICT_IGNORE_SUFFIX))) {
    	printf("Key %s: \n", tag->key);
    	printf("Value %s: \n", tag->value);
    }*/
	
	*ps = state;
	return SUCCESS;
}


const char* extract_metadata(State **ps, const char* key) {
	printf("extract_metadata\n");
    char* value = NULL;
	
	State *state = *ps;
    
	if (!state || !state->pFormatCtx) {
		goto fail;
	}

	if (key) {
		if (av_dict_get(state->pFormatCtx->metadata, key, NULL, AV_DICT_IGNORE_SUFFIX)) {
			value = av_dict_get(state->pFormatCtx->metadata, key, NULL, AV_DICT_IGNORE_SUFFIX)->value;
		}
	}

	fail:

	return value;
}

AVPacket* get_embedded_picture(State **ps) {
	printf("get_embedded_picture\n");
	int i = 0;
	AVPacket packet;
	AVPacket *pkt = NULL;
	
	State *state = *ps;
	
	if (!state || !state->pFormatCtx) {
		return pkt;
	}

    // read the format headers
    if (state->pFormatCtx->iformat->read_header(state->pFormatCtx) < 0) {
    	printf("Could not read the format header\n");
    	return pkt;
    }

    // find the first attached picture, if available
    for (i = 0; i < state->pFormatCtx->nb_streams; i++) {
        if (state->pFormatCtx->streams[i]->disposition & AV_DISPOSITION_ATTACHED_PIC) {
        	printf("Found album art\n");
        	packet = state->pFormatCtx->streams[i]->attached_pic;
        	pkt = (AVPacket *) malloc(sizeof(packet));
        	av_init_packet(pkt);
        	pkt->data = packet.data;
        	pkt->size = packet.size;
        }
    }

	return pkt;
}

void convert_to_jpeg(AVCodecContext *pCodecCtx, AVFrame *pFrame, AVPacket *avpkt, int *got_packet_ptr) {
	AVCodecContext *codecCtx;
	AVCodec *codec;
	
	*got_packet_ptr = 0;

	int pix_fmt = PIX_FMT_YUVJ420P;
	
	//int pix_fmt = PIX_FMT_BGR24;
	//codec = avcodec_find_encoder(CODEC_ID_BMP);
	
	codec = avcodec_find_encoder(CODEC_ID_MJPEG);
	if (!codec) {
	    printf("avcodec_find_decoder() failed to find video decoder\n");
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
	codecCtx->pix_fmt = pix_fmt;
	codecCtx->codec_type = AVMEDIA_TYPE_VIDEO;
	codecCtx->time_base.num = pCodecCtx->time_base.num;
	codecCtx->time_base.den = pCodecCtx->time_base.den;

	if (!codec || avcodec_open2(codecCtx, codec, NULL) < 0) {
	  	printf("avcodec_open2() failed\n");
		goto fail;
	}

	int ret = avcodec_encode_video2(codecCtx, avpkt, pFrame, got_packet_ptr);
	
	if (ret < 0) {
		*got_packet_ptr = 0;
	}
	
	avcodec_close(codecCtx);
	
	// TODO is this right?
	fail:
	if (ret < 0 || !*got_packet_ptr) {
		av_free_packet(avpkt);
	}
}

void decode_frame(State *state, AVPacket *avpkt, int *got_frame) {
	AVFrame *frame;
	AVPacket packet;

	*got_frame = 0;
	
	// Allocate video frame
	frame = avcodec_alloc_frame();

	if (!frame) {
		return;
	}
	
	// Read frames and return the first one found
	while (av_read_frame(state->pFormatCtx, &packet) >= 0) {

		// Is this a packet from the video stream?
		if (packet.stream_index == state->video_stream) {
			// Decode video frame
			if (avcodec_decode_video2(state->video_st->codec, frame, got_frame, &packet) < 0) {
				*got_frame = 0;
				break;
			}

			// Did we get a video frame?
			if (got_frame) {
				convert_to_jpeg(state->video_st->codec, frame, avpkt, got_frame);
				break;
			}
		}

		// Free the packet that was allocated by av_read_frame
		av_free_packet(&packet);
	}

	// Free the frame
	av_freep(&frame);
 }

AVPacket* get_frame_at_time(State **ps, long timeUs) {
	printf("get_frame_at_time\n");
	int got_packet;
	AVPacket packet;
	AVPacket *pkt = NULL;
	
    State *state = *ps;

	if (!state || !state->pFormatCtx || state->video_stream < 0) {
		return pkt;
	}

    if (timeUs != -1) {
    	int64_t seek_target = timeUs * 1000;

    	if (avformat_seek_file(state->pFormatCtx, -1, INT64_MIN, seek_target, INT64_MAX, 0) < 0) {
    		return pkt;
    	} else {
            if (state->audio_stream >= 0) {
            	avcodec_flush_buffers(state->audio_st->codec);
            }
    		
            if (state->video_stream >= 0) {
            	avcodec_flush_buffers(state->video_st->codec);
            }
    	}
    }
    
    av_init_packet(&packet);
    packet.data = NULL;
    packet.size = 0;
    
    decode_frame(state, &packet, &got_packet);
    
    if (got_packet) {
    	//const char *JPEGFName = "/home/seemann/Desktop/one.jpg";
    	//FILE *picture = fopen(JPEGFName, "wb");
    	//fwrite(packet.data, packet.size, 1, picture);
    	//fclose(picture);
    	
    	pkt = (AVPacket *) malloc(sizeof(packet));
    	av_init_packet(pkt);
    	pkt->data = packet.data;
    	pkt->size = packet.size;
    	
    	//av_free_packet(&packet);
    }
    
    return pkt;
}

void release(State **ps) {
	printf("release\n");

	State *state = *ps;
	
    if (state) {
    	if (state->pFormatCtx) {
    		avformat_close_input(&state->pFormatCtx);
    	}
    	
    	av_freep(&state);
        ps = NULL;
    }
}

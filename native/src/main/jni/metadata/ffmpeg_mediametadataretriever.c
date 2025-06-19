/*
 * FFmpegMediaMetadataRetriever: A unified interface for retrieving frame 
 * and meta data from an input media file.
 *
 * Copyright 2025 William Seemann
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
#include <libavutil/imgutils.h>
#include <libavutil/opt.h>
#include "ffmpeg_mediametadataretriever.h"
#include "ffmpeg_utils.h"

#include <stdio.h>
#include <unistd.h>

#include <android/log.h>

const int TARGET_IMAGE_FORMAT = AV_PIX_FMT_RGBA;
const int TARGET_IMAGE_CODEC = AV_CODEC_ID_PNG;

void convert_image(State *state, AVCodecContext *pCodecCtx, AVFrame *pFrame, AVPacket *avpkt, int *got_packet_ptr, int width, int height);

int is_supported_format(int codec_id, int pix_fmt) {
	if ((codec_id == AV_CODEC_ID_PNG ||
		 codec_id == AV_CODEC_ID_MJPEG ||
		 codec_id == AV_CODEC_ID_BMP) &&
		pix_fmt == AV_PIX_FMT_RGBA) {
		return 1;
	}

	return 0;
}

int get_scaled_context(State *s, AVCodecContext *pCodecCtx, int width, int height) {
	const AVCodec *targetCodec = avcodec_find_encoder(TARGET_IMAGE_CODEC);
	if (!targetCodec) {
		printf("avcodec_find_decoder() failed to find encoder\n");
		return FAILURE;
	}

	s->scaled_codecCtx = avcodec_alloc_context3(targetCodec);
	if (!s->scaled_codecCtx) {
		printf("avcodec_alloc_context3 failed\n");
		return FAILURE;
	}

	AVCodecParameters *codecpar = s->video_st->codecpar;
	s->scaled_codecCtx->bit_rate = codecpar->bit_rate;
	s->scaled_codecCtx->width = width;
	s->scaled_codecCtx->height = height;
	s->scaled_codecCtx->pix_fmt = TARGET_IMAGE_FORMAT;
	s->scaled_codecCtx->codec_type = AVMEDIA_TYPE_VIDEO;
	s->scaled_codecCtx->time_base = s->video_st->time_base;

	if (!targetCodec || avcodec_open2(s->scaled_codecCtx, targetCodec, NULL) < 0) {
		printf("avcodec_open2() failed\n");
		avcodec_free_context(&s->scaled_codecCtx);
		return FAILURE;
	}

	s->scaled_sws_ctx = sws_getContext(codecpar->width,
			codecpar->height,
			codecpar->format,
			width,
			height,
			TARGET_IMAGE_FORMAT,
			SWS_BILINEAR,
			NULL,
			NULL,
			NULL);

	return SUCCESS;
}

int stream_component_open(State *s, int stream_index) {
	AVFormatContext *pFormatCtx = s->pFormatCtx;
	AVCodecContext *codecCtx = NULL;

	if (stream_index < 0 || stream_index >= pFormatCtx->nb_streams) {
		return FAILURE;
	}

	// Get a pointer to the codec context for the stream
	AVCodecParameters *codecpar = pFormatCtx->streams[stream_index]->codecpar;

    // Find the decoder for the audio stream
    const AVCodec *codec = avcodec_find_decoder(codecpar->codec_id);
    if (!codec) {
        printf("avcodec_find_decoder() failed to find decoder\n");
        return FAILURE;
    }

    // Allocate the codec context
    codecCtx = avcodec_alloc_context3(codec);
    if (!codecCtx) {
        printf("avcodec_alloc_context3 failed\n");
        return FAILURE;
    }

    // Copies codec settings
    if (avcodec_parameters_to_context(codecCtx, codecpar) < 0) {
        printf("avcodec_parameters_to_context failed\n");
        avcodec_free_context(&codecCtx);
        return FAILURE;
    }

	// Open the codec
    if (!codec || (avcodec_open2(codecCtx, codec, NULL) < 0)) {
    	printf("avcodec_open2() failed\n");
    	avcodec_free_context(&codecCtx);
        return FAILURE;
	}

	switch(codecCtx->codec_type) {
		case AVMEDIA_TYPE_AUDIO:
			s->audio_stream = stream_index;
		    s->audio_st = pFormatCtx->streams[stream_index];
		    s->audioDecoderCodecCtx = codecCtx;
			break;
		case AVMEDIA_TYPE_VIDEO:
			s->video_stream = stream_index;
		    s->video_st = pFormatCtx->streams[stream_index];
		    s->videoDecoderCodecCtx = codecCtx;

			const AVCodec *targetCodec = avcodec_find_encoder(TARGET_IMAGE_CODEC);
			if (!targetCodec) {
			    printf("avcodec_find_encoder() failed to find encoder\n");
				return FAILURE;
			}

		    s->videoEncoderCodecCtx = avcodec_alloc_context3(targetCodec);
			if (!s->videoEncoderCodecCtx) {
				printf("avcodec_alloc_context3 failed\n");
				return FAILURE;
			}

			s->videoEncoderCodecCtx->bit_rate = s->video_st->codecpar->bit_rate;
			s->videoEncoderCodecCtx->width = s->video_st->codecpar->width;
			s->videoEncoderCodecCtx->height = s->video_st->codecpar->height;
			s->videoEncoderCodecCtx->pix_fmt = TARGET_IMAGE_FORMAT;
			s->videoEncoderCodecCtx->codec_type = AVMEDIA_TYPE_VIDEO;
			s->videoEncoderCodecCtx->time_base = s->video_st->time_base;

			if (!targetCodec || avcodec_open2(s->videoEncoderCodecCtx, targetCodec, NULL) < 0) {
			  	printf("avcodec_open2() failed\n");
				avcodec_free_context(&s->videoEncoderCodecCtx);
				return FAILURE;
			}

		    s->sws_ctx = sws_getContext(s->video_st->codecpar->width,
		    		s->video_st->codecpar->height,
		    		s->video_st->codecpar->format,
		    		s->video_st->codecpar->width,
		    		s->video_st->codecpar->height,
		    		TARGET_IMAGE_FORMAT,
		    		SWS_BILINEAR,
		    		NULL,
		    		NULL,
		    		NULL);
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
    
    if (state->media_data_source_callback) {
    	int size = 64 * 1024;
    	uint8_t *buffer = malloc(size);

    	AVIOContext* avioCtx = avio_alloc_context(buffer,
    			size,
				0,
				state,
				state->media_data_source_callback,
				NULL,
				state->media_data_source_seek_callback);

        state->pFormatCtx = avformat_alloc_context();
    	state->pFormatCtx->pb = avioCtx;
    	state->pFormatCtx->flags = AVFMT_FLAG_CUSTOM_IO;
    	state->position = 0;

    	if (avformat_open_input(&state->pFormatCtx, NULL, NULL, &options) != 0) {
    		printf("Metadata could not be retrieved\n");
    		*ps = NULL;
    		return FAILURE;
    	}
    } else {
    	if (avformat_open_input(&state->pFormatCtx, path, NULL, &options) != 0) {
    		printf("Metadata could not be retrieved\n");
    		*ps = NULL;
    		return FAILURE;
    	}
    }

	if (avformat_find_stream_info(state->pFormatCtx, NULL) < 0) {
	    printf("Metadata could not be retrieved\n");
	    avformat_close_input(&state->pFormatCtx);
		*ps = NULL;
    	return FAILURE;
	}

	set_duration(state->pFormatCtx);
	
	set_shoutcast_metadata(state->pFormatCtx);

	//av_dump_format(state->pFormatCtx, 0, path, 0);
	
    // Find the first audio and video stream
	for (i = 0; i < state->pFormatCtx->nb_streams; i++) {
		if (state->pFormatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO && video_index < 0) {
			video_index = i;
		}

		if (state->pFormatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO && audio_index < 0) {
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

    set_rotation(state->pFormatCtx, state->audio_st, state->video_st);
    set_framerate(state->pFormatCtx, state->audio_st, state->video_st);
    set_filesize(state->pFormatCtx);
    set_chapter_count(state->pFormatCtx);
    set_video_dimensions(state->pFormatCtx, state->video_st);
    
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
    state->audioDecoderCodecCtx = NULL;
    state->videoDecoderCodecCtx = NULL;
    state->videoEncoderCodecCtx = NULL;
	state->fd = -1;
	state->offset = 0;
	state->headers = NULL;
	state->callback_data_source = NULL;
    state->media_data_source_callback = NULL;
    state->media_data_source_seek_callback = NULL;
    state->position = 0;

	*ps = state;
}

int set_data_source_uri(State **ps, const char* path, const char* headers) {
	State *state = *ps;
	
	ANativeWindow *native_window = NULL;

	if (state && state->native_window) {
		native_window = state->native_window;
	}

	init(&state);
	
	state->native_window = native_window;

	state->headers = headers;
	
	*ps = state;
	
	return set_data_source_l(ps, path);
}

void advance_file_descriptor(int fd, int64_t offset) {
	unsigned char buffer[1];
	int count;

	int i;

	for (i = 0; i < offset; i = i + 1) {
		count = read(fd, buffer, 1);

		if (!count) {
			break;
		}
	}
}

int set_data_source_fd(State **ps, int fd, int64_t offset, int64_t length) {
    char path[256] = "";

	State *state = *ps;
	
	ANativeWindow *native_window = NULL;

	if (state && state->native_window) {
		native_window = state->native_window;
	}

	init(&state);

	state->native_window = native_window;
    	
    int myfd = dup(fd);

    char str[20];
    sprintf(str, "pipe:%d", myfd);
    strcat(path, str);
    
    state->fd = myfd;
    //state->offset = offset;
    
    if (offset > 0) {
    	advance_file_descriptor(myfd, offset);
    }

	*ps = state;

    return set_data_source_l(ps, path);
}

int set_data_source_callback(State **ps, void* callback_data_source, int (*read_packet) (void *, uint8_t *, int), int64_t (*seek)(void *, int64_t, int)) {
    char path[256] = "";

	State *state = *ps;

	init(&state);

	char str[20];
    sprintf(str, "pipe:0");
    strcat(path, str);

    state->callback_data_source = callback_data_source;
    state->media_data_source_callback = read_packet;
    state->media_data_source_seek_callback = seek;

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

	return extract_metadata_internal(state->pFormatCtx, state->audio_st, state->video_st, key);
}

const char* extract_metadata_from_chapter(State **ps, const char* key, int chapter) {
	printf("extract_metadata_from_chapter\n");
    char* value = NULL;
	
	State *state = *ps;
	
	if (!state || !state->pFormatCtx || state->pFormatCtx->nb_chapters <= 0) {
		return value;
	}

	if (chapter < 0 || chapter >= state->pFormatCtx->nb_chapters) {
		return value;
	}

	return extract_metadata_from_chapter_internal(state->pFormatCtx, state->audio_st, state->video_st, key, chapter);
}

int get_metadata(State **ps, AVDictionary **metadata) {
    printf("get_metadata\n");
    
    State *state = *ps;
    
    if (!state || !state->pFormatCtx) {
        return FAILURE;
    }
    
    get_metadata_internal(state->pFormatCtx, state->audio_st, state->video_st, metadata);
    
    return SUCCESS;
}

int get_embedded_picture(State **ps, AVPacket *pkt) {
    printf("get_embedded_picture\n");
    int i = 0;
    int got_packet = 0;
    AVFrame *frame = NULL;

    State *state = *ps;

    // Clear the packet which was passed in
    if (pkt) {
        av_packet_unref(pkt);
    }

    if (!state || !state->pFormatCtx) {
        return FAILURE;
    }

    // find the first attached picture, if available
    for (i = 0; i < state->pFormatCtx->nb_streams; i++) {
        if (state->pFormatCtx->streams[i]->disposition & AV_DISPOSITION_ATTACHED_PIC) {
            printf("Found album art\n");

            av_packet_ref(pkt, &state->pFormatCtx->streams[i]->attached_pic);
            got_packet = 1;

            // Is this a packet from the video stream?
            if (pkt->stream_index == state->video_stream) {
            	int codec_id = state->videoDecoderCodecCtx->codec_id;
            	int pix_fmt = state->videoDecoderCodecCtx->pix_fmt;

            	// If the image isn't already in a supported format convert it to one
                if (!is_supported_format(codec_id, pix_fmt)) {

                    frame = av_frame_alloc();
                    if (!frame) {
                        break;
                    }

                    if (avcodec_send_packet(state->videoDecoderCodecCtx, pkt) < 0) {
                        av_frame_free(&frame);
                        break;
                    }

                    if (avcodec_receive_frame(state->videoDecoderCodecCtx, frame) < 0) {
                        av_frame_free(&frame);
                        break;
                    }

                    AVPacket *convertedPkt = av_packet_alloc();
                    if (!convertedPkt) {
                        av_frame_free(&frame);
                        break;
                    }

                    convert_image(state, state->videoDecoderCodecCtx, frame, convertedPkt, &got_packet, -1, -1);

                    av_packet_unref(pkt);
                    av_packet_ref(pkt, convertedPkt);
                    av_packet_free(&convertedPkt);
                } else {
                	av_packet_unref(pkt);
                	av_packet_ref(pkt, &state->pFormatCtx->streams[i]->attached_pic);
                	got_packet = 1;
                }

                break;
            }
        }
    }

    av_frame_free(&frame);

    return got_packet ? SUCCESS : FAILURE;
}

void convert_image(State *state, AVCodecContext *pCodecCtx, AVFrame *pFrame, AVPacket *avpkt, int *got_packet_ptr, int width, int height) {
    AVCodecContext *codecCtx;
    struct SwsContext *scalerCtx;
    AVFrame *frame = NULL;

    *got_packet_ptr = 0;

    // Clear the packet which was passed in
    if (avpkt) {
    	av_packet_unref(avpkt);
    }

    if (width != -1 && height != -1) {
        if (state->scaled_codecCtx == NULL || state->scaled_sws_ctx == NULL) {
            get_scaled_context(state, pCodecCtx, width, height);
        }

        codecCtx = state->scaled_codecCtx;
        scalerCtx = state->scaled_sws_ctx;
    } else {
        codecCtx = state->videoEncoderCodecCtx;
        scalerCtx = state->sws_ctx;
    }

    if (width == -1) {
    	width = pCodecCtx->width;
    }

    if (height == -1) {
    	height = pCodecCtx->height;
    }

    frame = av_frame_alloc();
    if (!frame) {
        return;
    }

    // Determine required buffer size and allocate buffer
    int size = av_image_get_buffer_size(TARGET_IMAGE_FORMAT, width, height, 1);
    void * buffer = (uint8_t *) av_malloc(size * sizeof(uint8_t));

    // Set the frame parameters
    frame->format = TARGET_IMAGE_FORMAT;
    frame->width = codecCtx->width;
    frame->height = codecCtx->height;

    int ret = av_image_alloc(frame->data, frame->linesize, codecCtx->width, codecCtx->height, TARGET_IMAGE_FORMAT, 32);
    if (ret < 0) {
        av_frame_free(&frame);
        av_free(buffer);
        return;
    }

    sws_scale(scalerCtx,
              (const uint8_t * const *)pFrame->data,
              pFrame->linesize,
              0,
              pFrame->height,
              frame->data,
              frame->linesize);

    // Send frame to encoder
    ret = avcodec_send_frame(codecCtx, frame);
    if (ret < 0) {
        av_freep(&frame->data[0]);
        av_frame_free(&frame);
        av_free(buffer);
        return;
    }

    // Receive encoded packet
    ret = avcodec_receive_packet(codecCtx, avpkt);
    if (ret == 0) {
    	// Send the frame to the native surface
    	if (state->native_window) {
    		ANativeWindow_setBuffersGeometry(state->native_window, width, height, WINDOW_FORMAT_RGBA_8888);

    		ANativeWindow_Buffer windowBuffer;

    		if (ANativeWindow_lock(state->native_window, &windowBuffer, NULL) == 0) {
    			//__android_log_print(ANDROID_LOG_VERBOSE, "LOG_TAG", "width %d", windowBuffer.width);
    			//__android_log_print(ANDROID_LOG_VERBOSE, "LOG_TAG", "height %d", windowBuffer.height);

    			int h = 0;

    			for (h = 0; h < height; h++)  {
    				memcpy(windowBuffer.bits + h * windowBuffer.stride * 4,
    						buffer + h * frame->linesize[0],
							width*4);
    			}

    			ANativeWindow_unlockAndPost(state->native_window);
    		}
    	}

        *got_packet_ptr = 1;
    } else {
        *got_packet_ptr = 0;
        av_packet_unref(avpkt);
    }

    av_freep(&frame->data[0]);
    av_frame_free(&frame);
    av_free(buffer);
}

void decode_frame(State *state, AVPacket *pkt, int *got_frame, int64_t desired_frame_number, int width, int height) {
    *got_frame = 0;

    // Clear the packet which was passed in
    if (pkt) {
    	av_packet_unref(pkt);
    }

	// Allocate video frame
    AVFrame *frame = av_frame_alloc();
    if (!frame || !state->videoDecoderCodecCtx) {
        av_frame_free(&frame);
        return;
    }

    // Read frames and return the first one found
    while (av_read_frame(state->pFormatCtx, pkt) >= 0) {

    	// Is this a packet from the video stream?
        if (pkt->stream_index == state->video_stream) {
        	int codec_id = state->videoDecoderCodecCtx->codec_id;
        	int pix_fmt = state->videoDecoderCodecCtx->pix_fmt;

        	// If the image isn't already in a supported format convert it to one
            if (!is_supported_format(codec_id, pix_fmt)) {
            	 *got_frame = 0;

                if (avcodec_send_packet(state->videoDecoderCodecCtx, pkt) < 0) {
                    break;
                }

                int ret = avcodec_receive_frame(state->videoDecoderCodecCtx, frame);
                if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) {
                    continue;
                } else if (ret < 0) {
                    break;
                }

                if (desired_frame_number == -1 || (frame->pts >= desired_frame_number)) {
                    AVPacket *convertedPkt = av_packet_alloc();
                    if (!convertedPkt) {
                    	break;
                    }

                    convert_image(state, state->videoDecoderCodecCtx, frame, convertedPkt, got_frame, width, height);

                    av_packet_unref(pkt);
                    av_packet_move_ref(pkt, convertedPkt);
                    av_packet_free(&convertedPkt);

                    break;
                }
            } else {
                *got_frame = 1;
                break;
            }
        }

        av_packet_unref(pkt);
    }

	// Free the frame
    av_frame_free(&frame);
}

int get_frame_at_time(State **ps, int64_t timeUs, int option, AVPacket *pkt) {
	return get_scaled_frame_at_time(ps, timeUs, option, pkt, -1, -1);
}

int get_scaled_frame_at_time(State **ps, int64_t timeUs, int option, AVPacket *pkt, int width, int height) {
    printf("get_frame_at_time\n");
    int got_packet = 0;
    int64_t desired_frame_number = -1;

    State *state = *ps;

    Options opt = option;

    // Clear the packet which was passed in
    if (pkt) {
    	av_packet_unref(pkt);
    }

    if (!state || !state->pFormatCtx || state->video_stream < 0) {
        return FAILURE;
    }

    if (timeUs > -1) {
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

        switch (opt) {
            case OPTION_CLOSEST:
                desired_frame_number = seek_time;
                flags = AVSEEK_FLAG_BACKWARD;
                break;
            case OPTION_CLOSEST_SYNC:
            case OPTION_NEXT_SYNC:
                flags = 0;
                break;
            case OPTION_PREVIOUS_SYNC:
                flags = AVSEEK_FLAG_BACKWARD;
                break;
        }

        ret = av_seek_frame(state->pFormatCtx, stream_index, seek_time, flags);
        if (ret < 0) {
        	return FAILURE;
        } else {
        	if (state->audio_stream >= 0) {
        		avcodec_flush_buffers(state->audioDecoderCodecCtx);
        	}

        	if (state->video_stream >= 0) {
        		avcodec_flush_buffers(state->videoDecoderCodecCtx);
        	}
        }
    }

    decode_frame(state, pkt, &got_packet, desired_frame_number, width, height);

    if (got_packet) {
        // FILE *picture = fopen("/Users/wseemann/Desktop/frame.png", "wb");
        // fwrite(pkt->data, pkt->size, 1, picture);
        // fclose(picture);
    }

    return got_packet ? SUCCESS : FAILURE;
}

int set_native_window(State **ps, ANativeWindow* native_window) {
    printf("set_native_window\n");

	State *state = *ps;

	if (native_window == NULL) {
		return FAILURE;
	}

	if (!state) {
		init(&state);
	}

	state->native_window = native_window;

	*ps = state;

	return SUCCESS;
}

void release(State **ps) {
	printf("release\n");

	State *state = *ps;
	
    if (state) {
    	if (state->audioDecoderCodecCtx) {
    	    avcodec_free_context(&state->audioDecoderCodecCtx);
    	}

    	if (state->videoDecoderCodecCtx) {
    		avcodec_free_context(&state->videoDecoderCodecCtx);
    	}

        if (state->pFormatCtx->pb) {
        	avio_context_free(&state->pFormatCtx->pb);
        }

        if (state->pFormatCtx) {
    		avformat_close_input(&state->pFormatCtx);
    	}
    	
    	if (state->fd != -1) {
    		close(state->fd);
    	}
    	
    	if (state->sws_ctx) {
    		sws_freeContext(state->sws_ctx);
    		state->sws_ctx = NULL;
	    }

    	if (state->videoEncoderCodecCtx) {
    		avcodec_free_context(&state->videoEncoderCodecCtx);
    	}

    	if (state->scaled_codecCtx) {
    		avcodec_free_context(&state->scaled_codecCtx);
    	}

    	if (state->scaled_sws_ctx) {
    		sws_freeContext(state->scaled_sws_ctx);
    	}

        // make sure we don't leak native windows
        if (state->native_window != NULL) {
            ANativeWindow_release(state->native_window);
            state->native_window = NULL;
        }

    	av_freep(&state);
        ps = NULL;
    }
}

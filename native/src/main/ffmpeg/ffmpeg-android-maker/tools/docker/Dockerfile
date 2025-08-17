FROM --platform=linux/amd64 ubuntu:24.04

# Arguments that can be overridden in 'docker build' command:
# Versions of Android SDK and NDK.
ARG VERSION_SDK=13114758
ARG VERSION_NDK=28.1.13356709

# The HOME variable isn't available for ENV directive (during building an image).
# So we define one manually. For alpine and ubuntu it should be '/root'
ARG HOME_TWIN=/root

# Creating mandatory environment variables
ENV ANDROID_SDK_HOME=${HOME_TWIN}/android-sdk
ENV ANDROID_NDK_HOME=${ANDROID_SDK_HOME}/ndk/${VERSION_NDK}

# Installing basic software
RUN apt-get --allow-releaseinfo-change update && apt-get install -y --no-install-recommends \
  ninja-build \
  build-essential \
  openjdk-17-jdk-headless \
  curl \
  unzip \
  cmake \
  meson \
  bash \
  nasm \
  pkg-config \
  make \
  git \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Download the Android SDK
RUN curl https://dl.google.com/android/repository/commandlinetools-linux-${VERSION_SDK}_latest.zip --output ${HOME_TWIN}/android-sdk.zip
# Unzip it and remove the archive
RUN mkdir -p ${HOME_TWIN}/android-sdk && \
  unzip -qq ${HOME_TWIN}/android-sdk.zip -d ${HOME_TWIN}/android-sdk && \
  rm ${HOME_TWIN}/android-sdk.zip

# Installing components through the Android SDK
RUN installAndroidComponent() { yes | ${ANDROID_SDK_HOME}/cmdline-tools/bin/sdkmanager --sdk_root=${ANDROID_SDK_HOME} "$1" > /dev/null; } && \
  installAndroidComponent "ndk;${VERSION_NDK}"

# The command to be executed when a container is running
# Passing additional arguments to the script is done via FAM_ARGS environment variable
CMD cd /mnt/ffmpeg-android-maker && ./ffmpeg-android-maker.sh ${FAM_ARGS}

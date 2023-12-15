include("$ENV{ANDROID_NDK_HOME}/build/cmake/android.toolchain.cmake")

if("${CMAKE_SYSTEM_PROCESSOR}" STREQUAL "i686")
  set(CMAKE_C_FLAGS "-mpclmul -msse2 -maes")
endif()

cmake_minimum_required(VERSION 3.22.1)

project("core")

add_library(${CMAKE_PROJECT_NAME} SHARED
        dpi.cpp
        core.cpp)

# libbyedpi.so dosyasını bağla
add_library(byedpi SHARED IMPORTED)
set_target_properties(byedpi PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/lib/${ANDROID_ABI}/libbyedpi.so)

target_link_libraries(${CMAKE_PROJECT_NAME}
        android
        log
        byedpi) # libbyedpi.so dosyasını linkle

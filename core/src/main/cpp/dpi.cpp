#include <jni.h>
#include <android/log.h>
#include <unistd.h>

#define LOG_TAG "ByeDpiProxy"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" {
    jint Java_io_github_dovecoteescapee_byedpi_core_ByeDpiProxy_jniStartProxy(JNIEnv *env, jobject thiz, jint fd);
    jint Java_io_github_dovecoteescapee_byedpi_core_ByeDpiProxy_jniStopProxy(JNIEnv *env, jobject thiz, jint fd);
    jint Java_io_github_dovecoteescapee_byedpi_core_ByeDpiProxy_jniCreateSocketWithCommandLine(JNIEnv *env, jobject thiz, jobjectArray args);
}


extern "C"
JNIEXPORT jint JNICALL
Java_kurd_reco_core_ByeDpiProxy_jniStartProxy(JNIEnv *env, jobject thiz, jint fd) {
    LOGI("Proxy başlatılıyor: fd = %d", fd);
    return Java_io_github_dovecoteescapee_byedpi_core_ByeDpiProxy_jniStartProxy(env, thiz, fd);
}

extern "C"
JNIEXPORT jint JNICALL
Java_kurd_reco_core_ByeDpiProxy_jniStopProxy(JNIEnv *env, jobject thiz, jint fd) {
    LOGI("Proxy durduruluyor: fd = %d", fd);
    return Java_io_github_dovecoteescapee_byedpi_core_ByeDpiProxy_jniStopProxy(env, thiz, fd);
}

extern "C"
JNIEXPORT jint JNICALL
Java_kurd_reco_core_ByeDpiProxy_jniCreateSocketWithCommandLine(JNIEnv *env, jobject thiz, jobjectArray args) {
    return Java_io_github_dovecoteescapee_byedpi_core_ByeDpiProxy_jniCreateSocketWithCommandLine(env, thiz, args);
}

static JNINativeMethod methods[] = {
        {"jniStartProxy", "(I)I", (void*) Java_kurd_reco_core_ByeDpiProxy_jniStartProxy},
        {"jniStopProxy", "(I)I", (void*) Java_kurd_reco_core_ByeDpiProxy_jniStopProxy},
        {"jniCreateSocketWithCommandLine", "([Ljava/lang/String;)I", (void*) Java_kurd_reco_core_ByeDpiProxy_jniCreateSocketWithCommandLine}
};

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env = nullptr;
    if (vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    jclass clazz = env->FindClass("kurd/reco/core/ByeDpiProxy");
    if (clazz == nullptr) {
        LOGE("ByeDpiProxy sınıfı bulunamadı!");
        return -1;
    }

    if (env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0])) < 0) {
        LOGE("RegisterNatives başarısız!");
        return -1;
    }

    LOGI("JNI fonksiyonları başarıyla kaydedildi.");
    return JNI_VERSION_1_6;
}
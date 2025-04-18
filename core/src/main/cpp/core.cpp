#include <jni.h>
#include <cstring> // For strstr
#include <jni.h>
#include <cstdio>
#include <string>
#include <dirent.h>
#include <dlfcn.h>

// Critical API data that will be embedded in the binary
static const char* ENC_API_URL = "eT+HecDjd4Z0VpLCjNbCJRmpea/pjx5pi0uV6zlNp/bmrNnZy6kQP4loOsh6T6Br2i2itjMfjdOlFpOJrnc0bvVaMaK6YriI0m0nEWbTZ54=";
static const char* ENC_PLUGIN_URL = "eT+HecDjd4Z0VpLCjNbCJRmpea/pjx5pi0uV6zlNp/adu2bJnma9Uh4fKJemF+rJnUTEotSQQkpUDuoNzVnYm7QzWwlckV0CV7SqZN4z/AM=";

void th(JNIEnv *env, const char* message) {
    jclass throwableClass = env->FindClass("java/lang/Throwable");
    if (throwableClass != nullptr) {
        env->ThrowNew(throwableClass, message);
    }
}

// Frida detection functions
static bool checkFridaProcess() {
    FILE* file = fopen("/proc/self/maps", "r");
    if (file == nullptr) return false;

    char line[512];
    bool found = false;
    while (fgets(line, sizeof(line), file)) {
        if (strstr(line, "frida") || strstr(line, "gum-js-loop") || strstr(line, "gmain")) {
            found = true;
            break;
        }
    }
    fclose(file);
    return found;
}

static bool checkFridaThreads() {
    DIR* dir = opendir("/proc/self/task");
    if (dir == nullptr) return false;

    struct dirent* entry;
    bool found = false;
    while ((entry = readdir(dir)) != nullptr) {
        if (entry->d_type == DT_DIR && isdigit(entry->d_name[0])) {
            char path[256];
            snprintf(path, sizeof(path), "/proc/self/task/%s/status", entry->d_name);
            FILE* file = fopen(path, "r");
            if (file) {
                char line[256];
                while (fgets(line, sizeof(line), file)) {
                    if (strstr(line, "frida") || strstr(line, "gum-js-loop")) {
                        found = true;
                        break;
                    }
                }
                fclose(file);
            }
        }
    }
    closedir(dir);
    return found;
}

static bool checkFridaPorts() {
    FILE* file = fopen("/proc/net/tcp", "r");
    if (file == nullptr) return false;

    char line[256];
    bool found = false;
    // Skip header line
    fgets(line, sizeof(line), file);
    while (fgets(line, sizeof(line), file)) {
        if (strstr(line, ":27042") || strstr(line, ":8888")) {  // Common Frida ports
            found = true;
            break;
        }
    }
    fclose(file);
    return found;
}

static bool checkFridaLibraries() {
    void* handle = dlopen("libfrida-agent.so", RTLD_NOW);
    if (handle != nullptr) {
        dlclose(handle);
        return true;
    }
    return false;
}

extern "C"
JNIEXPORT jobjectArray JNICALL
Java_kurd_reco_core_SGCheck_checkSGIntegrity(JNIEnv *env, jobject thiz) {
    // Check for Frida
    if (checkFridaProcess() || checkFridaThreads() || checkFridaPorts() || checkFridaLibraries()) {
        th(env, "Security check failed");
        return nullptr;
    }

    // 1. PackageInfo class check
    jclass packageInfoClass = env->FindClass("android/content/pm/PackageInfo");
    if (packageInfoClass == nullptr) {
        th(env, "PackageInfo class not found");
        return nullptr;
    }

    // 2. Get CREATOR field from PackageInfo
    jfieldID creatorFieldID = env->GetStaticFieldID(packageInfoClass, "CREATOR", "Landroid/os/Parcelable$Creator;");
    if (creatorFieldID == nullptr) {
        th(env, "CREATOR field not found");
        return nullptr;
    }
    jobject creator = env->GetStaticObjectField(packageInfoClass, creatorFieldID);
    if (creator == nullptr) {
        th(env, "CREATOR is null");
        return nullptr;
    }

    // 3. Check if CREATOR is instance of Parcelable.Creator
    jclass parcelableCreatorClass = env->FindClass("android/os/Parcelable$Creator");
    if (!env->IsInstanceOf(creator, parcelableCreatorClass)) {
        th(env, "CREATOR is not instance of Parcelable.Creator");
        return nullptr;
    }

    // 4. Get CREATOR's class name
    jclass objectClass = env->FindClass("java/lang/Object");
    jmethodID getClassMethod = env->GetMethodID(objectClass, "getClass", "()Ljava/lang/Class;");
    if (getClassMethod == nullptr) {
        th(env, "getClass method not found");
        return nullptr;
    }
    jobject creatorClassObj = env->CallObjectMethod(creator, getClassMethod);
    if (creatorClassObj == nullptr) {
        th(env, "Could not obtain CREATOR's class");
        return nullptr;
    }
    jclass classClass = env->FindClass("java/lang/Class");
    jmethodID getNameMethod = env->GetMethodID(classClass, "getName", "()Ljava/lang/String;");
    if (getNameMethod == nullptr) {
        th(env, "getName method not found");
        return nullptr;
    }
    auto creatorClassNameString = (jstring)env->CallObjectMethod(creatorClassObj, getNameMethod);
    if (creatorClassNameString == nullptr) {
        th(env, "Could not get CREATOR class name");
        return nullptr;
    }
    const char* creatorClassNameCStr = env->GetStringUTFChars(creatorClassNameString, nullptr);
    const char* expectedSubstring = "android.content.pm.PackageInfo";
    if (strstr(creatorClassNameCStr, expectedSubstring) == nullptr) {
        env->ReleaseStringUTFChars(creatorClassNameString, creatorClassNameCStr);
        th(env, "CREATOR class name does not contain expected substring");
        return nullptr;
    }
    env->ReleaseStringUTFChars(creatorClassNameString, creatorClassNameCStr);

    // 5. Check if CREATOR is a proxy
    jclass proxyClass = env->FindClass("java/lang/reflect/Proxy");
    if (proxyClass == nullptr) {
        th(env, "Proxy class not found");
        return nullptr;
    }
    jmethodID isProxyClassMethod = env->GetStaticMethodID(proxyClass, "isProxyClass", "(Ljava/lang/Class;)Z");
    if (isProxyClassMethod == nullptr) {
        th(env, "isProxyClass method not found");
        return nullptr;
    }
    jboolean isProxy = env->CallStaticBooleanMethod(proxyClass, isProxyClassMethod, creatorClassObj);
    if (isProxy == JNI_TRUE) {
        th(env, "CREATOR is a proxy instance");
        return nullptr;
    }

    // 6. Check CREATOR's toString() result
    jmethodID toStringMethod = env->GetMethodID(objectClass, "toString", "()Ljava/lang/String;");
    if (toStringMethod == nullptr) {
        th(env, "toString method not found");
        return nullptr;
    }
    auto creatorString = (jstring)env->CallObjectMethod(creator, toStringMethod);
    if (creatorString == nullptr) {
        th(env, "toString() returned null");
        return nullptr;
    }
    const char* creatorStringCStr = env->GetStringUTFChars(creatorString, nullptr);
    if (strstr(creatorStringCStr, expectedSubstring) == nullptr) {
        env->ReleaseStringUTFChars(creatorString, creatorStringCStr);
        th(env, "CREATOR toString() does not contain expected substring");
        return nullptr;
    }
    env->ReleaseStringUTFChars(creatorString, creatorStringCStr);

    // All checks passed, return the encrypted API data
    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray result = env->NewObjectArray(2, stringClass, nullptr);
    
    env->SetObjectArrayElement(result, 0, env->NewStringUTF(ENC_API_URL));
    env->SetObjectArrayElement(result, 1, env->NewStringUTF(ENC_PLUGIN_URL));

    return result;
}


extern "C"
JNIEXPORT jstring JNICALL
Java_kurd_reco_core_SGCheck_getSG(JNIEnv *env, jobject thiz, jobject context) {
    // Context'ten PackageManager ve packageName bilgisini al
    jclass contextClass = env->GetObjectClass(context);
    if (!contextClass) return env->NewStringUTF("");

    jmethodID midGetPackageManager = env->GetMethodID(contextClass, "getPackageManager", "()Landroid/content/pm/PackageManager;");
    jobject packageManager = env->CallObjectMethod(context, midGetPackageManager);
    if (!packageManager) return env->NewStringUTF("");

    jmethodID midGetPackageName = env->GetMethodID(contextClass, "getPackageName", "()Ljava/lang/String;");
    auto packageName = (jstring)env->CallObjectMethod(context, midGetPackageName);
    if (!packageName) return env->NewStringUTF("");

    // SDK sürümünü al: Build.VERSION.SDK_INT
    jclass buildVersionClass = env->FindClass("android/os/Build$VERSION");
    if (!buildVersionClass) return env->NewStringUTF("");
    jfieldID fidSdkInt = env->GetStaticFieldID(buildVersionClass, "SDK_INT", "I");
    jint sdkInt = env->GetStaticIntField(buildVersionClass, fidSdkInt);

    // Flag belirle: SDK_INT >= 28 ise GET_SIGNING_CERTIFICATES, yoksa GET_SIGNATURES
    jint flag = 0;
    jclass packageManagerClass = env->GetObjectClass(packageManager);
    if (sdkInt >= 28) {
        jfieldID fidGetSigningCertificates = env->GetStaticFieldID(packageManagerClass, "GET_SIGNING_CERTIFICATES", "I");
        flag = env->GetStaticIntField(packageManagerClass, fidGetSigningCertificates);
    } else {
        jfieldID fidGetSignatures = env->GetStaticFieldID(packageManagerClass, "GET_SIGNATURES", "I");
        flag = env->GetStaticIntField(packageManagerClass, fidGetSignatures);
    }

    // PackageManager'dan PackageInfo nesnesini al: getPackageInfo(packageName, flag)
    jmethodID midGetPackageInfo = env->GetMethodID(packageManagerClass, "getPackageInfo", "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
    jobject packageInfo = env->CallObjectMethod(packageManager, midGetPackageInfo, packageName, flag);
    if (!packageInfo) return env->NewStringUTF("");

    // Signatures dizisini elde et:
    //    API 28 ve üzeri için: packageInfo.signingInfo.getApkContentsSigners()
    //    Alt sürümlerde: packageInfo.signatures
    jobjectArray signaturesArray = nullptr;
    jclass packageInfoClass = env->GetObjectClass(packageInfo);
    if (sdkInt >= 28) {
        // "signingInfo" alanını alıyoruz.
        jfieldID fidSigningInfo = env->GetFieldID(packageInfoClass, "signingInfo", "Landroid/content/pm/SigningInfo;");
        if (fidSigningInfo == nullptr) return env->NewStringUTF("");
        jobject signingInfo = env->GetObjectField(packageInfo, fidSigningInfo);
        if (!signingInfo) return env->NewStringUTF("");

        jclass signingInfoClass = env->GetObjectClass(signingInfo);
        jmethodID midGetApkContentsSigners = env->GetMethodID(signingInfoClass, "getApkContentsSigners", "()[Landroid/content/pm/Signature;");
        if (midGetApkContentsSigners == nullptr) return env->NewStringUTF("");
        signaturesArray = (jobjectArray)env->CallObjectMethod(signingInfo, midGetApkContentsSigners);
    } else {
        jfieldID fidSignatures = env->GetFieldID(packageInfoClass, "signatures", "[Landroid/content/pm/Signature;");
        if (fidSignatures == nullptr) return env->NewStringUTF("");
        signaturesArray = (jobjectArray)env->GetObjectField(packageInfo, fidSignatures);
    }
    if (!signaturesArray) return env->NewStringUTF("");

    jsize sigCount = env->GetArrayLength(signaturesArray);
    if (sigCount <= 0) return env->NewStringUTF("");

    // İlk Signature nesnesini al ve toByteArray() metodunu çağır
    jobject firstSignature = env->GetObjectArrayElement(signaturesArray, 0);
    if (!firstSignature) return env->NewStringUTF("");

    jclass signatureClass = env->GetObjectClass(firstSignature);
    jmethodID midToByteArray = env->GetMethodID(signatureClass, "toByteArray", "()[B");
    auto certByteArray = (jbyteArray)env->CallObjectMethod(firstSignature, midToByteArray);
    if (!certByteArray) return env->NewStringUTF("");

    // MessageDigest ile MD5 özetini hesapla
    jclass messageDigestClass = env->FindClass("java/security/MessageDigest");
    if (!messageDigestClass) return env->NewStringUTF("");

    jmethodID midGetInstance = env->GetStaticMethodID(messageDigestClass, "getInstance", "(Ljava/lang/String;)Ljava/security/MessageDigest;");
    if (!midGetInstance) return env->NewStringUTF("");
    jstring md5String = env->NewStringUTF("MD5");
    jobject messageDigest = env->CallStaticObjectMethod(messageDigestClass, midGetInstance, md5String);
    env->DeleteLocalRef(md5String);
    if (!messageDigest) return env->NewStringUTF("");

    jmethodID midDigest = env->GetMethodID(messageDigestClass, "digest", "([B)[B");
    if (!midDigest) return env->NewStringUTF("");
    auto digestByteArray = (jbyteArray)env->CallObjectMethod(messageDigest, midDigest, certByteArray);
    if (!digestByteArray) return env->NewStringUTF("");

    // MD5 özetini hexadecimal stringe çevir
    jsize digestLength = env->GetArrayLength(digestByteArray);
    jbyte* digestBytes = env->GetByteArrayElements(digestByteArray, nullptr);

    std::string hexStr;
    char buffer[3]; // 2 karakter + null terminator
    for (jsize i = 0; i < digestLength; i++) {
        sprintf(buffer, "%02X", (unsigned char)digestBytes[i]);
        hexStr.append(buffer);
    }
    env->ReleaseByteArrayElements(digestByteArray, digestBytes, 0);

    return env->NewStringUTF(hexStr.c_str());
}
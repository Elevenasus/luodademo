/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
#include <string.h>
#include <jni.h>
#include <dlfcn.h>
#include <stdio.h>
#include <stdlib.h>



jstring
Java_com_airoha_android_lib_jniWrapper_BasicDSP_compressFromJNI( JNIEnv* env, jobject thiz, jstring jlibpath, jstring jinputfilename, jstring joutputfilename)
{
    int rest = 0;
    int compressRet = -1;
    int (*compress)(char *infile, char *outfile);
    char* libpath ;
    libpath = (*env)->GetStringUTFChars( env, jlibpath , NULL ) ;
    char* inputfilename;
    inputfilename = (*env)->GetStringUTFChars( env, jinputfilename , NULL ) ;

    char *ouputfilname;
    ouputfilname = (*env)->GetStringUTFChars( env, joutputfilename , NULL ) ;

    void *myso = dlopen(libpath, RTLD_NOW);
    *(void **) (&compress) = dlsym(myso, "compress");
    if(&compress == NULL)
        return (*env)->NewStringUTF(env, "Function point NULL !" ".");

    compressRet = compress(inputfilename, ouputfilname);

    switch (compressRet){
        case 1:
            return (*env)->NewStringUTF(env, "error 1");
        case 2:
            return (*env)->NewStringUTF(env, "error 2, not a wav");
        case 3:
            return (*env)->NewStringUTF(env, "error 3, format not correct");
        case 4:
            return (*env)->NewStringUTF(env, "error 4, can't find data");
    }

    if(compressRet != 0)
        return (*env)->NewStringUTF(env, "Hello from JNI !  Compiled with ABI "  ". ====> compress error code:");

    return (*env)->NewStringUTF(env, "AMR_COMPRESSED");
}


jint
Java_com_airoha_android_spp_headset_SpeakerAdaptation_AdapterRecordMgr_pitchDetectorFromJNI(JNIEnv* env, jobject thiz, jstring jlibpath, jstring pcmpath)
{
    int (*compress)(int NP, short *Sx);
    char* libpath ;
    libpath = (*env)->GetStringUTFChars( env, jlibpath , NULL ) ;
    void *myso = dlopen(libpath, RTLD_NOW);
    *(void **) (&compress) = dlsym(myso, "pitch_detector");
    if(&compress == NULL)
        return -1;


    char* filepath ;
    filepath = (*env)->GetStringUTFChars( env, pcmpath , NULL ) ;

    FILE *fppcm;
    int wav_len,NP;
    short *Sx;
    int chk_s = 0;
    if ((fppcm = fopen(filepath, "rb")) == NULL)
        return -2;

    fseek(fppcm, 0, SEEK_END);
    wav_len = ftell(fppcm);//bytes
    wav_len /= sizeof(short);
    fseek(fppcm, 0, SEEK_SET);
    NP = wav_len-1;
    Sx = malloc(sizeof(short) * wav_len);
    fread(Sx, sizeof(short), wav_len, fppcm);

    chk_s = compress(NP, Sx);

    free(Sx);
    if(chk_s)
        return 0;
    else
        return 1;
}

/*jint
Java_com_airoha_android_spp_headset_SpeakerAdaptation_SpeakerAdaptationActivity_SpkAdaptFromJNI( JNIEnv* env, jobject thiz, jstring jlibpath, jshort jrecordNum, jshort jspkAdapt, jshort jlang, jshort jscenario)
{
    char* libpath ;
    libpath = (*env)->GetStringUTFChars( env, jlibpath , NULL ) ;

    short recordNum = (short)(jrecordNum & 0xFF);
    short spkAdapt = (short)(jspkAdapt & 0xFF);
    short lang = (short)(jlang & 0xFF);
    short scenario = (short)(jscenario & 0xFF);

    short chkVal=0;
    int freeLib=1;
    char ApkName[]={"/data/data/com.airoha.android.spp.headset/lib/"};
    void (*getLibPath)(char projectName[]);
    short (*SpkAdaptProcess)(short RecordNum,short SpeakerAdaptation,short language,short scenario);
    void *handle = dlopen(libpath, RTLD_NOW);
    *(void **) (&getLibPath) = dlsym(handle, "getLibPath");
    *(void **) (&SpkAdaptProcess) = dlsym(handle, "SpkAdaptProcess");

    if(!handle)
        return 1;

    getLibPath(ApkName);
    chkVal = SpkAdaptProcess(recordNum, spkAdapt, lang, scenario);

    freeLib = dlclose(handle);

    if(freeLib)
        return 2;

    if(chkVal)
        return 0;
    else
        return 3;
}*/
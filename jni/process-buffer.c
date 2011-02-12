/*
Copyright (C) 2010 Haowen Ning

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/

#include <jni.h>
#include <stdlib.h>
#include <math.h>
#include "fft.h"

void amprotate(float* re, float* im, float amp, int angle)
{
    float r = sqrtf(*re * *re + *im * *im);
    float theta = atan2f(*im, *re);
    float angle_rad = M_PI * (float)angle / 180.0;
    *re = r * amp * cosf(theta + angle_rad);
    *im = r * amp * sinf(theta + angle_rad);
}




void Java_org_liberty_android_noisecanceller_NoiseProcessor_processBuffer(JNIEnv* env, jobject obj, jfloat amp, jint angle)
{
    jfieldID fid;
    jclass cls;
    jshortArray buffer;
    jint bufferSize;
    short* orig;
    float* orig_re;
    float* trans_re;
    float* trans_im;
    float* invtrans_re;
    float* invtrans_im;
    int i;

    cls = (*env) -> GetObjectClass(env, obj);
    fid = (*env) -> GetFieldID(env, cls, "bufferSize", "I");
    bufferSize = (*env) -> GetIntField(env, obj, fid);
    fid = (*env) -> GetFieldID(env, cls, "buffer", "[S");
    buffer = (jshortArray)(*env) -> GetObjectField(env, obj, fid);
    orig = (short*)malloc(sizeof(short) * bufferSize);
    orig_re = (float*)malloc(sizeof(float) * bufferSize);
    trans_re = (float*)malloc(sizeof(float) * bufferSize);
    trans_im = (float*)malloc(sizeof(float) * bufferSize);
    invtrans_re = (float*)malloc(sizeof(float) * bufferSize);
    invtrans_im = (float*)malloc(sizeof(float) * bufferSize);

    (*env) -> GetShortArrayRegion(env, buffer, 0, bufferSize, orig);
    for(i = 0; i < bufferSize; i++){
        orig_re[i] = (float)orig[i];
    }
    RealFFT(bufferSize, orig_re, trans_re, trans_im );
    for(i = 0; i < bufferSize; i++){
        amprotate(&(trans_re[i]), &(trans_im[i]), amp, angle);
    }
    FFT(bufferSize, 1, trans_re, trans_im, invtrans_re, invtrans_im);  
    for(i = 0; i < bufferSize; i++){
        orig[i] = (short)invtrans_re[i];
    }
    (*env) -> SetShortArrayRegion(env, buffer, 0, bufferSize, orig);

    free(orig);
    free(orig_re);
    free(trans_re);
    free(trans_im);
    free(invtrans_re);
    free(invtrans_im);
}

jstring
Java_org_liberty_android_noisecanceller_NoiseProcessor_showHello(JNIEnv* env,
                                                                jobject obj){
    return (*env) -> NewStringUTF(env, "Set Amp and Phase to get the best noise cancellation effect");
}


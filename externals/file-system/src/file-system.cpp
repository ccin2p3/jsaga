#include "file-system.h"
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <errno.h>
#include <malloc.h>

//WARNING: not compatible with -mno-cygwin
#ifndef WIN32
    #include <pwd.h>
    #include <grp.h>
#endif
#include <iostream>

using namespace std;

#define DEFAULT_MODE 32768
#define TRUE  1
#define FALSE 0
#define SYMLINK_ERRNO_PERMISSIONDENIED -1
#define SYMLINK_ERRNO_DOESNOTEXIST -2
#define SYMLINK_ERRNO_ALREADYEXISTS -3
#define SYMLINK_ERRNO_INTERNALERROR -4


JNIEXPORT jboolean JNICALL Java_fr_in2p3_commons_filesystem_FileSystem_stat
  (JNIEnv *env, jobject, jstring jPath, jobject obj)
{
    const char *cPath = env->GetStringUTFChars(jPath, 0);
    struct stat buf;
    int ret = lstat(cPath, &buf);

    //file does not exist
    if (buf.st_mode==0 || buf.st_mode==7) {
        return FALSE;
    }

    int isdir = S_ISDIR(buf.st_mode);
    int isfile = S_ISREG(buf.st_mode);
#ifdef S_ISLNK
    int islink = S_ISLNK(buf.st_mode);
#else
    int islink = FALSE;
#endif

    //WARNING: on Windows, permissions outside of CYGWIN environment are always 666
    int perms = buf.st_mode - DEFAULT_MODE;
    int user_perms = perms/8/8;
    int group_perms = perms/8%8;
    int other_perms = perms%8%8;

    jclass cls = env->GetObjectClass(obj);
    env->SetBooleanField(obj, env->GetFieldID(cls, "isdir", "Z"), isdir);
    env->SetBooleanField(obj, env->GetFieldID(cls, "isfile", "Z"), isfile);
    env->SetBooleanField(obj, env->GetFieldID(cls, "islink", "Z"), islink);
    env->SetIntField(obj, env->GetFieldID(cls, "size", "I"), buf.st_size);
    env->SetIntField(obj, env->GetFieldID(cls, "user_perms", "I"), user_perms);
    env->SetIntField(obj, env->GetFieldID(cls, "group_perms", "I"), group_perms);
    env->SetIntField(obj, env->GetFieldID(cls, "other_perms", "I"), other_perms);
    if (islink == TRUE) {
       int size = 100;
       while (1) {
           char *buffer = (char *) malloc (size);
           int nchars = readlink (cPath, buffer, size);
           if (nchars < 0) {
               break;
           }
           if (nchars < size) {
               buffer[nchars] = '\0';
               env->SetObjectField(obj, env->GetFieldID(cls, "target", "Ljava/lang/String;"), env->NewStringUTF(buffer));
               free (buffer);
               break; /*return buffer;*/
           }
           free (buffer);
           size *= 2;
        }
    }
//WARNING: not compatible with -mno-cygwin
#ifndef WIN32
    struct passwd *pws = getpwuid(buf.st_uid);
    char *owner = pws->pw_name;
    env->SetObjectField(obj, env->GetFieldID(cls, "owner", "Ljava/lang/String;"), env->NewStringUTF(owner));
    struct group *grp = getgrgid(buf.st_gid);
    char *group = grp->gr_name;
    env->SetObjectField(obj, env->GetFieldID(cls, "group", "Ljava/lang/String;"), env->NewStringUTF(group));
#endif
    env->SetLongField(obj, env->GetFieldID(cls, "atime", "J"), buf.st_atime);
    env->SetLongField(obj, env->GetFieldID(cls, "mtime", "J"), buf.st_mtime);
    env->SetLongField(obj, env->GetFieldID(cls, "ctime", "J"), buf.st_ctime);

    env->ReleaseStringUTFChars(jPath, cPath);
    return TRUE;
}

JNIEXPORT jboolean JNICALL Java_fr_in2p3_commons_filesystem_FileSystem_chmod
  (JNIEnv *env, jobject, jstring jPath, jint user_perms, jint group_perms, jint other_perms)
{
    const char *cPath = env->GetStringUTFChars(jPath, 0);
    mode_t perms = user_perms*8*8 + group_perms*8 + other_perms;
    int ret = chmod(cPath, perms);
    env->ReleaseStringUTFChars(jPath, cPath);
    return TRUE;
}

JNIEXPORT jint JNICALL Java_fr_in2p3_commons_filesystem_FileSystem_symlink
  (JNIEnv *env, jobject, jstring jOldPath, jstring jNewPath)
{
    const char *cOldPath = env->GetStringUTFChars(jOldPath, 0);
    const char *cNewPath = env->GetStringUTFChars(jNewPath, 0);
    int ret = symlink(cOldPath, cNewPath);
    env->ReleaseStringUTFChars(jOldPath, cOldPath);
    env->ReleaseStringUTFChars(jNewPath, cNewPath);
    if (ret == 0) { return 0; }
    switch (errno) {
       case EACCES:
       case EFAULT:
       case EROFS:
       case EPERM:
         return SYMLINK_ERRNO_PERMISSIONDENIED;
         break;
       case ENOENT:
         return SYMLINK_ERRNO_DOESNOTEXIST;
         break;
       case EEXIST:
         return SYMLINK_ERRNO_ALREADYEXISTS;
         break;
       default:
         return SYMLINK_ERRNO_INTERNALERROR;
         break;
    }
}


JNIEXPORT void JNICALL Java_fr_in2p3_commons_filesystem_FileSystem_intArray
  (JNIEnv *env, jobject, jintArray arr)
{
    jsize len = env->GetArrayLength(arr);
    jint *vals = env->GetIntArrayElements(arr,0);
    cout << vals[0] << " - " << vals[1] << "..." << endl;
    env->ReleaseIntArrayElements(arr,vals,0);
}

JNIEXPORT void JNICALL Java_fr_in2p3_commons_filesystem_FileSystem_stringArray
  (JNIEnv * env, jobject obj, jobjectArray arr)
{
    jsize len = env->GetArrayLength(arr);
    for(int i=0; arr!=NULL && i<len; i++) {
        jstring st = (jstring) env->GetObjectArrayElement(arr,i);
        const char *cStr = env->GetStringUTFChars(st,0);
        cout << cStr << endl;
    }
}

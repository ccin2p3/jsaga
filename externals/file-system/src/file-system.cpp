#include "file-system.h"
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <errno.h>
#include <malloc.h>
#include <string.h>

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
               // If target is a directory, add / at the end
               //struct stat targetstat;
               //int targetret = lstat(buffer, &targetstat);
               ret = stat(cPath, &buf);
               //target exist
               if (buf.st_mode!=0 && buf.st_mode!=7) {
                   if (S_ISDIR(buf.st_mode)) {
                       buffer[nchars] = '/';
                       buffer[nchars+1] = '\0';
                   }
               }
               
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

JNIEXPORT void JNICALL Java_fr_in2p3_commons_filesystem_FileSystem_symlink
  (JNIEnv *env, jobject, jstring jOldPath, jstring jNewPath)
{
    const char *cOldPath = env->GetStringUTFChars(jOldPath, 0);
    const char *cNewPath = env->GetStringUTFChars(jNewPath, 0);
    int ret = symlink(cOldPath, cNewPath);
    int thisError = errno;
    env->ReleaseStringUTFChars(jOldPath, cOldPath);
    env->ReleaseStringUTFChars(jNewPath, cNewPath);
    if (ret == 0) { return ; }
    switch (thisError) {
       case EACCES:
       case EFAULT:
       case EROFS:
       case EPERM:
         env->ThrowNew(env->FindClass("fr/in2p3/commons/filesystem/FileSystemException"), "5:Permission denied");
         break;
       case ENOENT:
         env->ThrowNew(env->FindClass("fr/in2p3/commons/filesystem/FileSystemException"), "1:File not found");
         break;
       case EEXIST:
         env->ThrowNew(env->FindClass("fr/in2p3/commons/filesystem/FileSystemException"), "2:File already exists");
         break;
       default:
         env->ThrowNew(env->FindClass("fr/in2p3/commons/filesystem/FileSystemException"), "8:Internal I/O error");
         break;
    }
}

/*
JNIEXPORT jint JNICALL Java_fr_in2p3_commons_filesystem_FileSystem_symlinkold
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
         return PERMISSIONDENIED;
         break;
       case ENOENT:
         return FILEDOESNOTEXIST;
         break;
       case EEXIST:
         return FILEALREADYEXISTS;
         break;
       default:
         return INTERNALERROR;
         break;
    }
}
*/

JNIEXPORT void JNICALL Java_fr_in2p3_commons_filesystem_FileSystem_chown
  (JNIEnv *env, jobject, jstring jPath, jstring jUsername, jstring jUsergroup)
{
//WARNING: not compatible with -mno-cygwin
#ifndef WIN32
    const char *cUsername = env->GetStringUTFChars(jUsername, 0);
    uid_t newUid = -1;
    if (strlen(cUsername) > 0) {
      struct passwd *pws = getpwnam(cUsername);
      if (pws == NULL) {
        switch (errno) {
          case 0:
          case ESRCH:
          case EBADF:
          case EPERM:
          case ENOENT:
            env->ThrowNew(env->FindClass("fr/in2p3/commons/filesystem/FileSystemException"), "3:User does not exist");
            break;
          default:
            env->ThrowNew(env->FindClass("fr/in2p3/commons/filesystem/FileSystemException"), "8:Could not getpwnam");
            break;
        }
        env->ReleaseStringUTFChars(jUsername, cUsername);
        return;
      }
      newUid = pws->pw_uid;
    }
    env->ReleaseStringUTFChars(jUsername, cUsername);

    const char *cUsergroup = env->GetStringUTFChars(jUsergroup, 0);
    gid_t newGid = -1;
    if (strlen(cUsergroup) > 0) {
      struct group *grp = getgrnam(cUsergroup);
      if (grp == NULL) {
        switch (errno) {
          case 0:
          case ESRCH:
          case EBADF:
          case EPERM:
          case ENOENT:
            env->ThrowNew(env->FindClass("fr/in2p3/commons/filesystem/FileSystemException"), "4:Group does not exist");
            break;
          default:
            env->ThrowNew(env->FindClass("fr/in2p3/commons/filesystem/FileSystemException"), "8:Could not getgrnam");
            break;
        }
        env->ReleaseStringUTFChars(jUsergroup, cUsergroup);
        return;
      }
      newGid = grp->gr_gid;
    }
    env->ReleaseStringUTFChars(jUsergroup, cUsergroup);

    const char *cPath = env->GetStringUTFChars(jPath, 0);
    int ret = chown(cPath, newUid, newGid);
    int thisError = errno;
    env->ReleaseStringUTFChars(jPath, cPath);
    if (ret == 0) { return ; }
    switch (thisError) {
       case EACCES:
       case EFAULT:
       case EROFS:
       case EPERM:
         env->ThrowNew(env->FindClass("fr/in2p3/commons/filesystem/FileSystemException"), "5:Permission denied");
         break;
       case ENOENT:
         env->ThrowNew(env->FindClass("fr/in2p3/commons/filesystem/FileSystemException"), "1:File not found");
         break;
       default:
         env->ThrowNew(env->FindClass("fr/in2p3/commons/filesystem/FileSystemException"), "8:Could not chown");
         break;
    }
#else
    env->ThrowNew(env->FindClass("fr/in2p3/commons/filesystem/FileSystemException"), "6:Not implemented");
    //return NOTSUPPORTED;
#endif
}

JNIEXPORT jobjectArray JNICALL Java_fr_in2p3_commons_filesystem_FileSystem_getgrouplist
  (JNIEnv *env, jobject, jstring jUsername)
{
//WARNING: not compatible with -mno-cygwin
#ifndef WIN32
    //jthrowable exception = 0;
    //jclass exc_class;
    //jmethodID cid;
    
    //exc_class = env->FindClass("fr/in2p3/commons/filesystem/FileSystemException");
    //cid = env->GetMethodID(exc_class,"<init>","(ILjava/lang/String;)V");
    
    const char *cUsername = env->GetStringUTFChars(jUsername, 0);
    if (strlen(cUsername) == 0) {
      env->ReleaseStringUTFChars(jUsername, cUsername);
      env->ThrowNew(env->FindClass("fr/in2p3/commons/filesystem/FileSystemException"), "3:User is empty");
      return NULL;
    }
    struct passwd *pws = getpwnam(cUsername);
    if (pws == NULL) {
        int thisError = errno;
        env->ReleaseStringUTFChars(jUsername, cUsername);
        switch (thisError) {
          case 0:
          case ESRCH:
          case EBADF:
          case EPERM:
          case ENOENT:
            env->ThrowNew(env->FindClass("fr/in2p3/commons/filesystem/FileSystemException"), "3:User does not exist");
            break;
          default:
            env->ThrowNew(env->FindClass("fr/in2p3/commons/filesystem/FileSystemException"), "8:Could not getpwnam");
            break;
        }
        return NULL;
    }
    int j,ngroups;
    gid_t *groups;
    struct group *gr;

#define STEP 1
#define MAXGROUPS 20

    ngroups = STEP;
    groups = (gid_t *) malloc(ngroups * sizeof (gid_t));

    while (ngroups < MAXGROUPS && getgrouplist(cUsername, pws->pw_gid, groups, &ngroups) == -1) {
      ngroups += STEP;
      groups = (gid_t *) malloc(ngroups * sizeof (gid_t));
      if (groups == NULL) {
          env->ReleaseStringUTFChars(jUsername, cUsername);
          env->ThrowNew(env->FindClass("fr/in2p3/commons/filesystem/FileSystemException"), "8:Could not malloc");
          return NULL;
      }
    }

    env->ReleaseStringUTFChars(jUsername, cUsername);

    if (ngroups >= MAXGROUPS) {
        env->ThrowNew(env->FindClass("fr/in2p3/commons/filesystem/FileSystemException"), "8:Too many groups");
        return NULL;
    }
    jobjectArray jGroupsArray = (jobjectArray)env->NewObjectArray(ngroups, env->FindClass("java/lang/String"),env->NewStringUTF("ee"));

    for (j = 0; j < ngroups; j++) {
        gr = getgrgid(groups[j]);
        if (gr != NULL)
            env->SetObjectArrayElement(jGroupsArray, j, env->NewStringUTF(gr->gr_name));
    }

    return jGroupsArray;
#else
    env->ThrowNew(env->FindClass("fr/in2p3/commons/filesystem/FileSystemException"), "6:Not implemented");
    return NULL;
#endif
}

/*
JNIEXPORT jint JNICALL Java_fr_in2p3_commons_filesystem_FileSystem_getgrouplistold
  (JNIEnv *env, jobject, jstring jUsername, jobjectArray jGroupsArray)
{
//WARNING: not compatible with -mno-cygwin
#ifndef WIN32
    const char *cUsername = env->GetStringUTFChars(jUsername, 0);
    if (strlen(cUsername) == 0) {
      env->ReleaseStringUTFChars(jUsername, cUsername);
      return USERDOESNOTEXIST;
    }
    struct passwd *pws = getpwnam(cUsername);
    if (pws == NULL) {
        env->ReleaseStringUTFChars(jUsername, cUsername);
        switch (errno) {
          case ESRCH:
          case EBADF:
          case EPERM:
          case ENOENT:
            return USERDOESNOTEXIST;
            break;
          default:
            return INTERNALERROR;
            break;
        }
    }
    int j,ngroups;
    gid_t *groups;
    struct group *gr;

#define STEP 1
#define MAXGROUPS 20

    ngroups = STEP;
    groups = (gid_t *) malloc(ngroups * sizeof (gid_t));

    while (ngroups < MAXGROUPS && getgrouplist(cUsername, pws->pw_gid, groups, &ngroups) == -1) {
      ngroups += STEP;
      groups = (gid_t *) malloc(ngroups * sizeof (gid_t));
      if (groups == NULL) {
          env->ReleaseStringUTFChars(jUsername, cUsername);
          return INTERNALERROR;
      }
    }

    env->ReleaseStringUTFChars(jUsername, cUsername);

    if (ngroups >= MAXGROUPS) {
        return INTERNALERROR;
    }
    jGroupsArray = (jobjectArray)env->NewObjectArray(ngroups, env->FindClass("java/lang/String"),env->NewStringUTF("ee"));

    for (j = 0; j < ngroups; j++) {
        gr = getgrgid(groups[j]);
        if (gr != NULL)
            env->SetObjectArrayElement(jGroupsArray, j, env->NewStringUTF(gr->gr_name));
    }

    return 0;
#else
    return NOTSUPPORTED;
#endif
}
*/

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

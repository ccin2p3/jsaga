#!/bin/bash

function decode () {
    WORKER_PATH=$1
    FUNCTION=$2
    if test -x /usr/bin/uudecode ; then
        (
            echo "begin-base64 644 ${WORKER_PATH}"
            ${FUNCTION}
            echo "===="
        ) | /usr/bin/uudecode
    else
        ${FUNCTION} | /usr/bin/openssl enc -d -base64 -out ${WORKER_PATH}
    fi
}

function decode_append () {
    WORKER_PATH=$1
    FUNCTION=$2
    if test -x /usr/bin/uudecode ; then
        (
            echo "begin-base64 644 ${WORKER_PATH}.tmp"
            ${FUNCTION}
            echo "===="
        ) | /usr/bin/uudecode
        /usr/bin/cat ${WORKER_PATH}.tmp >> ${WORKER_PATH}
        rm -f ${WORKER_PATH}.tmp
    else
        ${FUNCTION} | /usr/bin/openssl enc -d -base64 >> ${WORKER_PATH}
    fi
}

function encode () {
    WORKER_PATH=$1
    LOCAL_PATH=$2
    if test -x /usr/bin/uuencode ; then
        /usr/bin/uuencode -m ${WORKER_PATH} ${LOCAL_PATH}
    else
        echo "begin-base64 644 ${LOCAL_PATH}"
    	/usr/bin/openssl enc -base64 -in ${WORKER_PATH}
    	echo "===="
    fi
}


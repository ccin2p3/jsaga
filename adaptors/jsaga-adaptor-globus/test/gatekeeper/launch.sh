#!/bin/sh

# set classpath
if test ! -f build/cp.txt ; then
    mvn dependency:build-classpath -Dmdep.cpFile=build/cp.txt
fi
cpSep=":"
grep ';' build/cp.txt && cpSep=";"

export CLASSPATH="test/gatekeeper/cog-jobmanager.jar$cpSep`cat build/cp.txt`"

# set java
if test -z "$JAVA_HOME" ; then
  JAVA=java
else
  JAVA="$JAVA_HOME/bin/java"
fi

# set X509_* environment variables
#USER_HOME="e:/User Settings"
USER_HOME=$HOME
X509_USER_PROXY=$USER_HOME/.jsaga/tmp/globus_cred.txt
X509_CERT_DIR=$USER_HOME/.jsaga/contexts/globus/certificates/

# set system properties
PROPERTIES=
PROPERTIES="$PROPERTIES -DGLOBUS_LOCATION=build/"
PROPERTIES="$PROPERTIES -DX509_USER_PROXY=\"$X509_USER_PROXY\""
PROPERTIES="$PROPERTIES -DX509_CERT_DIR=\"$X509_CERT_DIR\""

# run command
CMD="\"$JAVA\" $PROPERTIES -cp \"$CLASSPATH\" org.globus.gatekeeper.Gatekeeper $*"
if test -n "$DEBUG" ; then
  echo $CMD
fi
eval $CMD

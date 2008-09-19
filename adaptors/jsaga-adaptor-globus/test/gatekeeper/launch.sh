#!/bin/sh

# set classpath
if test ! -f build/cp.txt ; then
    mvn dependency:build-classpath -Dmdep.cpFile=build/cp.txt
fi
export CLASSPATH="test/gatekeeper/cog-jobmanager.jar;`cat build/cp.txt`"

# set java
if test -z "$JAVA_HOME" ; then
  JAVA=java
else
  JAVA="$JAVA_HOME/bin/java"
fi

# set X509_* environment variables
cd build/
JSAGA_HELP="\"$JAVA\" -cp \"$CLASSPATH;classes\" \
    -Djsaga.universe=file:../test/resources/etc/jsaga-universe.xml
    fr.in2p3.jsaga.command.Help"
X509_USER_PROXY=`eval $JSAGA_HELP -a Test_Globus.UserProxy | tr -d "\r\n"`
X509_CERT_DIR=`eval $JSAGA_HELP -a Test_Globus.CertRepository | tr -d "\r\n"`
cd -
echo "X509_USER_PROXY=$X509_USER_PROXY"
echo "X509_CERT_DIR=$X509_CERT_DIR"

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

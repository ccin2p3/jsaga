#!/bin/sh

# set JSAGA_HOME
if test -z "$JSAGA_HOME" ; then
  JSAGA_HOME=.
fi

# set system properties
PROPERTIES=
PROPERTIES="${PROPERTIES} -Dinteractive"
#PROPERTIES="${PROPERTIES} -Ddebug"
#PROPERTIES="${PROPERTIES} -Dconfig=$JSAGA_HOME/etc/example/jsaga-config.xml"

# build classpath
if test "$OS"="OS=Windows_NT" ; then
  SEP=";"
else
  SEP=":"
fi
CLASSPATH=.
for i in `ls $JSAGA_HOME/lib/*` ; do
  CLASSPATH="${CLASSPATH}${SEP}${i}"
done
for i in `ls $JSAGA_HOME/lib-adaptors/*` ; do
  CLASSPATH="${CLASSPATH}${SEP}${i}"
done
if test -n "$DEBUG" ; then
  echo $CLASSPATH
fi

# run java
if test -z "JAVA_HOME" ; then
  JAVA=java
else
  JAVA="$JAVA_HOME/bin/java"
fi
"$JAVA" $PROPERTIES -cp $CLASSPATH ${class.name} $*

#!/bin/sh


# set arguments
ARGS=$*


#
if test "$OS" = "Windows_NT"
then
  SEP=";"
else
  SEP=":"
fi


# set JSAGA_HOME
if test -z "$JSAGA_HOME" ; then
  if test -d "%INSTALL_PATH" ; then
    JSAGA_HOME="%INSTALL_PATH"
  else
    JSAGA_HOME=.
  fi
fi

# set system properties
PROPERTIES=
PROPERTIES="${PROPERTIES} -DJSAGA_HOME=$JSAGA_HOME"
PROPERTIES="${PROPERTIES} -Dinteractive"
#PROPERTIES="${PROPERTIES} -Ddebug"
#PROPERTIES="${PROPERTIES} -Dconfig=$JSAGA_HOME/etc/example/jsaga-config.xml"

# set classpath
CLASSPATH=.
for i in `ls $JSAGA_HOME/lib/*.jar` ; do
  CLASSPATH="${CLASSPATH}${SEP}${i}"
done
for i in `ls $JSAGA_HOME/lib-adaptors/*.jar` ; do
  CLASSPATH="${CLASSPATH}${SEP}${i}"
done

# set java
if test -z "$JAVA_HOME" ; then
  JAVA=java
else
  JAVA="$JAVA_HOME/bin/java"
fi

# run command
CMD="\"$JAVA\" $PROPERTIES -cp \"$CLASSPATH\" ${class.name} $*"
if test -n "$DEBUG" ; then
  echo $CMD
fi
eval $CMD

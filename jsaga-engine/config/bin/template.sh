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
#PROPERTIES="${PROPERTIES} -Ddebug"

# set classpath
CLASSPATH=.
for i in $JSAGA_HOME/lib/*.jar ; do
  CLASSPATH="${CLASSPATH}${SEP}${i}"
done
for i in $JSAGA_HOME/lib-adaptors/*.jar ; do
  CLASSPATH="${CLASSPATH}${SEP}${i}"
done
if test "${class.name}" = "junit.textui.TestRunner" ; then
    for i in $JSAGA_HOME/lib-test/*.jar ; do
      CLASSPATH="${CLASSPATH}${SEP}${i}"
    done
fi

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

#!/bin/sh

JSAGA_HOME=${0%/*}

which dos2unix 2> /dev/null
if test $? -eq 0 ; then
	dos2unix $JSAGA_HOME/bin/*.sh
else
	perl -pi -e 's/\r\n/\n/;' $JSAGA_HOME/bin/*.sh
fi

chmod 0755 $JSAGA_HOME/bin/*.sh
echo "permissions set to 0755"

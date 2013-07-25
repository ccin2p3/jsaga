#!/bin/sh

cp ./doc/etc/jsaga-default-contexts.xml ./etc/
./examples/jsaga-help.bat -s default

./examples/jsaga-context-info.bat
./examples/jsaga-context-init.bat
./examples/jsaga-context-info.bat

./examples/jsaga-job-run.bat
./examples/jsaga-job-run.bat -Executable /bin/hostname -r fork://localhost
cat doc/job-resources.txt | while read url ; do echo $url ; ./examples/jsaga-job-run.bat -Executable /examples/hostname -r "$url" ; done

./examples/jsaga-ls.bat -l file://c:/
cat doc/data-resources.txt | while read url ; do echo $url ; ./examples/jsaga-ls.bat -l $url ; done
./examples/jsaga-cat.bat

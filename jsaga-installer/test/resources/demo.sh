#!/bin/sh

cp ./doc/etc/jsaga-default-contexts.xml ./etc/
./bin/jsaga-help.bat -s default

./bin/jsaga-context-info.bat
./bin/jsaga-context-init.bat
./bin/jsaga-context-info.bat

./bin/jsaga-job-run.bat
./bin/jsaga-job-run.bat -Executable /bin/hostname -r fork://localhost
cat doc/job-resources.txt | while read url ; do echo $url ; ./bin/jsaga-job-run.bat -Executable /bin/hostname -r "$url" ; done

./bin/jsaga-ls.bat -l file://c:/
cat doc/data-resources.txt | while read url ; do echo $url ; ./bin/jsaga-ls.bat -l $url ; done
./bin/jsaga-cat.bat

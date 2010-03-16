#!/bin/sh

./bin/jsaga-help.bat -s default

# copier jsaga-universe.xml

./bin/jsaga-context-info.bat
./bin/jsaga-context-init.bat
./bin/jsaga-context-info.bat

./bin/jsaga-job-run.bat
./bin/jsaga-job-run.bat -Executable /bin/hostname -r local://localhost
cat job-resources.txt | while read url ; do echo $url ; ./bin/jsaga-job-run.bat -Executable /bin/hostname -r "$url" ; done

./bin/jsaga-ls.bat -l file://c:/
cat data-resources.txt | while read url ; do echo $url ; ./bin/jsaga-ls.bat -l $url ; done
./bin/jsaga-cat.bat

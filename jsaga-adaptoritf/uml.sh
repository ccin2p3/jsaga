#!/bin/sh

M2_REPO="E:/User Settings/.m2/repository"
DOT_HOME="../jsaga-installer/build/jsaga-installer-0.9.3-SNAPSHOT-bin.dir/jsaga-0.9.3-SNAPSHOT/lib"

UML_GRAPH="$M2_REPO/gr/spinellis/UmlGraph/4.6/UmlGraph-4.6.jar"

function install {
    if test ! -f "$UML_GRAPH" ; then
        curl -o build/uml.jar http://repo1.maven.org/maven2/gr/spinellis/UmlGraph/4.6/UmlGraph-4.6.jar
        mvn install:install-file -DgroupId=gr.spinellis -DartifactId=UmlGraph -Dversion=4.6 -Dpackaging=jar -Dfile=build/uml.jar
        rm -f build/uml.jar
    fi
}

function graph {
    javadoc \
        -docletpath "$UML_GRAPH" \
        -doclet gr.spinellis.umlgraph.doclet.UmlGraph \
        -sourcepath src \
        -subpackages fr.in2p3.jsaga.adaptor.$1
    if test $? -ne 0 ; then exit 1 ; fi
    "$DOT_HOME/win32/dot" -Tgif -obuild/graph-$1.gif graph.dot
}

install
graph data
graph security
graph job

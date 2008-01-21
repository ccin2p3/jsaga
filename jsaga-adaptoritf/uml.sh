#!/bin/sh

M2_REPO="E:/User Settings/.m2/repository"
DOT_HOME="E:/User Settings/Bureau/JSAGA-devel/maven/dot"

function graph {
    javadoc \
	-docletpath "$M2_REPO/gr/spinellis/UmlGraph/4.6/UmlGraph-4.6.jar" \
	-doclet gr.spinellis.umlgraph.doclet.UmlGraph \
	-sourcepath src \
	-subpackages fr.in2p3.jsaga.adaptor.$1
    "$DOT_HOME/bin/dot" -Tgif -obuild/graph-$1.gif graph.dot
}

graph data
graph security
graph job

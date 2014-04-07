#!/bin/bash

if [ $# -ne 4 ]; then 
    echo Usage: $0 N M edges_file vertices\_file
    exit 1
fi

N=$1
M=$2
fname=$3
vfile=$4

cd src
javac *.java
# convert edges to bytes, in ($fname)_min1
java Utility ceb $fname
# generate files ($fname)_min1.{DestSorted,OriginSorted}
java Main $fname\_min1 $N sort
# generate files ($fname)_min1.Topo{Edges,Vertices}
java Main $fname\_min1 $N topoSort
# generate file 'water.txt'
java Main $fname\_min1 $N WaterFlow $M

# generate 'vertices.txt'
java Utility pd $N $fname\_min1.TopoVertices > vertices.txt
cd ..

cp src/vertices.txt src/water.txt src/$4 .

python graphingTest/graph.py $vfile water.txt vertices.txt

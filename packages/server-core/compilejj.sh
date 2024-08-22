#!/usr/bin/env bash

PWD=`pwd`
echo "Generating ${PWD}"
cd src/main/kotlin/org/migor/feedless/document/filter \
   && javacc *.jj \
   && rm -rf generated \
   && mkdir generated \
   && mv *.java generated

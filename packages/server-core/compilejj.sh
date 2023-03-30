#!/usr/bin/env bash

PWD=`pwd`
echo "Generating complex ${PWD}"
cd src/main/kotlin/org/migor/rich/rss/harvest/entryfilter/complex \
   && javacc *.jj \
   && rm -rf generated \
   && mkdir generated \
   && mv *.java generated

cd -
cd src/main/kotlin/org/migor/rich/rss/harvest/entryfilter/simple \
   && javacc *.jj \
   && rm -rf generated \
   && mkdir generated \
   && mv *.java generated

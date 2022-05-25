#!/usr/bin/env bash

OLDPWD=`pwd`

echo "Generating simple"
cd src/main/kotlin/org/migor/rich/rss/harvest/entryfilter/simple \
   && javacc *.jj \
   && rm -rf generated \
   && mkdir generated \
   && mv *.java generated

cd ${OLDPWD}

echo "Generating complex"
cd src/main/kotlin/org/migor/rich/rss/harvest/entryfilter/complex \
   && javacc *.jj \
   && rm -rf generated \
   && mkdir generated \
   && mv *.java generated

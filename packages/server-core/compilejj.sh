#!/usr/bin/env bash

echo "Generating complex"
cd src/main/kotlin/org/migor/rich/rss/harvest/entryfilter/complex \
   && javacc *.jj \
   && rm -rf generated \
   && mkdir generated \
   && mv *.java generated

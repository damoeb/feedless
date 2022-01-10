#!/usr/bin/env bash

echo "Generating sources"
cd src/main/kotlin/org/migor/rich/rss/harvest/entryfilter \
   && javacc *.jj \
   && rm -f generated/*.java \
   && mv *.java generated

#!/usr/bin/env bash

echo "Generating sources"
cd src/main/kotlin/org/migor/rss/rich/filter \
   && javacc *.jj \
   && rm -f generated/*.java \
   && mv *.java generated

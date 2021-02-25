#!/usr/bin/env bash

echo "Cleaning generated sources"
cd src/main/kotlin/org/migor/rss/rich/filter \
   && rm -f generated/*.java \

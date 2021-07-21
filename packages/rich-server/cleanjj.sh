#!/usr/bin/env bash

echo "Cleaning generated sources"
cd src/main/kotlin/org/migor/rss/rich/harvest/entryfilter \
   && rm -f generated/*.java \

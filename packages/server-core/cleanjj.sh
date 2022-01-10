#!/usr/bin/env bash

echo "Cleaning generated sources"
cd src/main/kotlin/org/migor/rich/rss/harvest/entryfilter \
   && rm -f generated/*.java \

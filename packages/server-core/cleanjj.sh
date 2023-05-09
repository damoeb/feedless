#!/usr/bin/env bash

echo "Cleaning generated sources"
cd src/main/kotlin/org/migor/feedless/harvest/entryfilter \
   && rm -f generated/*.java \

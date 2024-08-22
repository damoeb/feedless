#!/usr/bin/env bash

echo "Cleaning generated sources"
cd src/main/kotlin/org/migor/feedless/document/filter \
   && rm -f generated/*.java \

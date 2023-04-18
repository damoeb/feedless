#!/usr/bin/env sh

echo 'Validating env'
nslookup br.de | grep br.de

exit 1

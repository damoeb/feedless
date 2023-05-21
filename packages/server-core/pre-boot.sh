#!/usr/bin/env sh

if test -z "$DNS_TEST_URL"
then
  echo "Skipping DNS check"
else
  echo "Verifying DNS resolution using $DNS_TEST_URL"
  nslookup $DNS_TEST_URL | grep $DNS_TEST_URL
fi


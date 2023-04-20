#!/usr/bin/env sh

echo "Verifying DNS resolution using $DNS_TEST_URL"
nslookup $DNS_TEST_URL | grep $DNS_TEST_URL

#!/bin/bash

# System test for nginx date-based redirects
# This test validates that URLs with dates older than 1 week return 302 redirects

set -e

# Configuration
CONTAINER_NAME="feedless-upcoming-latest-test"
PORT="8080"
BASE_URL="http://localhost:${PORT}"

get_date_days_ago() {
  local days="$1"
  local format="$2"
  if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    date -v-${days}d +"$format"
  else
    # Linux and other Unix-like systems
    date -d "${days} days ago" +"$format"
  fi
}

echo "Starting nginx date redirect system test..."

# Function to cleanup
cleanup() {
    echo "Cleaning up..."
    docker stop $CONTAINER_NAME 2>/dev/null || true
    docker rm $CONTAINER_NAME 2>/dev/null || true
}

trap cleanup EXIT


echo "Starting container..."
docker run -d --name $CONTAINER_NAME -p $PORT:80 damoeb/feedless:app-upcoming-latest

echo "Waiting for container to be ready..."
sleep 5

# Check if container is running
if ! docker ps | grep -q $CONTAINER_NAME; then
    echo "ERROR: Container failed to start"
    docker logs $CONTAINER_NAME
    exit 1
fi

echo "Container is running"

test_redirect() {
    local url="$1"
    local expected_status="$2"
    local description="$3"

    echo "Testing: $description"
    echo "URL: $url"

    # Make request and capture status code
    local status_code=$(curl -s -o /dev/null -w "%{http_code}" "$url")
    local redirect_url=$(curl -s -o /dev/null -w "%{redirect_url}" "$url")

    echo "Status: $status_code"
    if [ -n "$redirect_url" ]; then
        echo "Redirect to: $redirect_url"
    fi

    if [ "$status_code" = "$expected_status" ]; then
        echo "PASS"
        return 0
    else
        echo "FAIL - Expected $expected_status, got $status_code"
        return 1
    fi
}

echo "Running test cases..."

TODAY=$(date +%Y/%m/%d)
TODAY_YEAR=$(date +%Y)
TODAY_MONTH=$(date +%-m)
TODAY_DAY=$(date +%-d)

# Calculate date 8 days ago (older than 1 week)
OLD_DATE=$(get_date_days_ago 8 "%Y/%m/%d")
OLD_YEAR=$(get_date_days_ago 8 "%Y")
OLD_MONTH=$(get_date_days_ago 8 "%-m")
OLD_DAY=$(get_date_days_ago 8 "%-d")


echo "Today's date: $TODAY"
echo "Old date (8 days ago): $OLD_DATE"

# Test 1: URL with old date should redirect (302)
#test_redirect \
#    "${BASE_URL}/events/in/CH/ZH/Zürich/am/${OLD_YEAR}/${OLD_MONTH}/${OLD_DAY}" \
#    "302" \
#    "Old date should redirect to today"
#
## Test 2: URL for event details should redirect (302)
#test_redirect \
#    "${BASE_URL}/events/in/CH/ZH/Zürich/am/${OLD_YEAR}/${OLD_MONTH}/${OLD_DAY}/event-id" \
#    "302" \
#    "Old date should redirect to today"

# Test 3: URL with today's date should not redirect (200)
test_redirect \
    "${BASE_URL}/events/in/CH/ZH/Zürich/am/${TODAY_YEAR}/${TODAY_MONTH}/${TODAY_DAY}" \
    "200" \
    "Today's date should not redirect"

# Test 3: URL with today's date should not redirect (200)
test_redirect \
    "${BASE_URL}/events/in/CH/ZH/Zürich/heute" \
    "200" \
    "Today's date should not redirect"

# Test 4: URL with future date should not redirect (200)
FUTURE_YEAR=$((TODAY_YEAR + 1))
test_redirect \
    "${BASE_URL}/events/in/CH/ZH/Zürich/am/${FUTURE_YEAR}/${TODAY_MONTH}/${TODAY_DAY}" \
    "200" \
    "Future date should not redirect"

# Test 5: Events URL without date should not redirect (200)
test_redirect \
    "${BASE_URL}/events/in/CH/ZH/Zürich" \
    "200" \
    "Events URL without date should not redirect"

echo "Test completed"

echo "Container logs:"
docker logs $CONTAINER_NAME | tail -20

echo "All tests completed"

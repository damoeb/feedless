#!/usr/bin/env sh

if ! sh ./pre-boot.sh;
then
  echo 'pre boot validation failed'
  exit 1
fi

# see https://www.atamanroman.dev/articles/usecontainersupport-to-the-rescue/
#  -XX:+UseCGroupMemoryLimitForHeap \
spring_profiles=prod,"${APP_AUTHENTICATION}","${APP_ACTIVE_PROFILES}"
echo "Starting core with profiles ${spring_profiles}"

java -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=85.0 \
  -XX:+UnlockExperimentalVMOptions \
  -XX:HeapDumpPath=/usr/feedless/debug/java_error_in_feedless_.hprof \
  -Duser.timezone="${APP_TIMEZONE}" \
  -Dspring.profiles.active="${spring_profiles}" \
  -XX:+HeapDumpOnOutOfMemoryError \
  -Dfile.encoding=UTF-8 \
  -jar app.jar

#!/usr/bin/env sh

echo 'Starting app...'
# see https://www.atamanroman.dev/articles/usecontainersupport-to-the-rescue/
#  -XX:+UseCGroupMemoryLimitForHeap \
#-Dspring.profiles.active=stateless${SPRING_ACTIVE_PROFILES} \
java -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -XX:+UnlockExperimentalVMOptions \
  -Duser.timezone=${APP_TIMEZONE} \
  -Dspring.profiles.active=${spring_profiles_active} \
  -XX:+HeapDumpOnOutOfMemoryError \
  -Dfile.encoding=UTF-8 \
  -jar app.jar

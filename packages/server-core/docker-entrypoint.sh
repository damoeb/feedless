#!/usr/bin/env sh

echo 'Starting app...'
# see https://www.atamanroman.dev/articles/usecontainersupport-to-the-rescue/
#  -XX:+UseCGroupMemoryLimitForHeap \
java -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=85.0 \
  -XX:+UnlockExperimentalVMOptions \
  -Duser.timezone=${APP_TIMEZONE} \
  -Dspring.profiles.active=prod,${spring_profiles_active} \
  -XX:+HeapDumpOnOutOfMemoryError \
  -Dfile.encoding=UTF-8 \
  -jar app.jar

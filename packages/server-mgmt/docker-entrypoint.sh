echo 'Database Schema Migration'
node node_modules/.bin/prisma migrate deploy
if [ $? -eq 1 ]; then
  echo "Aborting! Migration Failed!"
  exit 1
fi
node src/main.js

wait -n
exit $?

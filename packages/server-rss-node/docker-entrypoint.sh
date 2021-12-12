echo 'Database Schema Migration'
yarn prisma migrate deploy
node src/main.js

#wait -n
#exit $?

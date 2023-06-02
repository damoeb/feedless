while ! nc -z localhost 8080; do sleep 1; done;
echo 'core is ready'

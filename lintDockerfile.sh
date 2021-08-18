echo "Linting file $1"
docker run --rm -i hadolint/hadolint < $1

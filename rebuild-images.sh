echo "Rebuilding JVM image"
docker build -f src/main/docker/Dockerfile.jvm -t masteringapi/attendees-quarkus:jvm .

echo "Rebuilding Native Image"
docker build -f src/main/docker/Dockerfile.multistage -t masteringapi/attendees-quarkus:native .

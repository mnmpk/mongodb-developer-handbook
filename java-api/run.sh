docker build . --platform=linux/amd64 -t mongodb-developer/java-api
docker run --platform=linux/amd64 -p 8080:8080 -t mongodb-developer/java-api
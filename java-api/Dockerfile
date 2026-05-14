FROM openjdk:26-ea-slim
RUN apt-get update && apt-get install -y nodejs npm
RUN npm install -g mongodb-mcp-server@latest
RUN nohup npx -y @mongodb-labs/mcp-server &
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
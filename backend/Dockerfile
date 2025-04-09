# Stage 1: Build the Maven application
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package

# Stage 2: Deploy the WAR to Tomcat
FROM tomcat:10.1-jdk17-openjdk
WORKDIR /usr/local/tomcat/webapps
COPY --from=builder /app/target/*.war ROOT.war

# Start Tomcat server
CMD ["catalina.sh", "run"]
FROM openjdk:17-jdk-alpine
EXPOSE 8081

WORKDIR .

# Copy the JAR file into the image
COPY target/*.jar app.jar

# Copy the CSV file into the image
COPY lijnsecties.csv ./lijnsecties.csv

ENTRYPOINT ["java","-jar","/app.jar"]

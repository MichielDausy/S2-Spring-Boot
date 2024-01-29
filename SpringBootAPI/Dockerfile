FROM openjdk:17-jdk-alpine
EXPOSE 8081

WORKDIR .

ADD target/*.jar app.jar

# Copy the CSV file into the image
COPY lijnsecties.csv ./lijnsecties.csv

# Copy the TXT file into the image
COPY coordinates.txt ./coordinates.txt

ENTRYPOINT ["java","-jar","/app.jar"]

FROM openjdk:8-jre
COPY target/scala-2.12/stacktome_test-assembly-1.0.jar file.jar
COPY rss/cnn.rss rss/cnn.rss
EXPOSE 9000
CMD java -Dplay.http.secret.key=stacktome -jar file.jar
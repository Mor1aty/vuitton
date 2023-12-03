FROM openjdk:21

ADD target/vuitton.jar vuitton.jar

VOLUME /vuitton.db

EXPOSE 8080

ARG DATASOURCE_URL
ENV DATASOURCE_URL $DATASOURCE_URL

ARG FILE_SERVER_IP
ENV FILE_SERVER_IP $FILE_SERVER_IP

ENTRYPOINT java -jar vuitton.jar --spring.datasource.url="$DATASOURCE_URL"
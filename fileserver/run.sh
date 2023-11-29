docker stop file-server
docker rm file-server
docker rmi file-server
docker build -t file-server .
docker run -di --name file-server -p 8081:8081 -v /data/video:/file/video -v /data/novel:/file/novel \
 --restart=always file-server
docker stop file-server
docker rm file-server
docker rmi file-server
docker build -t file-server .
REM docker run -di --name file-server -p 8081:8081 -v E:/DDiskRefuge/剧:/file/video --restart=always file-server
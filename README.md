# stacktome_test

git pull

cd stacktome_test

sbt stacktome_test/assembly

docker build -t stacktome_test .

docker run -p 9000:9000 --rm stacktome_test

http://localhost:9000
./mvnw clean install  -Dmaven.test.skip=true
mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)
docker build -t pedibus-backend .


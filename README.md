[![Quality](https://github.com/69pmb/user-api/actions/workflows/quality.yml/badge.svg)](https://github.com/69pmb/user-api/actions/workflows/quality.yml)
[![Deploy](https://github.com/69pmb/user-api/actions/workflows/deploy.yml/badge.svg)](https://github.com/69pmb/user-api/actions/workflows/deploy.yml)

# user-api

### How to run it in local:

Docker command to run a MariaDB container:  
`docker run -d --name maria_user -e MARIADB_ROOT_PASSWORD=user -e MARIADB_DATABASE=user -p 3307:3306 mariadb:10.5`  
To launch the application:  
`mvn spring-boot:run -Dspring-boot.run.profiles=local`

### Other commands:

To connect to the database:  
`docker exec -it maria_user mysql --user root -puser user`  
To launch it in production:  
`java -Dserver.port=4141 -jar -Dspring.profiles.active=dev user-api.jar`

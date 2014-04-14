# Larch repository

This project integrates ElasticSearch and WeedFs into a large scale distributed repository system modeled after Fedora 3

## Links
 * [Spring Boot](http://projects.spring.io/spring-boot/)
 * [Spring MVC](http://projects.spring.io/spring-framework/)
 * [ElasticSearch](http://www.elasticsearch.org/)
 * [WeedFs](https://code.google.com/p/weed-fs/)
 * [Facebook's Haystack paper](http://www.usenix.org/event/osdi10/tech/full_papers/Beaver.pdf)

## Settings
Settings can be found in
 * `src/main/resources/application.properties`
 * `src/main/resources/application-fs.properties`
 * `src/main/resources/application-weedfs.properties`

## Build Larch

```
#> mvn clean package
```

## Run the application using the WeedFs adapter. This will start a WeedFs master and volume server

```
#> java -jar larch-server/target/larch-server-1.0-SNAPSHOT.jar
```

## Run the application using the plain filesystem adapter

```
#> java -jar larch-server/target/larch-server-1.0-SNAPSHOT.jar --spring.profiles.active=fs
```

## Accessing the repository

Various endpoints are exposed via a REST API (see package `net.objecthunter.larch.controller` in `larch-server` for now).
A Dashboard view is exposed at the webserver root `http://localhost:8080/` and port is `8080`


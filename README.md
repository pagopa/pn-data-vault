
# Compile
```
./mvnw clean install
```

# Produce docker image
```
./mvnw clean spring-boot:build-image
```

# Execute docker image
```
docker run -p 9804:8080 pn-data-vault:0.0.1-SNAPSHOT
```

# Execute e2e tests
```
docker run -v $(pwd)/scripts/taurus:/configs blazemeter/taurus -o settings.env.BASE_URL=http://host.docker.internal:9804/  /configs/main.yaml
```






# Cryptocurrency Wallet

## Requirements

For building and running the application you need:

- [JDK 11](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
- [Maven 3](https://maven.apache.org)

## Running the application locally
##  Add config env before run application
```shell
database.host=127.0.0.1;
database.port=3307;
database.name=wallet;
database.user=root;
database.password=root;
email.username=example@gmail.com;
email.password=example
```
There are several ways to run a Spring Boot application on your local machine. One way is to execute the `main` method in the `com/tungpv/wallet/CryptocurrencyWalletApplication.java` class from your IDE.

Alternatively you can use the [Spring Boot Maven plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html) like so:

```shell
mvn spring-boot:run
```

## Copyright

[Phan Van Tung](https://github.com/tungpv-0974)
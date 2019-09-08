CryptoTrickstr
===
This demo application is a utility app for cryptocrurrency day traders. 
It provides information about the top cryptocurrencies 
(measured by total trading volume / day) and their prices on some famous cryptocurrency 
exchanges. Day traders can use it to spot an opportunity to gain more income by just 
transferring cryptocurrency between two different exchanges.

Building, testing and running the app
---
To be able to build and test the application, you will need Java 8 (JDK 8+) 
and Apache Maven 3+ installed on your machine. To build the app, please use the 
following command:
```
mvn package
```

This will build the app, run all of its tests and then package it into a standard 
`.jar` file. After a successful build, you will find the `.jar` file in 
the `target` directory. You can then run the app using the following command:
```
java -jar target/cryptotrickstr-0.0.1-SNAPSHOT.jar
```

###Database Initialization
To be able to use this application with real 
cryptocurrency data already stored in the database, you can pass the `initDb` 
parameter when starting the application. This will fetch some real data from 
CryptoCompare.com (using their API) and store the information in the database. 

#####Example:
```
java -jar target/cryptotrickstr-0.0.1-SNAPSHOT.jar --initDb=true
```

This will either *initialize or update* the information from the remote API.
The database is stored in a file on the file system (see `~/ctdb*`).

Using the App
---
This application provides a RESTful API to store and search for cryptocurrency 
statistics. By default this service can be accessed on HTTP port `8080` using 
the `coins` path (e.g. `http://localhost:8080/coins`). It exposes a RESTful resource
that provides standard CRUD functionality based on default ReST HTTP verbs 
(GET, POST, PUT, DELETE). Data is transferred in JSON format. This resource follows 
the HATEOAS principles and is self descriptive.

You can provide the following string query parameters to customize the search result:
```
http://localhost:8080/coins?page=0&size=20&sort=maxPrice,desc
```

In addition to the standard CRUD functionality it also provides search operations to
find more interesting cryptocurrencies. You can find a technical description of 
all available operations by sending a `GET` request 
to the `/coins/search` path (e.g. `http://localhost:8080/coins/search`).

##### Examples:
1. Finding all cryptocurrencies with a price that differs by at least $500 
but at most $10,000 on 2 different exchanges: 
`http://localhost:8080/coins/search/findByPriceGapBetween?from=500&to=10000`
2. The same as the first query, however, this time we use a percentage range 
instead of a USD range (5.5% - 80%): 
`http://localhost:8080/coins/search/findByPriceGapPercentBetween?from=5.5&to=80`
3. Finding a cryptocurrency by its name: 
`http://localhost:8080/coins/search/findByFullName?name=Ethereum`





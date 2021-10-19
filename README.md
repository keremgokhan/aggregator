# aggregator
Aggregates shipment, track and pricing requests

You will have to run this two docker applications:
```
docker run --name aggregator -p 6379:6379 -d redis
docker run --publish 8080:8080 xyzassessment/backend-services
```

Then you  build and run the aggregator app on 8081 port.
```
./mvnw spring-boot:run
```

You can make requests and get the response:
```
curl "http://localhost:8081/aggregation?pricing=NL,CN&track=109347263,123456891&shipments=109347263,123456891"
```

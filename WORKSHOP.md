# Create consumer
- Create quarkus skeleton for consumer with:
```
mvn io.quarkus:quarkus-maven-plugin:2.0.0.CR3:create \
-DprojectGroupId=org.acme \
-DprojectArtifactId=consumer \
-DclassName="org.acme.consumer.GreetingResource" \
-Dpath="/hello"
cd consumer
./mvnw quarkus:add-extension -Dextensions="quarkus-resteasy,quarkus-resteasy-jackson,quarkus-smallrye-openapi,rest-client,rest-client-jackson"

```
Add pact dependency to `pom.xml`
```
    <dependency>
      <groupId>au.com.dius.pact.consumer</groupId>
      <artifactId>junit5</artifactId>
      <version>4.2.5</version>
      <scope>test</scope>
    </dependency>
```
- Create Greeting.java
```java
@JsonIgnoreProperties(ignoreUnknown = true)
public class Greeting {

    @JsonProperty("greeting")
    public String greeting;
}
````
- Create GreetingService.java
```java
@ApplicationScoped
@RegisterRestClient
public interface GreetingService {

    @GET
    @Path("/hello")
    Greeting getGreeting();
}
```
- Modify GreetingResource.java
```java
@Path("/hello")
public class GreetingResource {

    @Inject
    @RestClient
    GreetingService greetingService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Greeting hello(){
        return greetingService.getGreeting();
    }
}
```
- Change src/main/resource/application.properties to:
```properties
%dev.quarkus.http.port=9090
quarkus.http.test-port=9091
org.acme.consumer.GreetingService/mp-rest/url=http://localhost:8080
mp.openapi.extensions.smallrye.info.title=Consumer API
```
- Replace src/main/resources/META-INF.resources/index.html with:
```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv='refresh' content='0; URL=/q/swagger-ui'>
</head>
```
- Remove all test from test directory
- Add GreetingServiceContractIntegrationTest.java
```java
@QuarkusTest
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "Provider")
class GreetingsServiceContractIntegrationTest {

    @Inject
    @RestClient
    GreetingService greetingService;

    @Pact(provider = "Provider", consumer = "Consumer")
    public RequestResponsePact greet(PactDslWithProvider builder) {
        return builder
                .uponReceiving("greet")
                .path("/hello")
                .method("GET")
                .willRespondWith()
                .status(200)
                .body(new PactDslJsonBody()
                        .stringType("greeting")
                )
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "greet", port = "8080")
    void testHelloEndpoint() {
        Greeting actualGreeting = greetingService.getGreeting();
        System.out.println(actualGreeting.greeting);
        Assertions.assertNotNull(actualGreeting.greeting);
    }
}
```
# Create provider
- Go back to project root directory
- Create quarkus skeleton for provider with:
```
mvn io.quarkus:quarkus-maven-plugin:2.0.0.CR3:create \
-DprojectGroupId=org.acme \
-DprojectArtifactId=provider \
-DclassName="org.acme.provider.GreetingResource" \
-Dpath="/hello"
cd provider
./mvnw quarkus:add-extension -Dextensions="quarkus-resteasy,quarkus-resteasy-jackson,quarkus-smallrye-openapi"
```
Add pact dependency to `pom.xml`
```
    <dependency>
      <groupId>au.com.dius.pact.provider</groupId>
      <artifactId>junit5</artifactId>
      <version>4.2.5</version>
      <scope>test</scope>
    </dependency>
```

- Create Greeting.java
```java
public class Greeting {

    public String greeting;

    public Greeting(String greeting) {
        this.greeting = greeting;
    }
}
````
- Modify GreetingResource.java
```java
@Path("/hello")
public class GreetingResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Greeting hello() {
        return new Greeting("Hello from your Provider!");
    }
}
```
- Change src/main/resource/application.properties to:
```properties
quarkus.http.test-port=8080
mp.openapi.extensions.smallrye.info.title=Provider API
```
- Replace src/main/resources/META-INF.resources/index.html with:
```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv='refresh' content='0; URL=/q/swagger-ui'>
</head>
```
- Remove all test from test directory
- Add GreetingResourceContractIntegrationTest.java
```java
@QuarkusTest
@Provider("Provider")
@PactFolder("../consumer/target/pacts")
class GreetingResourceContractIntegrationTest {
    
    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }
}
```




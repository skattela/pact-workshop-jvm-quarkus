package org.acme.consumer;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@ApplicationScoped
@RegisterRestClient
public interface GreetingService {

    @GET
    @Path("/hello")
    Greeting getGreeting();
}

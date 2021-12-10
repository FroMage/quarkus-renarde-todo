package rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@ClientHeaderParam(name = "User-Agent", value = "MP REST") 
@RegisterRestClient
@Path("/")
public interface GithubClient {

    @GET
    @Path("user/emails")
    public List<Email> getEmails(@HeaderParam("Authorization") String auth);

    public static class Email {
        public String email;
        public boolean primary;
        public boolean verified;
        public String visibility;
    }
}

package rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

// import io.quarkus.qute.CheckedTemplate;
// import io.quarkus.qute.TemplateInstance;

@Path("")
public class Application {

    // @CheckedTemplate
    // public static class Templates {
    //     public static native TemplateInstance index();
    //     public static native TemplateInstance about();
    // }

    // @GET
    // @Path("/")
    // public TemplateInstance index(){
    //     return Templates.index();
    // }

    // @GET
    // @Path("/about")
    // public TemplateInstance about(){
    //     return Templates.about();
    // }

    @GET
    @Path("/foo")
    public String foo(){
        return "FU STEF 222";
    }
}

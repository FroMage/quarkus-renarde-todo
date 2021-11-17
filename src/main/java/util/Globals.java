package util;

import javax.ws.rs.core.UriInfo;

import io.quarkus.arc.Arc;
import io.quarkus.qute.TemplateExtension;

@TemplateExtension(namespace = "global")
public class Globals {
    public static String requestUrl() {
        return Arc.container().instance(UriInfo.class).get().getRequestUri().toASCIIString();
    }
    public static int VARCHAR_SIZE() {
        return Util.VARCHAR_SIZE;
    }
}

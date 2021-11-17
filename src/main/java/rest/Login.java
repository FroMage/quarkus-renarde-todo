package rest;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.POST;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestForm;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.vixen.Controller;
import io.quarkus.vixen.router.Router;
import model.User;

public class Login extends Controller {
    
    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance login();
    }
    
    public TemplateInstance loginForm() {
        return Templates.login();
    }

    @POST
    public Response login(@NotBlank @RestForm String userName, 
            @NotBlank @RestForm String password) {
        if(validationFailed()) {
            loginForm();
        }
        User user = User.findRegisteredByUserName(userName);
        if(user == null
                || !BcryptUtil.matches(password, user.password)) {
            validation.addError("userName", "Invalid username/pasword");
            prepareForErrorRedirect();
            loginForm();
        }
        NewCookie cookie = Register.makeUserCookie(user);
        return Response.seeOther(Router.getURI(Application::index)).cookie(cookie).build();
    }
    
    
    public Response logout() {
        NewCookie cookie = new NewCookie("QuarkusUser", null, "/", null, null, 0, false, true);
        return Response.seeOther(Router.getURI(Application::index)).cookie(cookie).build();
    }
}

package rest;

import java.util.UUID;

import javax.inject.Inject;
import javax.persistence.PostRemove;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.BeanParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestQuery;

import email.Emails;
import io.quarkiverse.renarde.Controller;
import io.quarkiverse.renarde.oidc.RenardeSecurity;
import io.quarkiverse.renarde.router.Router;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.webauthn.WebAuthnLoginResponse;
import io.quarkus.security.webauthn.WebAuthnRegisterResponse;
import io.quarkus.security.webauthn.WebAuthnSecurity;
import io.smallrye.common.annotation.Blocking;
import io.vertx.ext.auth.webauthn.Authenticator;
import io.vertx.ext.web.RoutingContext;
import model.User;
import model.WebAuthnCredential;

@Blocking
public class Login extends Controller {

    @Inject
    RenardeSecurity security;

    @Inject
    WebAuthnSecurity webAuthnSecurity;

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance login();
        public static native TemplateInstance complete(User user);
        public static native TemplateInstance register(User user);
    }

    @Path("/login")
    public TemplateInstance login(){
        return Templates.login();
    }

    @POST
    public Response doLogin(@NotBlank @RestForm String username,
                            @RestForm String password,
                            @BeanParam WebAuthnLoginResponse webAuthn,
                            RoutingContext ctx
                            ){
        if(!webAuthn.isSet()){
            validation.required("password", password);
        }
        if(validationFailed())
            login();
        User user = User.findByUsername(username);
        if(user == null){
            validation.addError("username", "Unknown user");
        } else if(!webAuthn.isSet() && !BcryptUtil.matches(password, user.password)){
            validation.addError("username", "Unknown user");
        }
        if(validationFailed())
            login();
        if(webAuthn.isSet()){
            webAuthnSecurity.login(webAuthn, ctx).await().indefinitely();
        }
        return Response.seeOther(Router.getURI(Application::index)).cookie(security.makeUserCookie(user)).build();
    }

    @Path("/logout")
    public Response logout(){
        return security.makeLogoutResponse();
    }

    @POST
    public TemplateInstance register(@NotBlank @RestForm String email){
        if(validationFailed())
            login();
        User user = new User();
        user.email = email;
        user.confirmationCode = UUID.randomUUID().toString();
        user.persist();
        Emails.register(user);
        return Templates.register(user);
    }

    @Path("/complete")
    public TemplateInstance complete(@RestQuery String confirmationCode){
        User user = User.findByConfirmationCode(confirmationCode);
        notFoundIfNull(user);
        return Templates.complete(user);
    }

    @POST
    public Response doComplete(@RestQuery String confirmationCode,
                            @NotBlank @RestForm String username,
                            @NotBlank @RestForm String firstname,
                            @NotBlank @RestForm String lastname,
                            @RestForm String password1,
                            @RestForm String password2,
                            @BeanParam WebAuthnRegisterResponse webAuthn,
                            RoutingContext ctx
                            ){
        if(validationFailed())
            complete(confirmationCode);
        User user = User.findByConfirmationCode(confirmationCode);
        notFoundIfNull(user);
        if(!user.isOidc() && !webAuthn.isSet()){
            validation.required("password1", password1);
            validation.required("password2", password2);
            validation.equals("password1", password1, password2);
        }
        if(webAuthn.isSet()){
            Authenticator auth = webAuthnSecurity.register(webAuthn, ctx).await().indefinitely();
            WebAuthnCredential webAuthnCredential = new WebAuthnCredential(auth, user);
            webAuthnCredential.persist();
        }
        user.confirmationCode = null;
        user.username = username;
        user.firstname = firstname;
        user.lastname = lastname;
        if(!user.isOidc() && !webAuthn.isSet()){
            user.password = BcryptUtil.bcryptHash(password1);
        }
        flash("message", "Account created!");
        return Response.seeOther(Router.getURI(Application::index)).cookie(security.makeUserCookie(user)).build();
    }
}

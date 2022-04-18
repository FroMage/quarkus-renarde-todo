package rest;

import java.util.UUID;

import javax.inject.Inject;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.BeanParam;
import javax.ws.rs.POST;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;

import io.quarkiverse.renarde.Controller;
import io.quarkiverse.renarde.oidc.RenardeSecurity;
import io.quarkiverse.renarde.router.Router;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.qute.CheckedTemplate;
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

    @Inject RenardeSecurity security;

    @Inject WebAuthnSecurity webAuthnSecurity;

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance login();

        public static native TemplateInstance register(User user);
        public static native TemplateInstance confirm(User user);
    }

    public TemplateInstance login(){
        return Templates.login();
    }

    @POST
    public Response doLogin(@NotBlank @RestForm String username,
    @RestForm String password,
    @BeanParam WebAuthnLoginResponse webauthn,
    RoutingContext ctx){
        if(!webauthn.isSet()){
            validation.required("password", password);
        }
        if(validationFailed()){
            login();
        }
        User user = User.findByUsername(username);
        if(user == null || (!webauthn.isSet() && !BcryptUtil.matches(password, user.password))){
            validation.addError("username", "Invalid user");
            prepareForErrorRedirect();
            login();
        }
        if(webauthn.isSet()){
            Authenticator auth = webAuthnSecurity.login(webauthn, ctx).await().indefinitely();
            user.webAuthnCredential.counter = auth.getCounter();
        }
        NewCookie cookie = security.makeUserCookie(user);
        return Response.seeOther(Router.getURI(Application::index)).cookie(cookie).build();
    }

    public Response logout(){
        return security.makeLogoutResponse();
    }

    @POST
    public TemplateInstance register(@NotBlank @RestForm String email){
        User user = new User();
        user.email = email;
        user.confirmationCode = UUID.randomUUID().toString();
        user.persist();
        Emails.register(user);
        return Templates.register(user);
    }

    public TemplateInstance confirm(@RestQuery String confirmationCode){
        User user = User.find("confirmationCode", confirmationCode).firstResult();
        return Templates.confirm(user);
    }

    @POST
    public Response doConfirm(
        @NotBlank @RestPath String confirmationCode,
        @NotBlank @RestForm String username,
    @NotBlank @RestForm String firstname,
    @NotBlank @RestForm String lastname,
    @RestForm String password1,
    @RestForm String password2,
    @BeanParam WebAuthnRegisterResponse webauthn,
    RoutingContext ctx){
        if(validationFailed()){
            confirm(confirmationCode);
        }
        User user = User.find("confirmationCode", confirmationCode).firstResult();
        notFoundIfNull(user);

        if(!webauthn.isSet() && user.authId == null){
            validation.required("password1", password1);
            validation.required("password2", password2);
            validation.equals("password", password1, password2);
        }
        if(validationFailed()){
            confirm(confirmationCode);
        }

        user.firstname = firstname;
        user.lastname = lastname;
        user.username = username;
        if(webauthn.isSet()){
            Authenticator auth = webAuthnSecurity.register(webauthn, ctx).await().indefinitely();
            user.webAuthnCredential = new WebAuthnCredential(auth, user);
            user.webAuthnCredential.persist();
        }else if(user.authId == null){
            user.password = BcryptUtil.bcryptHash(password1);
        }
        user.confirmationCode = null;
        NewCookie cookie = security.makeUserCookie(user);
        flash("message", "Welcome!");
        return Response.seeOther(Router.getURI(Application::index)).cookie(cookie).build();
    }
}

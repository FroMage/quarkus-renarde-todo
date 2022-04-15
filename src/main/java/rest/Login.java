package rest;

import java.util.UUID;

import javax.inject.Inject;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.BeanParam;
import javax.ws.rs.POST;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;

import email.Emails;
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

        public static native TemplateInstance doRegister(String email);

        public static native TemplateInstance completeRegistration(User user);
    }

    public TemplateInstance login(){
        return Templates.login();
    }

    public Response logout(){
        flash("message", "Good bye!");
        return security.makeLogoutResponse();
    }

    @POST
    public Response doLogin(@NotBlank @RestForm String username,
                        @RestForm String password,
                        @BeanParam WebAuthnLoginResponse webauthn,
                        RoutingContext ctx){
        if(validationFailed()){
            login();
        }
        if(!webauthn.isSet()){
            validation.required("password", password);
        }
        if(validationFailed()){
            login();
        }
        User user = User.findByUsername(username);
        if(user == null){
            validation.addError("username", "Unknown user");
            prepareForErrorRedirect();
            login();
        }
        if(webauthn.isSet()){
            Authenticator auth = webAuthnSecurity.login(webauthn, ctx).await().indefinitely();
            user.webAuthnCredential.counter = auth.getCounter();
        } else if(!BcryptUtil.matches(password, user.password)){
            validation.addError("username", "Unknown user");
            prepareForErrorRedirect();
            login();
        }
        NewCookie userCookie = security.makeUserCookie(user);
        flash("message", "Welcome home!");
        return Response.seeOther(Router.getURI(Application::index)).cookie(userCookie).build();
    }

    @POST
    public TemplateInstance doRegister(@NotBlank @RestForm String email){
        if(validationFailed()){
            login();
        }
        User user = User.findByEmail(email);
        if(user != null){
            validation.addError("email", "User already exists");
            prepareForErrorRedirect();
            login();
        }
        user = new User();
        user.email = email;
        user.confirmationCode = UUID.randomUUID().toString();
        user.persist();
        Emails.verify(user);
        return Templates.doRegister(email);
    }

    public TemplateInstance completeRegistration(@RestQuery String confirmationCode){
        if(confirmationCode == null){
            flash("message", "Invalid confirmation code");
            redirect(Application.class).index();
        }
        User user = User.findByConfirmationCode(confirmationCode);
        if(user == null
           || !confirmationCode.equals(confirmationCode)){
            flash("message", "Invalid confirmation code");
            redirect(Application.class).index();
        }
        return Templates.completeRegistration(user);
    }

    @POST
    public Response complete(@NotBlank @RestPath String confirmationCode,
    @NotBlank @RestForm String firstname,
    @NotBlank @RestForm String lastname,
    @NotBlank @RestForm String username,
    @RestForm String password1,
    @RestForm String password2,
    @BeanParam WebAuthnRegisterResponse webauthn,
    RoutingContext ctx){
        if(validationFailed()){
            completeRegistration(confirmationCode);
        }
        User user = User.findByConfirmationCode(confirmationCode);
        notFoundIfNull(user);
        if(user.authId == null && !webauthn.isSet()){
            validation.required("password1", password1);
            validation.required("password2", password2);
            validation.equals("password1", password1, password2);
        }
        User otherUser = User.findByUsername(username);
        if(otherUser != null){
            validation.addError("username", "User name already taken");
        }
        if(validationFailed()){
            completeRegistration(confirmationCode);
        }
        user.username = username;
        user.firstname = firstname;
        user.lastname = lastname;
        if(webauthn.isSet()){
            Authenticator auth = webAuthnSecurity.register(webauthn, ctx).await().indefinitely();
            WebAuthnCredential creds = new WebAuthnCredential(auth, user);
            creds.persist();
        } else if(user.authId == null){
            user.password = BcryptUtil.bcryptHash(password1);
        }
        user.confirmationCode = null;
        flash("message", "Account created, welcome!");
        NewCookie userCookie = security.makeUserCookie(user);
        return Response.seeOther(Router.getURI(Application::index)).cookie(userCookie).build();
   }
}
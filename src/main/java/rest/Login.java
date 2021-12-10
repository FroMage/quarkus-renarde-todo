package rest;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.logging.Log;
import io.quarkus.oidc.AccessTokenCredential;
import io.quarkus.oidc.IdToken;
import io.quarkus.oidc.UserInfo;
import io.quarkus.oidc.runtime.OidcConfig;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.quarkus.vixen.Controller;
import io.quarkus.vixen.router.Router;
import model.User;
import model.UserStatus;
import rest.GithubClient.Email;

public class Login extends Controller {

    @CheckedTemplate
    static class Templates {
        public static native TemplateInstance login();
    }
    
    public TemplateInstance login() {
        return Templates.login();
    }

    @POST
    public Response manualLogin(@NotBlank @RestForm String userName, 
            @NotBlank @RestForm String password) {
        if(validationFailed()) {
            login();
        }
        User user = User.findRegisteredByUserName(userName);
        if(user == null
                || !BcryptUtil.matches(password, user.password)) {
            validation.addError("userName", "Invalid username/pasword");
            prepareForErrorRedirect();
            login();
        }
        NewCookie cookie = Register.makeUserCookie(user);
        return Response.seeOther(Router.getURI(Application::index)).cookie(cookie).build();
    }
        
    @Path("/login-{provider}")
    @Authenticated
    public void loginNow(@RestPath String provider) {
        // never actually called, but better be safe
        redirect(Application.class).index();
    }

    @Inject
    OidcConfig oidcConfig;

    @Path("/logout")
    public Response logout() {
        List<NewCookie> cookies = new ArrayList<>(oidcConfig.namedTenants.size() + 1);
        // Default tenant
        cookies.add(new NewCookie("q_session", null, "/", null, null, 0, false, true));
        // Named tenants
        for (String tenant : oidcConfig.namedTenants.keySet()) {
            cookies.add(new NewCookie("q_session_"+tenant, null, "/", null, null, 0, false, true));
        }
        // Manual
        cookies.add(new NewCookie("QuarkusUser", null, "/", null, null, 0, false, true));
        return Response.seeOther(Router.getURI(Application::index)).cookie(cookies.toArray(new NewCookie[0])).build();
    }

    @Inject
    AccessTokenCredential accessToken;

    @Inject
    @IdToken
    JsonWebToken idToken;

    @Inject
    UserInfo userInfo;
    
    @RestClient
    GithubClient client;

    @Transactional
    public void githubLoginSuccess() {
        // something is coming
        String authId = userInfo.getLong("id").toString();
        User user = User.findByAuthId(authId);
        if(user == null) {
            // registration
            List<Email> emails = client.getEmails("Bearer "+accessToken.getToken());
            user = new User();
            String name = userInfo.getString("name");
            int firstSpace = name.indexOf(' ');
            if(firstSpace != -1) {
                user.firstName = name.substring(0, firstSpace);
                user.lastName = name.substring(firstSpace+1);
            } else {
                user.firstName = name;
            }
            String userName = userInfo.getString("login");
            if(User.findByUserName(userName) == null) {
                user.userName = userName;
            }
            for (Email email : emails) {
                if(email.primary) {
                    user.email = email.email;
                    break;
                }
            }
            user.status = UserStatus.CONFIRMATION_REQUIRED;
            user.authId = authId;
            user.persist();
            // go to registration
            redirect(Register.class).confirmOidc(authId);
        } else if(!user.isRegistered()) {
            // user exists, but not fully registered yet
            // go to registration
            redirect(Register.class).confirmOidc(authId);
        } else {
            // login
            redirect(Application.class).index();
        }
    }

    @Transactional
    public void facebookLoginSuccess() {
        // something is coming
        String authId = userInfo.getString("id");
        User user = User.findByAuthId(authId);
        if(user == null) {
            // registration
            user = new User();
            user.firstName = userInfo.getString("first_name");
            user.lastName = userInfo.getString("last_name");
            user.email = userInfo.getString("email");
            user.status = UserStatus.CONFIRMATION_REQUIRED;
            user.authId = authId;
            user.persist();
            // go to registration
            redirect(Register.class).confirmOidc(authId);
        } else if(!user.isRegistered()) {
            // user exists, but not fully registered yet
            // go to registration
            redirect(Register.class).confirmOidc(authId);
        } else {
            // login
            redirect(Application.class).index();
        }
    }


    @Transactional
    public void oidcLoginSuccess() {
        // something is coming
        String authId = idToken.getName();
        User user = User.findByAuthId(authId);
        if(user == null) {
            // registration
            user = new User();
            user.firstName = idToken.getClaim(Claims.given_name);
            user.lastName = idToken.getClaim(Claims.family_name);
            if(user.firstName == null && user.lastName == null) {
                // MS has it in "name"
                String name = idToken.getClaim("name");
                if(name != null) {
                    int firstSpace = name.indexOf(' ');
                    if(firstSpace != -1) {
                        user.firstName = name.substring(0, firstSpace);
                        user.lastName = name.substring(firstSpace+1);
                    } else {
                        user.firstName = name;
                    }
                }                
            }
            String userName = idToken.getClaim(Claims.preferred_username);
            if(userName != null && !userName.isBlank() && User.findByUserName(userName) == null) {
                user.userName = userName;
            }
            user.email = idToken.getClaim(Claims.email);
            user.status = UserStatus.CONFIRMATION_REQUIRED;
            user.authId = authId;
            user.persist();
            // go to registration
            redirect(Register.class).confirmOidc(authId);
        } else if(!user.isRegistered()) {
            // user exists, but not fully registered yet
            // go to registration
            redirect(Register.class).confirmOidc(authId);
        } else {
            // login
            redirect(Application.class).index();
        }
    }
}

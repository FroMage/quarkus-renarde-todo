package util;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;

import io.quarkiverse.renarde.oidc.RenardeOidcHandler;
import io.quarkiverse.renarde.oidc.RenardeSecurity;
import io.quarkiverse.renarde.oidc.RenardeUser;
import io.quarkiverse.renarde.oidc.RenardeUserProvider;
import io.quarkiverse.renarde.router.Router;
import io.quarkiverse.renarde.util.Flash;
import io.quarkiverse.renarde.util.RedirectException;
import io.quarkus.security.webauthn.WebAuthnUserProvider;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.auth.webauthn.Authenticator;
import model.User;
import rest.Application;
import rest.Login;

@Blocking
@ApplicationScoped
public class MyOidcSetup implements RenardeOidcHandler, RenardeUserProvider {

    @Inject
    RenardeSecurity security;

    @Inject
    Flash flash;

    @Transactional
    @Override
    public void oidcSuccess(String tenantId, String authId) {
        User user = User.findByAuthId(tenantId, authId);
        URI uri;
        if(user == null){
            // new user
            user = new User();
            user.authId = authId;
            user.tenantId = tenantId;
            user.confirmationCode = UUID.randomUUID().toString();
            user.email = security.getOidcEmail();
            user.username = security.getOidcUserName();
            user.firstname = security.getOidcFirstName();
            user.lastname = security.getOidcLastName();
            user.persist();
            uri = Router.getURI(Login::completeRegistration, user.confirmationCode);
        } else if(!user.isRegistered()) {
            // didn't finish registration
            uri = Router.getURI(Login::completeRegistration, user.confirmationCode);
        } else {
            // regular login
            flash.flash("message", "Welcome from OIDC via "+tenantId);
            uri = Router.getURI(Application::index);
        }
        throw new RedirectException(Response.seeOther(uri).build());
    }

    @Transactional
    @Override
    public void loginWithOidcSession(String tenantId, String authId) {
        User user = User.findByAuthId(tenantId, authId);
        URI uri;
        if(user == null){
            flash.flash("message", "Invalid user");
            uri = Router.getURI(Application::index);
        } else if(!user.isRegistered()) {
            // didn't finish registration
            uri = Router.getURI(Login::completeRegistration, user.confirmationCode);
        } else {
            // regular login
            flash.flash("message", "Welcome from OIDC via "+tenantId);
            uri = Router.getURI(Application::index);
        }
        throw new RedirectException(Response.seeOther(uri).build());

    }

    @Override
    public RenardeUser findUser(String tenantId, String authId) {
        if(tenantId == null || tenantId.equals("manual"))
            return User.findByUsername(authId);
        else
            return User.findByAuthId(tenantId, authId);
    }
    
}

package util;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import io.quarkus.security.identity.SecurityIdentity;
import model.User;

@RequestScoped
public class Security {
    
    private final static User NO_USER = new User();
    
    @Inject
    SecurityIdentity identity;

    User user;
    
    @Named("user")
    @Produces
    public User getUser() {
        if(user != null) {
            // turn our null marker into null
            return user == NO_USER ? null : user;
        }
        if(!identity.isAnonymous()) {
            user = User.findRegisteredByUserName(identity.getPrincipal().getName());
            if(user == null) {
                // FIXME: error for invalid user?
                // avoid looking it up again
                user = NO_USER;
            }
        }
        return user;
    }
    
    // for initial login, since we can't arbitrarily set request-scoped values
    public void setUser(User user) {
        this.user = user;
    }
}

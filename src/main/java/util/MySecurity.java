package util;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import io.quarkiverse.renarde.oidc.RenardeSecurity;
import model.User;

@RequestScoped
public class MySecurity {
    
    // FIXME: this should be generatable
    @Inject
    RenardeSecurity security;
    
    @Named("user")
    @Produces
    public User getUser() {
        return (User) security.getUser();
    }
}

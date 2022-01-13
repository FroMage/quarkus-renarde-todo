package rest;

import javax.inject.Inject;

import io.quarkiverse.renarde.Controller;
import io.quarkiverse.renarde.oidc.RenardeSecurity;
import model.User;

public abstract class ControllerWithUser extends Controller {
    @Inject
    protected RenardeSecurity security;

    protected User getUser() {
        return (User) security.getUser();
    }
}

package rest;

import javax.inject.Inject;

import io.quarkus.vixen.Controller;
import model.User;
import util.Security;

public abstract class MyController extends Controller {
    @Inject
    protected Security security;

    protected User getUser() {
        return security.getUser();
    }
}

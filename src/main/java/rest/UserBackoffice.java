package rest;

import io.quarkiverse.renarde.backoffice.BackofficeController;
import jakarta.annotation.security.RolesAllowed;
import model.User;

@RolesAllowed("admin")
public class UserBackoffice extends BackofficeController<User> {

}

package rest;

import io.quarkiverse.renarde.backoffice.BackofficeController;
import jakarta.annotation.security.RolesAllowed;
import model.Todo;

@RolesAllowed("admin")
public class TodoBackoffice extends BackofficeController<Todo> {

}

package rest;

import io.quarkiverse.renarde.backoffice.BackofficeIndexController;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed("admin")
public class Backoffice extends BackofficeIndexController {

}

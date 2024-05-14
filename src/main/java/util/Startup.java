package util;

import java.util.Date;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.transaction.Transactional;
import model.Todo;
import model.User;
import model.UserStatus;

public class Startup {
	@io.quarkus.runtime.Startup
    @Transactional
    public void onStartup() {
        System.err.println("Adding user fromage");
        User stef = new User();
        stef.email = "fromage@example.com";
        stef.firstName = "Stef";
        stef.lastName = "Epardaud";
        stef.userName = "fromage";
        stef.password = BcryptUtil.bcryptHash("1q2w3e4r");
        stef.status = UserStatus.REGISTERED;
        stef.isAdmin = true;
        stef.persist();

        Todo todo = new Todo();
        todo.owner = stef;
        todo.task = "Buy cheese";
        todo.done = true;
        todo.doneDate = new Date();
        todo.persist();

        todo = new Todo();
        todo.owner = stef;
        todo.task = "Eat cheese";
        todo.persist();
    }
}

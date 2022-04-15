package util;

import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.transaction.Transactional;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.runtime.StartupEvent;
import model.Todo;
import model.User;

@ApplicationScoped
public class Startup {
    @Transactional
    public void startup(@Observes StartupEvent evt){
        User user = new User();
        user.username = "FroMage";
        user.password = BcryptUtil.bcryptHash("1q2w3e4r");
        user.email = "stef@epardaud.fr";
        user.firstname = "Stef";
        user.lastname = "Epardaud";
        user.persist();

        User user2 = new User();
        user2.username = "FroMage2";
        user2.password = BcryptUtil.bcryptHash("1q2w3e4r");
        user2.email = "stef@epardaud.fr";
        user2.firstname = "Stef";
        user2.lastname = "Epardaud 2";
        user2.persist();

        Todo todo = new Todo();
        todo.task = "Do Devoxx PRES";
        todo.created = new Date();
        todo.user = user;
        todo.persist();
        todo = new Todo();
        todo.task = "Delay Devoxx PRES until the last minute";
        todo.created = new Date();
        todo.closed = new Date();
        todo.done = true;
        todo.user = user;
        todo.persist();
    }
}

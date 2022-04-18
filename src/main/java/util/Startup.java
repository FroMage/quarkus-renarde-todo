package util;

import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.transaction.Transactional;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.common.annotation.Blocking;
import model.Todo;
import model.User;

@ApplicationScoped
@Blocking
public class Startup {
    @Transactional
    public void start(@Observes StartupEvent evt){
        User user = new User();
        user.firstname = "stef";
        user.lastname = "epardaud";
        user.username = "FroMage";
        user.email = "stef@epardaud.fr";
        user.password = BcryptUtil.bcryptHash("1q2w3e4r");
        user.persist();

        User user2 = new User();
        user2.firstname = "stef";
        user2.lastname = "epardaud";
        user2.username = "FroMage2";
        user2.email = "stef@epardaud.fr";
        user2.password = BcryptUtil.bcryptHash("1q2w3e4r");
        user2.persist();

        Todo todo = new Todo();
        todo.user = user;
        todo.task = "Preparer talk devoxx";
        todo.persist();

        todo = new Todo();
        todo.user = user;
        todo.task = "Venir a Paris";
        todo.done = true;
        todo.doneDate = new Date();
        todo.persist();
    }
}

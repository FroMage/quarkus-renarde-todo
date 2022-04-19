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

@Blocking
@ApplicationScoped
public class Startup {
    @Transactional
    public void start(@Observes StartupEvent evt){
        User user = new User();
        user.username = "fromage";
        user.password = BcryptUtil.bcryptHash("1q2w3e4r");
        user.email = "stef@epardaud.fr";
        user.persist();

        Todo todo = new Todo();
        todo.task = "Faire pres pour Devoxx";
        todo.user = user;
        todo.persist();

        todo = new Todo();
        todo.task = "Venir à Devoxx";
        todo.done = true;
        todo.doneDate = new Date();
        todo.user = user;
        todo.persist();
    }
}

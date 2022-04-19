package rest;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;

import io.quarkiverse.renarde.Controller;
import io.quarkiverse.renarde.oidc.ControllerWithUser;
import io.quarkus.panache.common.Sort;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import model.Todo;
import model.User;

@Authenticated
@Blocking
public class Todos extends ControllerWithUser<User> {

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance index(List<Todo> todos);
    }

    public TemplateInstance index(){
        List<Todo> todos = Todo.forUser(getUser());
        return Templates.index(todos);
    }

    @POST
    public void delete(@RestPath long id){
        Todo todo = Todo.findById(id);
        notFoundIfNull(todo);
        todo.delete();
        index();
    }

    @POST
    public void done(@RestPath long id){
        Todo todo = Todo.findById(id);
        notFoundIfNull(todo);
        todo.done = !todo.done;
        if(todo.done)
            todo.doneDate = new Date();
        else
            todo.doneDate = null;
        index();
    }

    @POST
    public void add(@NotBlank @RestForm String task){
        if(validationFailed()){
            index();
        }
        Todo todo = new Todo();
        todo.user = getUser();
        todo.task = task;
        todo.persist();
        index();
    }
}

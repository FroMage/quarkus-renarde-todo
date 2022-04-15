package rest;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.POST;

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
        List<Todo> todos = Todo.findByUser(getUser());
        return Templates.index(todos);
    }

    @POST
    public void add(@NotBlank @RestForm String task){
        if(validationFailed()){
            index();
        }
        Todo todo = new Todo();
        todo.task = task;
        todo.created = new Date();
        todo.user = getUser();
        todo.persist();
        index();
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
            todo.closed = new Date();
        else
            todo.closed = null;
        index();
    }
}

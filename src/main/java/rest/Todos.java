package rest;

import java.util.Date;
import java.util.List;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;

import io.quarkiverse.renarde.pdf.Pdf;
import io.quarkus.logging.Log;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import model.Todo;
import model.User;

@Blocking
@Authenticated
public class Todos extends HxControllerWithUser<User> {

    @CheckedTemplate
    static class Templates {
        public static native TemplateInstance index(List<Todo> todos);
        public static native TemplateInstance htmx(List<Todo> todos, String message);
        public static native TemplateInstance htmx$row(Todo todo);
        public static native TemplateInstance htmx$message(String message);
        public static native TemplateInstance htmx$error(String message);
        public static native TemplateInstance pdf(List<Todo> todos);
    }
    
    public TemplateInstance index() {
        List<Todo> todos = Todo.findByOwner(getUser());
        return Templates.index(todos);
    }

    public TemplateInstance htmx() {
        List<Todo> todos = Todo.findByOwner(getUser());
        return Templates.htmx(todos, null);
    }

    @Produces(Pdf.APPLICATION_PDF)
    public TemplateInstance pdf() {
        List<Todo> todos = Todo.findByOwner(getUser());
        return Templates.pdf(todos);
    }
    
    @POST
    public TemplateInstance delete(@RestPath Long id) {
        Todo todo = Todo.findById(id);
        notFoundIfNull(todo);
        if(todo.owner != getUser())
            notFound();
        todo.delete();
        String message = i18n.formatMessage("todos.message.deleted", todo.task);
        if (isHxRequest()) { 
        	// HTMX bug: https://github.com/bigskysoftware/htmx/issues/1043
//        	return concatTemplates(Templates.htmx$message(message), Templates.htmx$row(todo));
        	return Templates.htmx$row(todo);
        } else {
            return index();
        }
    }
    
    @POST
    public TemplateInstance done(@RestPath Long id) {
        Todo todo = Todo.findById(id);
        notFoundIfNull(todo);
        if(todo.owner != getUser())
            notFound();
        todo.done = !todo.done;
        if(todo.done)
            todo.doneDate = new Date();
        String message = i18n.formatMessage("todos.message.updated", todo.task);
        if (isHxRequest()) { 
        	// HTMX bug: https://github.com/bigskysoftware/htmx/issues/1043
//        	return concatTemplates(Templates.htmx$message(message), Templates.htmx$row(todo));
        	return Templates.htmx$row(todo);
        } else {
        	flash("message", message);
        	return index();
        }
    }

    @POST
    public TemplateInstance add(@NotBlank @RestForm String task) {
    	if (isHxRequest() && validation.hasErrors()) {
        	return Templates.htmx$error("Cannot be empty: "+task);
    	} else if(validationFailed()) {
        	index();
        }
        Todo todo = new Todo();
        todo.task = task;
        todo.owner = getUser();
        todo.persist();
        String message = i18n.formatMessage("todos.message.added", todo.task);
        if (isHxRequest()) { 
        	// HTMX bug: https://github.com/bigskysoftware/htmx/issues/1043
//        	return concatTemplates(Templates.htmx$message(message), Templates.htmx$row(todo));
        	return Templates.htmx$row(todo);
        } else {
            return index();
        }
    }
}
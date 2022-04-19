package model;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.panache.common.Sort;

@Entity
public class Todo extends PanacheEntity {
    public String task;
    public boolean done;
    public Date doneDate;
    @ManyToOne
    public User user;

    public static List<Todo> forUser(User user){
        return list("user", Sort.by("id"), user);
    }
}

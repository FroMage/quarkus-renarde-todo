package model;

import java.util.Date;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
public class Todo extends PanacheEntity {

    @ManyToOne
    public User owner;
    
	public String task;
	
	public boolean done;
	
	public Date doneDate;

    public static List<Todo> findByOwner(User user) {
        return find("owner = ?1 ORDER BY id", user).list();
    }
}

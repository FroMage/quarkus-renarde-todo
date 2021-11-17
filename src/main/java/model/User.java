package model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
@Table(name = "user_table")
public class User extends PanacheEntity {
	
	@Column(nullable = false)
	public String email;
	@Column(unique = true)
	public String userName;
	public String password;
	public String firstName;
	public String lastName;
	public boolean isAdmin;
	
	@Column(unique = true)
	public String confirmationCode;
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	public UserStatus status;
	
	@Transient
	public boolean isRegistered(){
	    return status == UserStatus.REGISTERED;
	}

	//
	// Helpers

	public static User findUnconfirmedByEmail(String email) {
		return find("LOWER(email) = ?1 AND status = ?2", email.toLowerCase(), UserStatus.CONFIRMATION_REQUIRED).firstResult();
	}

	public static User findRegisteredByUserName(String username) {
        return find("LOWER(userName) = ?1 AND status = ?2", username.toLowerCase(), UserStatus.REGISTERED).firstResult();
    }

    public static User findByUserName(String username) {
        return find("LOWER(userName) = ?1", username.toLowerCase()).firstResult();
    }
    
    public static User findByUserNameAndConfirmationCode(String username, String confirmationCode) {
        return find("LOWER(userName) = ?1 AND confirmationCode = ?2", username.toLowerCase(), confirmationCode).firstResult();
    }

	public static List<User> registeredUsers() {
		return find("status", UserStatus.REGISTERED).list();
	}

	public static long countRegisteredUsers() {
		return count("status", UserStatus.REGISTERED);
	}

    public static User findForContirmation(String confirmationCode) {
        return find("confirmationCode = ?1 AND status = ?2", confirmationCode, UserStatus.CONFIRMATION_REQUIRED).firstResult();
    }
}
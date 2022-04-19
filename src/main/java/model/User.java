package model;

import java.util.Collections;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import io.quarkiverse.renarde.oidc.RenardeUser;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Table(name = "user_table")
@Entity
public class User extends PanacheEntity implements RenardeUser {

    public String username;
    public String password;
    public String email;
    public String confirmationCode;

    @Override
    public Set<String> getRoles() {
        return Collections.emptySet();
    }

    @Override
    public String getUserId() {
        return username;
    }

    @Override
    public boolean isRegistered() {
        return confirmationCode == null;
    }

    public static User findByUsername(String username) {
        return find("username", username).firstResult();
    }

    public static User findByConfirmationCode(String confirmationCode) {
        return find("confirmationCode", confirmationCode).firstResult();
    }
    
}

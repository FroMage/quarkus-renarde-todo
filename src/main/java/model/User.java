package model;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import io.quarkiverse.renarde.oidc.RenardeUser;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Table(name = "user_table")
@Entity
public class User extends PanacheEntity implements RenardeUser {
    public String username;
    public String email;
    public String firstname;
    public String lastname;
    public String confirmationCode;
    public String password;
    public String authId;
    public String tenantId;
    @OneToOne(mappedBy = "user")
    public WebAuthnCredential webAuthnCredential;

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
    public static User findByAuthId(String tenantId, String authId) {
        return find("tenantId = ?1 and authId = ?2", tenantId, authId).firstResult();
    }
}

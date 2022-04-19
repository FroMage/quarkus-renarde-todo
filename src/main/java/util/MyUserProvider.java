package util;

import javax.enterprise.context.ApplicationScoped;

import io.quarkiverse.renarde.oidc.RenardeUser;
import io.quarkiverse.renarde.oidc.RenardeUserProvider;
import io.smallrye.common.annotation.Blocking;
import model.User;

@Blocking
@ApplicationScoped
public class MyUserProvider implements RenardeUserProvider {

    @Override
    public RenardeUser findUser(String tenantId, String authId) {
        return User.findByUsername(authId);
    }
    
}

package util;

import java.util.Map.Entry;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.quarkus.oidc.OidcTenantConfig;
import io.quarkus.oidc.TenantResolver;
import io.quarkus.oidc.runtime.OidcConfig;
import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;

@ApplicationScoped
public class OidcTenantResolver implements TenantResolver {
    
    @Inject
    OidcConfig oidcConfig;

    @Override
    public String resolve(RoutingContext context) {
        // Named tenants
        for (Entry<String, OidcTenantConfig> tenantEntry : oidcConfig.namedTenants.entrySet()) {
            if(!tenantEntry.getValue().tenantEnabled)
                continue;
            String tenant = tenantEntry.getKey();
            // First case: login
            // Note that Router.getURI only works in JAX-RS endpoints
            if(context.request().path().equals("/login-"+tenant)) {
                return tenant;
            }

            // Second case: auth callback from the auth server
            Cookie cookie = context.request().getCookie("q_auth_"+tenant);
            if(cookie != null) {
                return tenant;
            }

            // Third case: already logged in
            cookie = context.request().getCookie("q_session_"+tenant);
            if(cookie != null) {
                return tenant;
            }
        }
        
        // manual JWT session
        Cookie cookie = context.request().getCookie("QuarkusUser");
        if(cookie != null) {
            return "manual";
        }

        // Not logged in or default tenant
        return null;
    }
}

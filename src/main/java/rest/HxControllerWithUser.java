package rest;

import java.util.Arrays;
import java.util.Objects;

import io.quarkiverse.renarde.security.ControllerWithUser;
import io.quarkiverse.renarde.security.RenardeUser;
import io.quarkus.qute.Qute;
import io.quarkus.qute.TemplateInstance;
import io.vertx.core.http.HttpServerResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

public abstract class HxControllerWithUser<T extends RenardeUser> extends ControllerWithUser<T> {
	    public static final String HX_REQUEST_HEADER = "HX-Request";

	    public enum HxResponseHeader {
	        TRIGGER("HX-Trigger"), // Allows you to trigger client side events
	        REDIRECT("HX-Redirect"), // Can be used to do a client-side redirect to a new location
	        LOCATION("HX-Location"), // Allows you to do a client-side redirect that does not do a full page reload
	        REFRESH("HX-Refresh"), // If set to “true” the client side will do a a full refresh of the page
	        PUSH_URL("HX-Push-Url"), // Replaces the current URL in the location bar
	        HX_RESWAP("HX-Reswap"), // Allows you to specify how the response will be swapped. See hx-swap for possible values
	        HX_RETARGET("HX-Retarget"), // A CSS selector that updates the target of the content update to a different element on the page
	        TRIGGER_AFTER_SWAP("HX-Trigger-After-Swap"), // allows you to trigger client side events
	        TRIGGER_AFTER_SETTLE("HX-Trigger-After-Settle"); // allows you to trigger client side events

	        private final String key;

	        HxResponseHeader(String key) {
	            this.key = key;
	        }

	        public String key() {
	            return key;
	        }
	    }

	    @Inject
	    protected HttpHeaders httpHeaders;

	    @Inject
	    protected HttpServerResponse response;

	    /**
	     * This Qute helper make it easy to achieve htmx "Out of Band" swap by choosing which templates to return (refresh).
	     * <br />
	     * {@see <a href="https://htmx.org/attributes/hx-swap-oob/">Doc for htmx "hx-swap-oob"</a>}
	     *
	     * <br />
	     *
	     * @param templates the list of template instances to concatenate
	     * @return the concatenated templates instances
	     */
	    public static TemplateInstance concatTemplates(TemplateInstance... templates) {
	        return Qute.fmt("{#each elements}{it.raw}{/each}")
	                .cache()
	                .data("elements", Arrays.stream(templates).map(TemplateInstance::createUni))
	                .instance();
	    }

	    /**
	     * Check if this request has the htmx flag (header or flash data)
	     */
	    protected boolean isHxRequest() {
	        final boolean hxRequest = Objects.equals(flash.get(HX_REQUEST_HEADER), true);
	        if (hxRequest) {
	            return true;
	        }
	        return Objects.equals(httpHeaders.getHeaderString(HX_REQUEST_HEADER), "true");
	    }

	    /**
	     * Helper to define htmx response headers.
	     *
	     * @param hxHeader the {@link HxResponseHeader} to define
	     * @param value the value for this header
	     */
	    protected void hx(HxResponseHeader hxHeader, String value) {
	        response.headers().set(hxHeader.key(), value);
	    }

	    /**
	     * Make sure only htmx requests are calling this method.
	     */
	    protected void onlyHxRequest() {
	        if (!isHxRequest()) {
	            throw new WebApplicationException(
	                    Response.status(Response.Status.BAD_REQUEST).entity("Only Hx request are allowed on this method").build());
	        }
	    }

	    /**
	     * Keep the htmx flag for the redirect request.
	     * This is automatic.
	     */
	    protected void flashHxRequest() {
	        flash(HX_REQUEST_HEADER, isHxRequest());
	    }

	    @Override
	    protected void beforeRedirect() {
	        flashHxRequest();
	        super.beforeRedirect();
	    }

}

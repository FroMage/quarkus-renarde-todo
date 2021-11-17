package rest;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.POST;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.hibernate.validator.constraints.Length;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestQuery;

import email.Emails;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.vixen.util.StringUtils;
import io.smallrye.jwt.build.Jwt;
import model.User;
import model.UserStatus;
import util.Util;

public class Register extends MyController {

    @CheckedTemplate
    static class Templates {
        public native static TemplateInstance index();
        public static native TemplateInstance register(User newUser);
        public static native TemplateInstance confirm(User newUser);
        public static native TemplateInstance logoutFirst();
        public static native TemplateInstance complete(User user);
    }
    
	public TemplateInstance index(){
		return Templates.index();
	}
	
    @POST
    public TemplateInstance register(@RestForm @NotBlank @Length(max = Util.VARCHAR_SIZE) @Email String email) {
    	if(validationFailed())
    		index();
    	User newUser = User.findUnconfirmedByEmail(email);
    	if(newUser == null){
    		newUser = new User();
    		newUser.email = email;
    		newUser.confirmationCode = UUID.randomUUID().toString();
    		newUser.status = UserStatus.CONFIRMATION_REQUIRED;
    		newUser.persist();
    	}
    	Emails.confirm(newUser);
    	return Templates.register(newUser);
    }

    public TemplateInstance confirm(@RestQuery String confirmationCode){
        checkLogoutFirst();
		User newUser = checkConfirmationCode(confirmationCode);
    	return Templates.confirm(newUser);
    }

    private void checkLogoutFirst() {
        if(getUser() != null) {
            logoutFirst();
        }
    }

    public TemplateInstance logoutFirst() {
        return Templates.logoutFirst();
    }

    private User checkConfirmationCode(String confirmationCode) {
		if(StringUtils.isEmpty(confirmationCode)){
			validation.addError("confirmationCode", "Missing confirmation code");
			prepareForErrorRedirect();
	        redirect(Application.class).index();
		}
		User user = User.findForContirmation(confirmationCode);
    	if(user == null){
    		validation.addError("confirmationCode", "Invalid confirmation code");
    		prepareForErrorRedirect();
            redirect(Application.class).index();
    	}
		return user;
	}

    @POST
	public Response complete(@RestQuery String confirmationCode, 
    		@RestForm @NotBlank @Length(max = Util.VARCHAR_SIZE) String userName, 
    		@RestForm @NotBlank @Length(min = 8, max = Util.VARCHAR_SIZE) String password, 
    		@RestForm @NotBlank @Length(max = Util.VARCHAR_SIZE) String password2, 
    		@RestForm @NotBlank @Length(max = Util.VARCHAR_SIZE) String firstName, 
    		@RestForm @NotBlank @Length(max = Util.VARCHAR_SIZE) String lastName) {
        checkLogoutFirst();
        User user = checkConfirmationCode(confirmationCode);
        
        if(validationFailed())
			confirm(confirmationCode);
		
        validation.equals("password", password, password2);
        
		if(User.findByUserName(userName) != null)
			validation.addError("userName", "User name already taken");
		if(validationFailed())
		    confirm(confirmationCode);
		
    	user.userName = userName;
    	user.password = BcryptUtil.bcryptHash(password);
    	user.firstName = firstName;
    	user.lastName = lastName;
    	user.confirmationCode = null;
    	user.status = UserStatus.REGISTERED;
    	user.persist();
    	
    	NewCookie cookie = makeUserCookie(user);
        security.setUser(user);
    	return Response.ok(Templates.complete(user)).cookie(cookie).build();
    }

	static NewCookie makeUserCookie(User user) {
	    Set<String> roles = new HashSet<>();
	    if(user.isAdmin) {
	        roles.add("admin");
	    }
	    String token =
	            Jwt.issuer("https://example.com/issuer") 
	            .upn(user.userName) 
	             .groups(roles)
	             .expiresIn(Duration.ofDays(10))
	           .innerSign().encrypt();
	    // FIXME: expiry, auto-refresh?
	    return new NewCookie("QuarkusUser", token, "/", null, Cookie.DEFAULT_VERSION, null, NewCookie.DEFAULT_MAX_AGE, null, false, false);
	}

}

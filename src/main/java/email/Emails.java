package email;

import javax.validation.constraints.NotBlank;

import io.quarkus.mailer.MailTemplate.MailTemplateInstance;
import io.quarkus.qute.CheckedTemplate;
import model.User;

public class Emails {

    @CheckedTemplate
    public static class Templates {
        public static native MailTemplateInstance verify(User user);
    }

    public static void verify(User user) {
        Templates.verify(user).from("todo@example.com").to(user.email).send().await().indefinitely();
    }

}

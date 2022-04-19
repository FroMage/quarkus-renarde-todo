package email;

import io.quarkus.mailer.MailTemplate.MailTemplateInstance;
import io.quarkus.qute.CheckedTemplate;
import model.User;

public class Emails {
    @CheckedTemplate
    public static class Templates {
        public static native MailTemplateInstance register(User user);
    }

    public static void register(User user){
        Templates.register(user).from("stef@example.com").to(user.email).send().await().indefinitely();
    }
}

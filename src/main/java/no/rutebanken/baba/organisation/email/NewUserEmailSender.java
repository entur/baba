/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 */

package no.rutebanken.baba.organisation.email;

import java.util.Locale;
import no.rutebanken.baba.organisation.model.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class NewUserEmailSender {

  private static final Logger LOGGER = LoggerFactory.getLogger(NewUserEmailSender.class);
  private final JavaMailSender mailSender;
  private final NewUserEmailFormatter newUserEmailFormatter;
  private final String emailFrom;
  private final String emailLanguageDefault;
  private final boolean sendEmailEnabled;

  public NewUserEmailSender(
    JavaMailSender mailSender,
    NewUserEmailFormatter newUserEmailFormatter,
    @Value("${new.user.email.from:noreply@entur.org}") String emailFrom,
    @Value("${new.user.email.language.default:no}") String emailLanguageDefault,
    @Value("${new.user.email.enabled:true}") boolean sendEmailEnabled
  ) {
    this.mailSender = mailSender;
    this.newUserEmailFormatter = newUserEmailFormatter;
    this.emailFrom = emailFrom;
    this.emailLanguageDefault = emailLanguageDefault;
    this.sendEmailEnabled = sendEmailEnabled;
  }

  public void sendEmail(User user) {
    Locale locale = new Locale.Builder().setLanguage(emailLanguageDefault).build();
    sendEmail(
      user.getContactDetails().getEmail(),
      newUserEmailFormatter.getSubject(locale),
      newUserEmailFormatter.formatMessage(user, locale)
    );
  }

  protected void sendEmail(String to, String subject, String msg) {
    if (sendEmailEnabled) {
      mailSender.send(mimeMessage -> {
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setText(msg, true);
        helper.setSubject(subject);
        helper.setTo(to);
        helper.setFrom(emailFrom);
      });
      LOGGER.info("Sent email with account information to: {}", to);
    } else {
      LOGGER.info("Not sending email to new user: {} as this has been disabled", to);
    }
  }
}

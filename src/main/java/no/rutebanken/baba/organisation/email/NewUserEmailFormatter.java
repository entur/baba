package no.rutebanken.baba.organisation.email;

import java.util.Locale;
import no.rutebanken.baba.organisation.model.user.User;

public interface NewUserEmailFormatter {
  String getSubject(Locale locale);

  String formatMessage(User user, Locale locale);
}

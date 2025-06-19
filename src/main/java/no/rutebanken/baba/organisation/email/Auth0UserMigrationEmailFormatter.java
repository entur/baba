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

import freemarker.template.Configuration;
import no.rutebanken.baba.exceptions.BabaException;
import no.rutebanken.baba.organisation.model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Profile("migration")
@Service
public class Auth0UserMigrationEmailFormatter implements NewUserEmailFormatter {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private Configuration freemarkerConfiguration;

    @Value("${email.link.user.guide:https://enturas.atlassian.net/wiki/spaces/PUBLIC/pages/1444216931/Using+our+services}")
    private String userGuideLink;


    @Value("${email.contact.info:kollektivdata@entur.org}")
    private String contactInfoEmail;


    @Override
    public String getSubject(Locale locale) {
        return messageSource.getMessage("new.user.email.subject", new Object[]{}, locale);
    }

    @Override
    public String formatMessage(User user, Locale locale) {
        Map<String, Object> model = new HashMap<>();

        model.put("user", user);
        model.put("message", new MessageResolverMethod(messageSource, locale));

        model.put("contactInfoEmail", contactInfoEmail);
        model.put("userGuideLink", userGuideLink);

        return geFreeMarkerTemplateContent(model);
    }


    private String geFreeMarkerTemplateContent(Map<String, Object> model) {
        try {
            return FreeMarkerTemplateUtils.processTemplateIntoString(
                    freemarkerConfiguration.getTemplate("fm_email_new_user_template_auth0_migration.ftl"), model);
        } catch (Exception e) {
            throw new BabaException("Exception occurred while processing email template:" + e.getMessage(), e);
        }

    }
}

/* 
* Copyright 2014 Frank Asseg
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License. 
*/
package net.objecthunter.larch.service.impl;

import net.objecthunter.larch.model.security.UserRequest;
import net.objecthunter.larch.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Properties;


public class DefaultMailService implements MailService {
    private static final Logger log = LoggerFactory.getLogger(DefaultMailService.class);

    private InternetAddress mailFrom;
    private String smtpHost;
    private boolean enabled;

    @Autowired
    private Environment env;

    @PostConstruct
    public void init() {
        if (env.getProperty("larch.mail.enabled") != null &&
                env.getProperty("larch.mail.enabled").equalsIgnoreCase("false")){
            /* mail sending disabled */
            return;
        }
        try {
            final String value = env.getProperty("larch.mail.from");
            if (value.isEmpty()) {
                throw new IllegalArgumentException("The property larch.mail.from cannot be blank");
            }
            mailFrom = new InternetAddress(value);
            smtpHost = env.getProperty("larch.mail.smtp.host", "localhost");
            enabled = Boolean.parseBoolean(env.getProperty("larch.mail.enabled","false"));
        } catch (AddressException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void sendUserRequest(UserRequest req) throws IOException {
        if (!enabled) {
            throw new IOException("Mail service is disabled unable to comply with request");
        }
        final Properties props = System.getProperties();
        props.setProperty("mail.smtp.host", smtpHost);
        final Session sess = Session.getDefaultInstance(props);
        MimeMessage msg = new MimeMessage(sess);
        try {
            msg.setFrom(mailFrom);
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(req.getUser().getEmail()));
            msg.setSubject("User confirmation for larch repository");
            msg.setText("Please open the following link to confirm your account creation:\n" +
                    "http://localhost:8080/confirm?token=" + req.getToken());
            Transport.send(msg);
            log.info("Sent confirmation email to " + req.getUser().getEmail());
        } catch (MessagingException e) {
            throw new IOException(e);
        }
    }
}

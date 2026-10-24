package com.example.demo.email;

import com.example.demo.user.User;
import com.example.demo.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Trimite un email catre utilizatorii selectati (cerinta "sendEmail to all selected persons").
 * Daca spring.mail.host nu este configurat, mesajele sunt doar scrise in log,
 * ca sa se poata testa endpoint-ul fara un server SMTP.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final UserService userService;
    private final String from;
    private final String mailHost;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider,
                        UserService userService,
                        @Value("${app.mail.from:no-reply@vacationquester.local}") String from,
                        @Value("${spring.mail.host:}") String mailHost) {
        this.mailSenderProvider = mailSenderProvider;
        this.userService = userService;
        this.from = from;
        this.mailHost = mailHost;
    }

    /** Trimite mesajul si returneaza numarul de destinatari. */
    public int send(EmailRequest request) {
        if (request.getSubject() == null || request.getSubject().isBlank()
                || request.getMessage() == null || request.getMessage().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "subject and message are required");
        }
        List<User> recipients = userService.requireByIds(request.getUserIds());

        JavaMailSender mailSender = (mailHost == null || mailHost.isBlank())
                ? null
                : mailSenderProvider.getIfAvailable();

        for (User recipient : recipients) {
            if (mailSender == null) {
                log.info("[EMAIL] to {} <{}> | {} | {}",
                        recipient.getName(), recipient.getEmail(), request.getSubject(), request.getMessage());
                continue;
            }
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(from);
            mail.setTo(recipient.getEmail());
            mail.setSubject(request.getSubject());
            mail.setText(request.getMessage());
            mailSender.send(mail);
        }
        return recipients.size();
    }
}

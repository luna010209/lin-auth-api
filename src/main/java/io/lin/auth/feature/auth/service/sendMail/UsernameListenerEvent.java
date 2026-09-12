package io.lin.auth.feature.auth.service.sendMail;

import io.lin.auth.exception.CustomException;
import io.lin.auth.utils.i18n.I18nUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.io.UnsupportedEncodingException;

@RequiredArgsConstructor
@Component
public class UsernameListenerEvent implements ApplicationListener<UsernameListener> {

    private final JavaMailSender mailSender;
    private final I18nUtil languageUtil;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void onApplicationEvent(UsernameListener event) {
        try {
            String subject = languageUtil.m("mail.subject.username");
            String senderName = "Lingdingdong";

            String toEmail = event.getEmail();

            String safeUsername = HtmlUtils.htmlEscape(event.getUsername());

            String title = languageUtil.m("mail.content.username.title");
            String paragraph = languageUtil.m("mail.content.username.paragraph");
            String note = languageUtil.m("mail.content.username.note");
            String footer = languageUtil.m("mail.content.footer");

            String mailContent = String.format("""
                    <div style="margin:0; padding:0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif; background-color: #F5F7FF;">
                      <table align="center" width="100%%" cellpadding="0" cellspacing="0" style="padding: 40px 0;">
                        <tr>
                          <td align="center">
                            <table width="450" cellpadding="0" cellspacing="0"
                              style="background-color: #FFFFFF; border-radius: 16px; overflow: hidden; box-shadow: 0 6px 20px rgba(0,0,0,0.06);">
                    
                              <!-- Header (Logo) -->
                              <tr>
                                <td style="padding:0; margin:0; line-height:0; font-size:0; border-bottom: 2px solid #4F46E5;
                                      box-shadow: 0 4px 12px rgba(79, 70, 229, 0.2);">
                                  <img src="cid:logoImage"
                                    alt="Lin Langa Logo"
                                    width="450"
                                    style="display:block; width:100%%; max-width:450px; height:auto; border:0; margin:0; padding:0;" />
                                </td>
                              </tr>
                    
                              <!-- Content -->
                              <tr>
                                <td style="padding: 36px 28px;">
                                  <h2 style="margin: 0 0 16px; font-size: 20px; color: #111827; font-weight: 600;">
                                    %s
                                  </h2>
                    
                                  <p style="margin: 0 0 20px; font-size: 14px; color: #4B5563; line-height: 1.6;">
                                    %s
                                  </p>
                    
                                  <!-- Highlight Box (Username / Code) -->
                                  <div style="
                                      background-color: #FFF7ED;
                                      border: 2px solid #F59E0B;
                                      padding: 16px 0;
                                      text-align: center;
                                      font-size: 26px;
                                      font-weight: 700;
                                      color: #F59E0B;
                                      border-radius: 12px;
                                      width: 200px;
                                      margin: 26px auto;
                                      letter-spacing: 3px;
                                  ">
                                    %s
                                  </div>
                    
                                  <p style="font-size: 13px; color: #6B7280; line-height: 1.7; margin-top: 20px;">
                                    %s
                                  </p>
                                </td>
                              </tr>
                    
                              <!-- Footer -->
                              <tr>
                                <td style="padding: 20px 28px; background-color: #F3F4F6; font-size: 11px; color: #6B7280; line-height: 1.6;">
                                  %s
                                </td>
                              </tr>
                    
                            </table>
                          </td>
                        </tr>
                      </table>
                    </div>
                    """, title, paragraph, safeUsername, note, footer);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, senderName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(mailContent, true);

            helper.addInline("logoImage", new ClassPathResource("static/images/logo.png"));

            mailSender.send(message);

        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "mail.error.username.send_failed");
        }
    }
}
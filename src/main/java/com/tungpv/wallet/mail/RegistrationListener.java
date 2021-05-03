package com.tungpv.wallet.mail;

import com.tungpv.wallet.entity.User;
import com.tungpv.wallet.service.UserService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.UUID;

@Component
public class RegistrationListener implements
        ApplicationListener<OnRegistrationCompleteEvent> {

    @Autowired
    private UserService service;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${support.verifyEmailUrl}")
    private String verifyEmailUrl;

    @SneakyThrows
    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }

    private void confirmRegistration(OnRegistrationCompleteEvent event) throws MessagingException {
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        service.createVerificationToken(user, token);

        String recipientAddress = user.getEmail();
        String subject = "Registration Confirmation";
        String confirmationUrl = verifyEmailUrl + "?token=" + token;
//        String message = "Click the link to activate your account:";

//        SimpleMailMessage email = new SimpleMailMessage();
//        email.setTo(recipientAddress);
//        email.setSubject(subject);
//        email.setText("<!DOCTYPE html><html><head><title>Page Title</title><style>.wrapper{background-color:#fff;border-radius:4px;max-width:550px;width:100%;margin:100px 0;padding:30px 50px;box-sizing:border-box}.wrapper-td{padding:0 10px}.logo-wrap{text-align:center;padding:16px 0;margin-bottom:80px}.text-body{line-height:25px}.text-wrap{text-align:center;margin:40px 0}.text-welcome{font-weight:bold;font-size:18px;margin-bottom:30px}.activation-link{display:inline-block;padding:16px 50px;border-radius:50px;background-color:#877BED;text-transform:uppercase;color:#fff;text-decoration:none;margin:50px 0 60px}.link-copy{opacity: .6;font-size:14px;line-height:24px}</style><body><table width=\"100%\" style=\"background-color:#eee\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr><td align=\"center\" class=\"wrapper-td\"><div class=\"text-wrap\"><div class=\"text-welcome\">Bitcoin Wallet KMA</div><p class=\"text-body\">Bạn đã tiến hành đăng ký tài khoản trên hệ thống Bitcoin Wallet KMA. <br> Nhấn vào link phía dưới để tiến hành kích hoạt tài khoản:</p> <a href=\"" + confirmationUrl + "\" class=\"activation-link\">XÁC NHẬN TÀI KHOẢN</a><div class=\"link-copy\"><div>Liên hệ trợ giúp:</div> <a href=\"https://www.facebook.com/tungpv98/\">@TungPhan</a></div><div></div></div></div></td></tr></table></body></html>");
//        email.setText(message + "\r\n"  + confirmationUrl);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        String htmlMsg = "<!DOCTYPE html><html><head><title>Page Title</title><style>.wrapper{background-color:#fff;border-radius:4px;max-width:550px;width:100%;margin:100px 0;padding:30px 50px;box-sizing:border-box}.wrapper-td{padding:0 10px}.logo-wrap{text-align:center;padding:16px 0;margin-bottom:80px}.text-body{line-height:25px}.text-wrap{text-align:center;margin:40px 0}.text-welcome{font-weight:bold;font-size:18px;margin-bottom:30px}.activation-link{display:inline-block;padding:16px 50px;border-radius:50px;background-color:#877BED;text-transform:uppercase;color:#fff;text-decoration:none;margin:50px 0 60px}.link-copy{opacity: .6;font-size:14px;line-height:24px}</style><body><table width=\"100%\" style=\"background-color:#eee\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr><td align=\"center\" class=\"wrapper-td\"><div class=\"text-wrap\"><div class=\"text-welcome\">Bitcoin Wallet KMA</div><p class=\"text-body\">Bạn đã tiến hành đăng ký tài khoản trên hệ thống Bitcoin Wallet KMA. <br> Nhấn vào link phía dưới để tiến hành kích hoạt tài khoản:</p> <a href=\"" + confirmationUrl + "\" class=\"activation-link\">XÁC NHẬN TÀI KHOẢN</a><div class=\"link-copy\"><div>Liên hệ trợ giúp:</div> <a href=\"https://www.facebook.com/tungpv98/\">@TungPhan</a></div><div></div></div></div></td></tr></table></body></html>";
        mimeMessage.setContent(htmlMsg, "text/html"); /** Use this or below line **/
        helper.setText(htmlMsg, true); // Use this or above line.
        helper.setTo(recipientAddress);
        helper.setSubject(subject);
        helper.setFrom("noreply@email.kma.com");
        mailSender.send(mimeMessage);

//        mailSender.send(email);
    }
}

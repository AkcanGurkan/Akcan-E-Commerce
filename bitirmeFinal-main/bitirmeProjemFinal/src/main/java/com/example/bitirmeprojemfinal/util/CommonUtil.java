package com.example.bitirmeprojemfinal.util;


import com.example.bitirmeprojemfinal.entity.ProductOrder;
import com.example.bitirmeprojemfinal.entity.UserDtls;
import com.example.bitirmeprojemfinal.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.security.Principal;

@Component
public class CommonUtil {

	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private UserService userService;

	public Boolean sendMail(String url, String reciepentEmail) throws UnsupportedEncodingException, MessagingException {

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom("gurkansubasi13@gmail.com", "Akcan");
		helper.setTo(reciepentEmail);

		String content = "<p>Merhaba,</p>" + "<p>Şifrenizi sıfırlamak için bir talepte bulundunuz.</p>"
				+ "<p>Şifrenizi değiştirmek için aşağıdaki bağlantıya tıklayın:</p>" + "<p><a href=\"" + url
				+ "\">Şifremi Değiştir</a></p>";
		helper.setSubject("Password Reset");
		helper.setText(content, true);
		mailSender.send(message);
		return true;
	}

	public static String generateUrl(HttpServletRequest request) {

		String siteUrl = request.getRequestURL().toString();

		return siteUrl.replace(request.getServletPath(), "");
	}
	
	String msg=null;;
	
	public Boolean sendMailForProductOrder(ProductOrder order, String status) throws Exception
	{
		msg = """
			<!DOCTYPE html>
			<html>
			<head>
				<meta charset="UTF-8">
				<style>
					body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
					.container { max-width: 600px; margin: 0 auto; padding: 20px; }
					.header { background: linear-gradient(135deg, #2c3e50 0%, #3498db 100%); color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }
					.content { background: #fff; padding: 20px; border: 1px solid #e9ecef; border-radius: 0 0 10px 10px; }
					.status { font-size: 18px; font-weight: bold; color: #2c3e50; margin: 20px 0; }
					.product-details { background: #f8fafc; padding: 20px; border-radius: 10px; margin: 20px 0; }
					.footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
				</style>
			</head>
			<body>
				<div class="container">
					<div class="header">
						<h2>Sipariş Durumu Güncellendi</h2>
					</div>
					<div class="content">
						<p>Merhaba [[name]],</p>
						<div class="status">
							Siparişinizin Durumu: <span style="color: #3498db;">[[orderStatus]]</span>
						</div>
						<div class="product-details">
							<h3>Ürün Detayları:</h3>
							<p><strong>Ürün Adı:</strong> [[productName]]</p>
							<p><strong>Kategori:</strong> [[category]]</p>
							<p><strong>Miktar:</strong> [[quantity]]</p>
							<p><strong>Fiyat:</strong> ₺[[price]]</p>
							<p><strong>Ödeme Türü:</strong> [[paymentType]]</p>
						</div>
						<p>Siparişiniz için teşekkür ederiz.</p>
					</div>
					<div class="footer">
						<p>© 2024 Akcan. Tüm hakları saklıdır.</p>
					</div>
				</div>
			</body>
			</html>
		""";
		
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom("gurkansubasi13@gmail.com", "Akcan");
		helper.setTo(order.getOrderAddress().getEmail());

		msg=msg.replace("[[name]]",order.getOrderAddress().getFirstName());
		msg=msg.replace("[[orderStatus]]",status);
		msg=msg.replace("[[productName]]", order.getProduct().getTitle());
		msg=msg.replace("[[category]]", order.getProduct().getCategory());
		msg=msg.replace("[[quantity]]", order.getQuantity().toString());
		msg=msg.replace("[[price]]", String.format("%,.2f", order.getPrice()));
		msg=msg.replace("[[paymentType]]", order.getPaymentType());
		
		helper.setSubject("Sipariş Durumu Güncellendi - " + order.getOrderId());
		helper.setText(msg, true);
		mailSender.send(message);
		return true;
	}
	
	public UserDtls getLoggedInUserDetails(Principal p) {
		String email = p.getName();
		UserDtls userDtls = userService.getUserByEmail(email);
		return userDtls;
	}
	

}

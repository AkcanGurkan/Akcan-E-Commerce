package com.example.bitirmeprojemfinal.entity;



import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class UserDtls {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String name;

	private String mobileNumber;

	private String email;

	private String address;

	private String city;

	private String state; //Durumumuz

	private String pincode;

	private String password;

	private String profileImage;

	private String role;

	private Boolean isEnable; //etkin

	private Boolean accountNonLocked; //hesap kitli değil

	private Integer failedAttempt; //başarısız deneme

	private Date lockTime;
	
	private String resetToken;

}

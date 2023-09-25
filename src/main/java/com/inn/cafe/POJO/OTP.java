package com.inn.cafe.POJO;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "otp")
public class OTP {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "email")
    private String email;

    @Column(name = "created_otp")
    private LocalDateTime createdOtp;

    @Column(name = "otp")
    private Integer otp;

    @Column(name = "active")
    private boolean active;

    @Column(name = "expire_in")
    private LocalDateTime expireIn;
    
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", referencedColumnName = "id")
//    private User user;
//
//	public User getUser() {
//		return user;
//	}
//
//	public void setUser(User user) {
//		this.user = user;
//	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public LocalDateTime getCreatedOtp() {
		return createdOtp;
	}

	public void setCreatedOtp(LocalDateTime createdOtp) {
		this.createdOtp = createdOtp;
	}

	public Integer getOtp() {
		return otp;
	}

	public void setOtp(Integer otp) {
		this.otp = otp;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public LocalDateTime getExpireIn() {
		return expireIn;
	}

	public void setExpireIn(LocalDateTime expireIn) {
		this.expireIn = expireIn;
	}

   
}


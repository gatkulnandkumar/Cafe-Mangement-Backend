package com.inn.cafe.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.inn.cafe.POJO.OTP;
import com.inn.cafe.POJO.User;

@Repository
public interface OTPDao extends JpaRepository<OTP,Integer>{

	OTP findByEmail(String email);
}

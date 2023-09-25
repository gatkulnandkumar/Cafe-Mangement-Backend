package com.inn.cafe.POJO;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@NamedQuery(name = "Bill.getAllBills", query = "select b from Bill b order by b.id desc")

@NamedQuery(name = "Bill.getBillByUsername", query = "select b from Bill b where b.createdBy=:username order by b.id desc")

@Entity
@Table(name="bill")
public class Bill implements Serializable{

	private static final long serialVersionUID=1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private Integer id;
	
	@Column(name="uuid")
	private String uuid;
	
	@Column(name="name")
	private String name;

	@Column(name="email")
	private String email;

	@Column(name="contactNumber")
	private String contactNumber;
	
	@Column(name="paymentMethod")
	private String paymentMethod;
	
	@Column(name="total")
	private Integer total;
	
	@Column(name="productdetails",columnDefinition = "json")
	private String productDetail;
	
	@Column(name="createdby")
	private String createdBy;

	public Bill(String uuid, String name, String email, String contactNumber, String paymentMethod, Integer total,
			String productDetail, String createdBy) {
		this.uuid = uuid;
		this.name = name;
		this.email = email;
		this.contactNumber = contactNumber;
		this.paymentMethod = paymentMethod;
		this.total = total;
		this.productDetail = productDetail;
		this.createdBy = createdBy;
	}

	public Bill() {

	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getContactNumber() {
		return contactNumber;
	}

	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public String getProductDetail() {
		return productDetail;
	}

	public void setProductDetail(String productDetail) {
		this.productDetail = productDetail;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	
	
	
	
	
	
	
	
}

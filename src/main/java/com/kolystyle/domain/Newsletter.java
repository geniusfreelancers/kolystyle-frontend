package com.kolystyle.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Newsletter {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	@Column(name="email", nullable = false, updatable = false)
	private String email;
	private Date enrolledDate;
	private Date unenrolledDate;
	private boolean marketing=true;
	private boolean offers=true;
	private boolean sent =false;
	private String lastEmailSent;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Date getEnrolledDate() {
		return enrolledDate;
	}
	public void setEnrolledDate(Date enrolledDate) {
		this.enrolledDate = enrolledDate;
	}
	public Date getUnenrolledDate() {
		return unenrolledDate;
	}
	public void setUnenrolledDate(Date unenrolledDate) {
		this.unenrolledDate = unenrolledDate;
	}
	public boolean isMarketing() {
		return marketing;
	}
	public void setMarketing(boolean marketing) {
		this.marketing = marketing;
	}
	public boolean isOffers() {
		return offers;
	}
	public void setOffers(boolean offers) {
		this.offers = offers;
	}
	public boolean isSent() {
		return sent;
	}
	public void setSent(boolean sent) {
		this.sent = sent;
	}
	public String getLastEmailSent() {
		return lastEmailSent;
	}
	public void setLastEmailSent(String lastEmailSent) {
		this.lastEmailSent = lastEmailSent;
	}
	
}

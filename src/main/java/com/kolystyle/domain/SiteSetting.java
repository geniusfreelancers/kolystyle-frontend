package com.kolystyle.domain;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class SiteSetting {
	@Id
	private Long id =(long) 1;
	private BigDecimal freeShippingMin;
	
	private BigDecimal shippingCost;
	private String homeSlide;
	private BigDecimal premiumShippingCost;
	
	private String categoryBanner;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public BigDecimal getFreeShippingMin() {
		return freeShippingMin;
	}
	public void setFreeShippingMin(BigDecimal freeShippingMin) {
		this.freeShippingMin = freeShippingMin;
	}
	public BigDecimal getShippingCost() {
		return shippingCost;
	}
	public void setShippingCost(BigDecimal shippingCost) {
		this.shippingCost = shippingCost;
	}
	public String getHomeSlide() {
		return homeSlide;
	}
	public void setHomeSlide(String homeSlide) {
		this.homeSlide = homeSlide;
	}
	public BigDecimal getPremiumShippingCost() {
		return premiumShippingCost;
	}
	public void setPremiumShippingCost(BigDecimal premiumShippingCost) {
		this.premiumShippingCost = premiumShippingCost;
	}
	public String getCategoryBanner() {
		return categoryBanner;
	}
	public void setCategoryBanner(String categoryBanner) {
		this.categoryBanner = categoryBanner;
	}	
	
}

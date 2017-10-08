package com.kolystyle.domain;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;

@Entity
public class NewProduct {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	private String title;
	private String brand;
	private String gender;
	private Date addedDate;
	private String shippingOption;
	private String availabilityToShip;
	private int sku;
	private double shippingWeight;
	private double listPrice;
	private double ourPrice;
	private boolean active=true;
	private String extraPromo;
	@Column(columnDefinition="text")
	private String description;
	@Column(columnDefinition="text")
	private String tailoringService;
	@Column(columnDefinition="text")
	private String colorAndWashCare;
	@Column(columnDefinition="text")
	private String shippingAndReturnPolicy;
	private int inStockNumber;

	private String coverImageName;
	private String feature;
	private String size;
	private String category;
	
	/*@OneToMany(mappedBy="newProduct")
	@JsonIgnore
	private List<CategoryList> categoryList;*/
	
	@Transient
	private List<MultipartFile> productImage;
	
	@OneToMany(mappedBy="product")
	@JsonIgnore
	private List<ProductToCartItem> productToCartItemList;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Date getAddedDate() {
		return addedDate;
	}

	public void setAddedDate(Date addedDate) {
		this.addedDate = addedDate;
	}

	public String getShippingOption() {
		return shippingOption;
	}

	public void setShippingOption(String shippingOption) {
		this.shippingOption = shippingOption;
	}

	public String getAvailabilityToShip() {
		return availabilityToShip;
	}

	public void setAvailabilityToShip(String availabilityToShip) {
		this.availabilityToShip = availabilityToShip;
	}

	public int getSku() {
		return sku;
	}

	public void setSku(int sku) {
		this.sku = sku;
	}

	public double getShippingWeight() {
		return shippingWeight;
	}

	public void setShippingWeight(double shippingWeight) {
		this.shippingWeight = shippingWeight;
	}

	public double getListPrice() {
		return listPrice;
	}

	public void setListPrice(double listPrice) {
		this.listPrice = listPrice;
	}

	public double getOurPrice() {
		return ourPrice;
	}

	public void setOurPrice(double ourPrice) {
		this.ourPrice = ourPrice;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getExtraPromo() {
		return extraPromo;
	}

	public void setExtraPromo(String extraPromo) {
		this.extraPromo = extraPromo;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTailoringService() {
		return tailoringService;
	}

	public void setTailoringService(String tailoringService) {
		this.tailoringService = tailoringService;
	}

	public String getColorAndWashCare() {
		return colorAndWashCare;
	}

	public void setColorAndWashCare(String colorAndWashCare) {
		this.colorAndWashCare = colorAndWashCare;
	}

	public String getShippingAndReturnPolicy() {
		return shippingAndReturnPolicy;
	}

	public void setShippingAndReturnPolicy(String shippingAndReturnPolicy) {
		this.shippingAndReturnPolicy = shippingAndReturnPolicy;
	}

	public int getInStockNumber() {
		return inStockNumber;
	}

	public void setInStockNumber(int inStockNumber) {
		this.inStockNumber = inStockNumber;
	}

	public String getCoverImageName() {
		return coverImageName;
	}

	public void setCoverImageName(String coverImageName) {
		this.coverImageName = coverImageName;
	}

	public String getFeature() {
		return feature;
	}

	public void setFeature(String feature) {
		this.feature = feature;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public List<MultipartFile> getProductImage() {
		return productImage;
	}

	public void setProductImage(List<MultipartFile> productImage) {
		this.productImage = productImage;
	}

	public List<ProductToCartItem> getProductToCartItemList() {
		return productToCartItemList;
	}

	public void setProductToCartItemList(List<ProductToCartItem> productToCartItemList) {
		this.productToCartItemList = productToCartItemList;
	}

	
	
	
}

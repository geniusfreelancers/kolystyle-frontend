/*package com.kolystyle.domain;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Category {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	private String title;
	
	@OneToOne
	private NewProduct newProduct;
	
	@OneToMany(mappedBy = "category")
	@JsonIgnore
	private List<CategoryList> categoryList;

	
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
	public List<CategoryList> getCategoryList() {
		return categoryList;
	}
	public void setCategoryList(List<CategoryList> categoryList) {
		this.categoryList = categoryList;
	}
	
	
}
*/

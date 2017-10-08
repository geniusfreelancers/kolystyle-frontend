package com.kolystyle.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kolystyle.domain.ListItem;
import com.kolystyle.domain.Product;
import com.kolystyle.domain.ProductToListItem;
import com.kolystyle.domain.User;
import com.kolystyle.domain.WishList;
import com.kolystyle.repository.ListItemRepository;
import com.kolystyle.repository.ProductToListItemRepository;
import com.kolystyle.service.ListItemService;

@Service
public class ListItemServiceImpl implements ListItemService {

	@Autowired
	private ListItemRepository listItemRepository;
	
	@Autowired
	private ProductToListItemRepository productToListItemRepository;
	
	
	public List<ListItem> findByWishList(WishList wishList){
		return listItemRepository.findByWishList(wishList);
	}
	
	
	public ListItem addProductToListItem(Product product,User user){
		List<ListItem> listItemList = findByWishList(user.getWishList());
		
		for(ListItem listItem : listItemList){
			if(product.getId() == listItem.getProduct().getId()){
				listItemRepository.save(listItem);
				return listItem;
			}
		}
		
		ListItem listItem = new ListItem();
		listItem.setWishList(user.getWishList());
		listItem.setProduct(product);
		
		listItem = listItemRepository.save(listItem);
		
		ProductToListItem productToListItem = new ProductToListItem();
		productToListItem.setProduct(product);
		productToListItem.setListItem(listItem);
		productToListItemRepository.save(productToListItem);
		
		return listItem;
	}
	
	public ListItem findById(Long id){
		return listItemRepository.findOne(id);
	}
	
	public void removeListItem(ListItem listItem){
		productToListItemRepository.deleteByListItem(listItem);
		listItemRepository.delete(listItem);
	}
	
	public ListItem save(ListItem listItem){
		return listItemRepository.save(listItem);
	}


	
}

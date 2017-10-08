package com.kolystyle.service;
import java.util.List;

import com.kolystyle.domain.ListItem;
import com.kolystyle.domain.Product;
import com.kolystyle.domain.User;
import com.kolystyle.domain.WishList;

public interface ListItemService {
	List<ListItem> findByWishList(WishList wishList);
	
	ListItem addProductToListItem(Product product,User user);
	
	ListItem findById(Long id);
	
	void removeListItem(ListItem listItem);
	
	ListItem save(ListItem listItem);

}






package com.kolystyle.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kolystyle.domain.ListItem;
import com.kolystyle.domain.WishList;
import com.kolystyle.repository.WishListRepository;
import com.kolystyle.service.ListItemService;
import com.kolystyle.service.WishListService;
@Service
public class WishListServiceImpl implements WishListService{

	@Autowired
	private ListItemService listItemService;
	
	@Autowired
	private WishListRepository wishListRepository;
	
	public WishList updateWishList(WishList wishList){
		
		List<ListItem> listItemList = listItemService.findByWishList(wishList);
		
		for(ListItem listItem : listItemList){
			if(listItem.getProduct().getInStockNumber() > 0){
				/*listItemService.updateListItem(listItem);
				cartTotal = cartTotal.add(cartItem.getSubtotal());*/
			}
		}
		
		
		wishListRepository.save(wishList);
		
		return wishList;
	}
	
	
	public void clearWishList(WishList wishList){
		List<ListItem> listItemList = listItemService.findByWishList(wishList);
		
		for(ListItem listItem : listItemList){
			listItem.setWishList(null);
			listItemService.save(listItem);
		}
		wishListRepository.save(wishList);
	}
}

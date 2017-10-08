package com.kolystyle.service;

import com.kolystyle.domain.Product;
import com.kolystyle.domain.User;
import com.kolystyle.domain.WishList;

public interface WishListService {
	WishList updateWishList(WishList wishList);
	
	void clearWishList(WishList wishList);
}


package com.kolystyle.repository;
import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.kolystyle.domain.ListItem;
import com.kolystyle.domain.WishList;

@Transactional
public interface ListItemRepository extends CrudRepository<ListItem, Long>{
	List<ListItem> findByWishList(WishList wishList);
}




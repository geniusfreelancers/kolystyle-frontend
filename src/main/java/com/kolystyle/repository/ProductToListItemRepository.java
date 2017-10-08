package com.kolystyle.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.kolystyle.domain.ListItem;
import com.kolystyle.domain.ProductToListItem;
@Transactional
public interface ProductToListItemRepository extends CrudRepository<ProductToListItem, Long>{
	void deleteByListItem(ListItem listItem);
}

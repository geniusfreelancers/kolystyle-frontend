package com.kolystyle.repository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.kolystyle.domain.Product;

@Repository
public interface ProductsRepository extends PagingAndSortingRepository<Product,Long>{

}

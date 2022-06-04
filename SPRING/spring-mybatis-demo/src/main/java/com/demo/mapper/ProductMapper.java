package com.demo.mapper;


import com.demo.domain.Product;
import java.util.List;

public interface ProductMapper {
    List<Product> selectAllProducts();
    int insertProduct(Product product);
    void removeProduct(Product product);
}

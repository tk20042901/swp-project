package com.swp.project.repository.product;


import com.swp.project.entity.product.SubImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubImageRepository extends JpaRepository<SubImage,Integer> {

   
}

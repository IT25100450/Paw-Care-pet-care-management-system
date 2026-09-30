package com.pawcare.backend.specification;

import com.pawcare.backend.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductSpecification {

    public static Specification<Product> hasKeyword(String keyword) {

        return (root, query, criteriaBuilder) -> {

            if (keyword == null || keyword.trim().isEmpty()) {
                return null;
            }

            String searchKeyword =
                    "%" + keyword.toLowerCase() + "%";

            return criteriaBuilder.or(

                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.get("productName")
                            ),
                            searchKeyword
                    ),

                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.get("description")
                            ),
                            searchKeyword
                    ),

                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.get("brand")
                            ),
                            searchKeyword
                    )
            );
        };
    }

    public static Specification<Product> hasCategory(
            String category
    ) {

        return (root, query, criteriaBuilder) -> {

            if (category == null ||
                    category.trim().isEmpty()) {
                return null;
            }

            return criteriaBuilder.equal(
                    criteriaBuilder.lower(
                            root.get("category")
                    ),
                    category.toLowerCase()
            );
        };
    }

    public static Specification<Product> hasBrand(
            String brand
    ) {

        return (root, query, criteriaBuilder) -> {

            if (brand == null ||
                    brand.trim().isEmpty()) {
                return null;
            }

            return criteriaBuilder.equal(
                    criteriaBuilder.lower(
                            root.get("brand")
                    ),
                    brand.toLowerCase()
            );
        };
    }

    public static Specification<Product> priceGreaterThanOrEqual(
            BigDecimal minPrice
    ) {

        return (root, query, criteriaBuilder) -> {

            if (minPrice == null) {
                return null;
            }

            return criteriaBuilder.greaterThanOrEqualTo(
                    root.get("price"),
                    minPrice
            );
        };
    }

    public static Specification<Product> priceLessThanOrEqual(
            BigDecimal maxPrice
    ) {

        return (root, query, criteriaBuilder) -> {

            if (maxPrice == null) {
                return null;
            }

            return criteriaBuilder.lessThanOrEqualTo(
                    root.get("price"),
                    maxPrice
            );
        };
    }
}
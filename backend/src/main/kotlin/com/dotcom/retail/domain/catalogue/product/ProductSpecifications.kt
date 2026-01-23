package com.dotcom.retail.domain.catalogue.product

import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification

object ProductSpecifications {
    fun fromParams(params: ProductQueryParams): Specification<Product> {
        return Specification { root, query, cb ->
            val predicates = mutableListOf<Predicate>()

            params.categoryId?.let {
                predicates.add(cb.equal(root.get<Product>("category").get<Long>("id"), it))
            }

            if (params.brands.isNotEmpty()) {
                predicates.add(root.get<Product>("brand").get<Long>("id").`in`(params.brands))
            }

//            params.attributes?.forEach { attr ->
//                val orPredicates = attr.values.map { value ->
//                    cb.equal(
//                        cb.function(
//                            "jsonb_extract_path_text",
//                            String::class.java,
//                            root.get<Map<String, Any>>("attributes"),
//                            cb.literal(attr.name)
//                        ),
//                        value.toString()
//                    )
//                }.toTypedArray()
//
//                if (orPredicates.isNotEmpty()) {
//                    predicates.add(cb.or(*orPredicates))
//                }
//            }

            params.attributes?.forEach { (name, values) ->
                if (values.isNotEmpty()) {
                    val jsonPathExpression = cb.function(
                        "jsonb_extract_path_text",
                        String::class.java,
                        root.get<Any>("attributes"),
                        cb.literal(name)
                    )

                    val orPredicates = values.map { value ->
                        cb.equal(jsonPathExpression, value.toString())
                    }.toTypedArray()

                    predicates.add(cb.or(*orPredicates))
                }
            }

            predicates.add(cb.isTrue(root.get<Boolean>("isActive")))

            cb.and(*predicates.toTypedArray())
        }

    }
}
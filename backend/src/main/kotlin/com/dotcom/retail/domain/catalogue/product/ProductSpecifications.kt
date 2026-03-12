package com.dotcom.retail.domain.catalogue.product

import com.dotcom.retail.common.model.SortOrder
import com.dotcom.retail.domain.catalogue.category.attribute.AttributeMetadataService
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class ProductSpecifications(
    private val attributeMetadataService: AttributeMetadataService,
    private val objectMapper: ObjectMapper,
) {
    fun fromParams(params: ProductQuery): Specification<Product> {
        return Specification { root, query, cb ->
            val predicates = mutableListOf<Predicate>()

            params.categoryId?.let {
                predicates.add(cb.equal(root.get<Product>("category").get<Long>("id"), it))
            }

            if (params.brands.isNotEmpty()) {
                predicates.add(root.get<Product>("brand").get<Long>("id").`in`(params.brands))
            }

            params.attributes?.forEach { attr ->
                if (attributeMetadataService.isSlider(attr.name)) {
                    val min = attr.values.firstOrNull()?.toString()?.toDoubleOrNull()
                    val max = attr.values.lastOrNull()?.toString()?.toDoubleOrNull()

                    if (min == null || max == null) return@forEach

                    val predicatesString = mutableListOf<String>()
                    predicatesString.add("@ >= $min")
                    predicatesString.add("@ <= $max")

                    val rangePred = predicatesString.joinToString(" && ")
                    val jsonPath = "$.\"${attr.name}\"[*] ? ($rangePred)"

                    predicates.add(
                        cb.isTrue(
                            cb.function(
                                "jsonb_path_exists",
                                Boolean::class.java,
                                root.get<Any>("attributes"),
                                cb.literal(jsonPath)
                            )))
                } else {
                    val orPredicates = attr.values.mapNotNull { value ->
                        val checkedValue = if (attributeMetadataService.isNumeric(attr.name)) {
                            value.toString().toDoubleOrNull() ?: return@mapNotNull null
                        } else value.toString()

                        val json = objectMapper.writeValueAsString(mapOf(attr.name to listOf(checkedValue)))
                        cb.isTrue(
                            cb.function(
                                "jsonb_contains",
                                Boolean::class.java,
                                root.get<Any>("attributes"),
                                cb.literal(json)
                            )
                        )
                    }.toTypedArray()

                    if (orPredicates.isNotEmpty()) {
                        predicates.add(cb.or(*orPredicates))
                    }
                }
            }

            params.sort.let { sortOrder ->
                if (sortOrder == SortOrder.TOP) return@let

                val effectivePrice = cb.coalesce(
                    root.get<BigDecimal>("salePrice"),
                    root.get<BigDecimal>("price")
                )

                val order = if (sortOrder == SortOrder.PRICE_ASC) {
                    cb.asc(effectivePrice)
                } else {
                    cb.desc(effectivePrice)
                }

                query?.orderBy(order)
            }

            params.price?.let {
                val effectivePrice = cb.coalesce(
                    root.get<BigDecimal>("salePrice"),
                    root.get<BigDecimal>("price")
                )
                it.min.let { min -> predicates.add(cb.greaterThanOrEqualTo(effectivePrice, BigDecimal.valueOf(min))) }
                it.max.let { max -> predicates.add(cb.lessThanOrEqualTo(effectivePrice, BigDecimal.valueOf(max))) }

            }

            predicates.add(cb.greaterThan(root.get<Int>("stock"), 0))

            cb.and(*predicates.toTypedArray())
        }

    }
}
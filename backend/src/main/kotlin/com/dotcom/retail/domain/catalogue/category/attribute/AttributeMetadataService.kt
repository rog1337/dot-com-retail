package com.dotcom.retail.domain.catalogue.category.attribute

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicReference

@Service
class AttributeMetadataService(private val categoryAttributeService: CategoryAttributeService) {

    private val attributes = AtomicReference<Map<String, CategoryAttribute>>()

    @PostConstruct
    fun initialize() {
        refresh()
    }

    fun getAttribute(attribute: String): CategoryAttribute? {
        return attributes.get()[attribute]
    }

    fun isNumeric(attribute: String): Boolean {
        return attributes.get()[attribute]?.dataType == AttributeDataType.NUMBER
    }

    fun isSlider(attribute: String): Boolean {
        return attributes.get()[attribute]?.filterType == FilterType.SLIDER
    }

    fun refresh() {
        val attrs = categoryAttributeService.findAll().associateBy { it.attribute }.toMutableMap()
        attributes.set(attrs)
    }
}

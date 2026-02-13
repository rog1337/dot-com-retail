package com.dotcom.retail.domain.catalogue.category.attribute

import com.dotcom.retail.common.BaseEntity
import com.dotcom.retail.domain.catalogue.category.Category
import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany

@Entity
class CategoryAttribute(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    var attribute: String,
    var label: String,
    var unit: String? = null,
    var dataType: AttributeDataType,
    var filterType: FilterType,
    var displayOrder: Int,
    var isPublic: Boolean,

    @ManyToMany(mappedBy = "attributes")
    @JsonBackReference
    var categories: MutableSet<Category> = mutableSetOf()

    ) : BaseEntity() {

    override fun toString(): String {
        return "CategoryAttribute(id=$id, attribute='$attribute', label='$label', type=$filterType, displayOrder=$displayOrder), ${super.toString()}"
    }
}

enum class FilterType {
    CHECKBOX, DROPDOWN, SLIDER
}

enum class AttributeDataType {
    TEXT, NUMBER, BOOLEAN
}
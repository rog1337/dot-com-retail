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

    val attribute: String,
    val label: String,
    val type: FilterType,
    val displayOrder: Int,
    val isPublic: Boolean,

    @ManyToMany(mappedBy = "attributes")
    @JsonBackReference
    val categories: MutableSet<Category> = mutableSetOf()

    ) : BaseEntity() {

    override fun toString(): String {
        return "CategoryAttribute(id=$id, attribute='$attribute', label='$label', type=$type, displayOrder=$displayOrder), ${super.toString()}"
    }
}

enum class FilterType {
    CHECKBOX, DROPDOWN, SLIDER, RANGE
}
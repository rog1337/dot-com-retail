package com.dotcom.retail.domain.catalogue.category

import com.dotcom.retail.common.model.AuditingEntity
import com.dotcom.retail.domain.catalogue.category.attribute.CategoryAttribute
import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany

@Entity
class Category(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    var name: String,

    @ManyToMany
    @JoinTable(
        name = "category_attributes",
        joinColumns = [JoinColumn(name = "category_id")],
        inverseJoinColumns = [JoinColumn(name = "attribute_id")]
    )
    @JsonManagedReference
    var attributes: MutableList<CategoryAttribute> = mutableListOf()

) : AuditingEntity() {

    override fun toString(): String {
        return "Category(id=$id, name='$name', ${super.toString()})"
    }
}
package com.dotcom.retail.domain.user

import com.dotcom.retail.common.model.AuditingEntity
import com.dotcom.retail.domain.order.Order
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.UUID

@Table(name = "users")
@Entity
class User(

    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(unique = true)
    var email: String,
    var passwordHash: String? = null,

    var displayName: String,

    var twoFactorSecret: String? = null,
    var twoFactorEnabled: Boolean = false,

    @OneToOne(cascade = [CascadeType.ALL])
    var contact: Contact? = null,

    @Enumerated(EnumType.STRING)
    var role: Role = Role.USER,

    ) : AuditingEntity(), UserDetails {

    override fun toString(): String {
        return "User(id=$id, email='$email', passwordHash=$passwordHash, displayName='$displayName', twoFactorSecret=$twoFactorSecret, twoFactorEnabled=$twoFactorEnabled, role=$role, ${super.toString()})"
    }
    override fun getAuthorities(): Collection<Role> = listOf(role)
    override fun getUsername(): String = email
    override fun getPassword(): String? = passwordHash
}

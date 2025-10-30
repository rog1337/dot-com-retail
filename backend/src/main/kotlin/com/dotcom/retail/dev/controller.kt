package com.dotcom.retail.dev

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
class controller {

    @GetMapping("/")
    fun home(@AuthenticationPrincipal user: OAuth2User?): String {
        println("user: $user")
        return "Home page"
    }

    @GetMapping("/info")
//    fun getInfo(authentication: OAuth2AuthenticationToken): OAuth2AuthenticationToken {
    fun getInfo(@AuthenticationPrincipal authentication: OAuth2User?): Any? {
        println(authentication)
        return authentication
    }

//    data class Info(
//        private val application: String,
//        private val principal: MutableMap<String, Any>
//    )
}
package br.com.fiap.produtosms.utils;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

public class GitHubUserUtils {

    private GitHubUserUtils() {
    }

    public static String getUsername(OAuth2AuthenticationToken authentication) {
        return authentication.getPrincipal().getAttribute("login");
    }

    public static String getAvatar(OAuth2AuthenticationToken authentication) {
        return authentication.getPrincipal().getAttribute("avatar_url");
    }
}

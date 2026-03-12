package com.vaistra.config;

import com.vaistra.entity.Role;
import com.vaistra.entity.User;
import com.vaistra.repository.RoleRepository;
import com.vaistra.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class OAuthAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;
    private static boolean isNull = false;

    public OAuthAuthenticationSuccessHandler(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();

        oAuth2User.getAttributes().forEach((key, value) -> {
            System.out.println(key + "=>" + value);
        });

        System.out.println("User mail is " + oAuth2User.getAttribute("email"));

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        System.out.println("Email :: " + email);
        if(email == null || email.isEmpty() || email.isBlank()) {
            System.out.println("Email is Null!");
            isNull = true;
        }

//        assert email != null;
        User socialUser = userRepository.findByEmailIgnoreCase(email);

        Role role = roleRepository.findByRoleNameIgnoreCase("ROLE_USER");


        if(socialUser==null){
            socialUser = new User();

            socialUser.setEmail(email);
            socialUser.setPassword(null);
            socialUser.setPwd(null);
            socialUser.setFullName(name);
            socialUser.setMobNo(null);
            socialUser.setAddressLine1(null);
            socialUser.setJwtToken(null);
            socialUser.setIsDeleted(false);
            socialUser.setIsLoggedOut(true);
            socialUser.setCreatedAt(LocalDateTime.now());
            socialUser.setUpdatedAt(LocalDateTime.now());
            socialUser.setDeletedAt(null);
            socialUser.setActiveStatus(true);
            socialUser.setRole(role);

            userRepository.save(socialUser);

        }

        new DefaultRedirectStrategy().sendRedirect(request,response,"/auth/SocialLogin?email="+email+"&isNull="+isNull);
    }
}

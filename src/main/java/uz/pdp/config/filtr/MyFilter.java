package uz.pdp.config.filtr;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import uz.pdp.entity.User;

import java.io.IOException;

@Component
public class    MyFilter implements Filter {

    @Autowired
    @Lazy
    UserDetailsService userDetailsService;
    
    @Autowired
    private JwtProvider jwtProvider;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String authorization = request.getHeader("Authorization");
        
        if (authorization == null || authorization.isBlank()) {
            filterChain.doFilter(request, servletResponse);
            return;
        }

        if (authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            String username = jwtProvider.getUsernameFromToken(token);
            if (username != null) {
                setUserToContext(username);
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private void setUserToContext(String username) {
        User user = (User) userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities()  // This will now use the properly prefixed authorities from User class
                );
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}

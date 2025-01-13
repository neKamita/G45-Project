package uz.pdp.controller.graphql;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import uz.pdp.entity.User;
import uz.pdp.payload.EntityResponse;
import uz.pdp.service.UserService;
@Controller
public class UserGraphQLController {
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserGraphQLController.class);

    @Autowired
    public UserGraphQLController(UserService userService) {
        this.userService = userService;
    }

    @QueryMapping
    public User currentUser() {
        logger.info("GraphQL Query: Fetching current user");
        return userService.getCurrentUser();
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public EntityResponse<List<User>> users() {
        logger.info("GraphQL Query: Fetching all users");
        return userService.getAllUsers();
    }

    @MutationMapping
    public User requestSeller(@Argument Long userId) {
        logger.info("GraphQL Mutation: Requesting seller status for user {}", userId);
        return userService.requestSeller(Long.valueOf(userId)).data();
    }
}
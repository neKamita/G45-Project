package uz.pdp.controller.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import uz.pdp.dto.UserDoorHistoryDto;
import uz.pdp.entity.DoorHistory;
import uz.pdp.service.DoorHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * GraphQL controller for door history operations.
 * Provides endpoints for querying door access history and usage patterns.
 * Implements security controls for accessing sensitive history data.
 *
 * @version 1.0
 * @since 2025-01-17
 */
@Controller
public class DoorHistoryGraphQLController {
    private static final Logger logger = LoggerFactory.getLogger(DoorHistoryGraphQLController.class);
    
    private final DoorHistoryService doorHistoryService;

    public DoorHistoryGraphQLController(DoorHistoryService doorHistoryService) {
        this.doorHistoryService = doorHistoryService;
    }

    /**
     * GraphQL query to retrieve door access history.
     * Requires appropriate access permissions.
     *
     * @param doorId ID of the door
     * @return List of door history records
     */
    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public List<DoorHistory> getDoorHistory(@Argument Long doorId) {
        try {
            logger.info("GraphQL query: Retrieving history for door ID: {}", doorId);
            List<DoorHistory> history = doorHistoryService.getDoorHistory(doorId);
            logger.info("Retrieved {} history records for door {}", history.size(), doorId);
            return history;
        } catch (Exception e) {
            logger.error("Error retrieving door history via GraphQL: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * GraphQL query to retrieve user's door access history.
     * Users can only access their own history unless admin.
     *
     * @param userId ID of the user
     * @return List of door history records for the user
     */
    @QueryMapping
    public List<DoorHistory> getUserHistory(@Argument Long userId) {
        try {
            logger.info("GraphQL query: Retrieving history for user ID: {}", userId);
            List<DoorHistory> history = doorHistoryService.getUserHistory(userId);
            logger.info("Retrieved {} history records for user {}", history.size(), userId);
            return history;
        } catch (Exception e) {
            logger.error("Error retrieving user history via GraphQL: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * GraphQL query to retrieve detailed user-door interaction history.
     * Provides aggregated statistics about door usage.
     *
     * @param userId ID of the user
     * @param doorId ID of the door
     * @return DTO containing detailed history information
     */
    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public UserDoorHistoryDto getUserDoorHistory(
            @Argument Long userId,
            @Argument Long doorId) {
        try {
            logger.info("GraphQL query: Retrieving history for user {} and door {}", userId, doorId);
            UserDoorHistoryDto history = doorHistoryService.getUserDoorHistory(userId, doorId);
            logger.info("Retrieved detailed history for user {} and door {}", userId, doorId);
            return history;
        } catch (Exception e) {
            logger.error("Error retrieving user-door history via GraphQL: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * GraphQL query to retrieve recent access history.
     * Returns the most recent door access events.
     *
     * @param limit Maximum number of records to retrieve
     * @return List of recent door history records
     */
    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<DoorHistory> getRecentHistory(@Argument int limit) {
        try {
            logger.info("GraphQL query: Retrieving {} most recent history records", limit);
            List<DoorHistory> history = doorHistoryService.getRecentHistory(limit);
            logger.info("Retrieved {} recent history records", history.size());
            return history;
        } catch (Exception e) {
            logger.error("Error retrieving recent history via GraphQL: {}", e.getMessage());
            throw e;
        }
    }
}

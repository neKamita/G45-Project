package uz.pdp.controller.graphql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import uz.pdp.dto.UserDoorHistoryDto;
import uz.pdp.service.DoorHistoryService;
import uz.pdp.service.UserService;

@Controller
public class DoorHistoryGraphQLController {
    private final DoorHistoryService doorHistoryService;
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(DoorHistoryGraphQLController.class);

    @Autowired
    public DoorHistoryGraphQLController(DoorHistoryService doorHistoryService, UserService userService) {
        this.doorHistoryService = doorHistoryService;
        this.userService = userService;
    }

    @QueryMapping
    public UserDoorHistoryDto doorHistory() {
        Long userId = userService.getCurrentUser().getId();
        return doorHistoryService.getUserDoorHistoryGrouped(userId);
    }
}

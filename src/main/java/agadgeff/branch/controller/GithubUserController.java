package agadgeff.branch.controller;

import agadgeff.branch.model.UserOverview;
import agadgeff.branch.service.UserService;
import org.springframework.web.bind.annotation.*;

@RequestMapping("user")
@RestController
public class GithubUserController {
  private final UserService userService;

  public GithubUserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/{username}")
  @ResponseBody
  public UserOverview getUserOverview(final @PathVariable String username) {
    return userService.getUserOverview(username);
  }
}

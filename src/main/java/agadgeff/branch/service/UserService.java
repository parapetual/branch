package agadgeff.branch.service;

import agadgeff.branch.restclient.GithubClient;
import agadgeff.branch.external.model.GithubRepo;
import agadgeff.branch.external.model.GithubUser;
import agadgeff.branch.model.UserOverview;
import agadgeff.branch.transformer.ModelTransformer;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
  final GithubClient githubClient;

  public UserService(final GithubClient githubClient) {
    this.githubClient = githubClient;
  }

  @Cacheable("userOverview")
  public UserOverview getUserOverview(final String username) {
    GithubUser userSummary = githubClient.getUserSummary(username);
    List<GithubRepo> userRepos = githubClient.getReposForUser(username);
    return UserOverview.builder()
        .userName(userSummary.getLogin())
        .displayName(userSummary.getName())
        .avatar(userSummary.getAvatarUrl())
        .geoLocation(userSummary.getLocation())
        .email(userSummary.getEmail())
        .url(userSummary.getHtmlUrl())
        .createdAt(userSummary.getCreatedAt())
        .repos(ModelTransformer.githubReposToRepos(userRepos))
        .build();
  }
}

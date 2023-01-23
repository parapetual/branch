package agadgeff.branch.restclient;

import agadgeff.branch.external.model.GithubRepo;
import agadgeff.branch.external.model.GithubUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class GithubClient {
  Logger log = LoggerFactory.getLogger(GithubClient.class);

  private final String userEndpoint;
  private final String reposEndpoint;
  private final RestTemplate restTemplate;

  public GithubClient(@Value("${external.endpoints.github.USER}") final String userEndpoint,
                      @Value("${external.endpoints.github.REPOS}") final String reposEndpoint,
                      final RestTemplate restTemplate) {
    this.userEndpoint = userEndpoint;
    this.reposEndpoint = reposEndpoint;
    this.restTemplate = restTemplate;
  }

  public GithubUser getUserSummary(final String username) {
    log.info("GitHub request for user details, user={}", username);
    return restTemplate.getForObject(userEndpoint, GithubUser.class, username);
  }

  public List<GithubRepo> getReposForUser(final String username) {
    log.info("GitHub request for user repos, user={}", username);
    return restTemplate.exchange(RequestEntity.get(reposEndpoint, username).build(),
        new ParameterizedTypeReference<List<GithubRepo>>() {
        }).getBody();
  }
}

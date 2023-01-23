package agadgeff.branch;

import agadgeff.branch.external.model.GithubError;
import agadgeff.branch.external.model.GithubRepo;
import agadgeff.branch.external.model.GithubUser;
import agadgeff.branch.model.UserOverview;
import agadgeff.branch.restclient.RestClientErrorHandler;
import agadgeff.branch.transformer.ModelTransformer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
class BackendcodingApplicationIntegrationTests {
  final EasyRandom easyRandom = new EasyRandom();
  @Value("${external.endpoints.github.USER}")
  String userEndpoint;
  @Value("${external.endpoints.github.REPOS}")
  String reposEndpoint;

  final String user = easyRandom.nextObject(String.class);

  @MockBean
  RestTemplate restTemplate;

  @Autowired
  RestClientErrorHandler restClientErrorHandler;
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  ObjectMapper objectMapper;

  @Test
  @SneakyThrows
  void shouldReturnResultsForExistingUser() {
    final GithubUser githubUser = easyRandom.nextObject(GithubUser.class);
    final List<GithubRepo> githubRepos = easyRandom.objects(GithubRepo.class, easyRandom.nextInt(9))
        .collect(Collectors.toList());
    when(restTemplate.getForObject(userEndpoint, GithubUser.class, user))
        .thenReturn(githubUser);
    when(restTemplate.exchange(RequestEntity.get(reposEndpoint, user).build(),
        new ParameterizedTypeReference<List<GithubRepo>>() {
        }))
        .thenReturn(new ResponseEntity<>(githubRepos, HttpStatus.OK));

    mockMvc.perform(get("/user/{username}", user))
        .andExpect(status().isOk())
        .andExpect(content().json(objectMapper.writeValueAsString(UserOverview.builder()
            .userName(githubUser.getLogin())
            .displayName(githubUser.getName())
            .avatar(githubUser.getAvatarUrl())
            .geoLocation(githubUser.getLocation())
            .email(githubUser.getEmail())
            .url(githubUser.getHtmlUrl())
            .createdAt(githubUser.getCreatedAt())
            .repos(ModelTransformer.githubReposToRepos(githubRepos))
            .build())));
  }

  @Test
  @SneakyThrows
  void shouldReturnNotFoundForMissingUser() {
    when(restTemplate.getForObject(userEndpoint, GithubUser.class, user)).thenAnswer(ignored -> {
      ClientHttpResponse fakeResponse =
          new MockClientHttpResponse(objectMapper.writeValueAsBytes(new GithubError()), HttpStatus.NOT_FOUND.value());
      if (!restClientErrorHandler.hasError(fakeResponse)) {
        fail();
      } else {
        restClientErrorHandler.handleError(fakeResponse);
      }
      return null;
    });

    mockMvc.perform(get("/user/{username}", user))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("User not found"));
  }


  @Test
  @SneakyThrows
  void shouldPassThroughApiRateLimitError() {
    final GithubError fakeError = easyRandom.nextObject(GithubError.class);
    when(restTemplate.getForObject(userEndpoint, GithubUser.class, user)).thenAnswer(ignored -> {
      ClientHttpResponse fakeResponse =
          new MockClientHttpResponse(objectMapper.writeValueAsBytes(fakeError), HttpStatus.FORBIDDEN.value());
      if (!restClientErrorHandler.hasError(fakeResponse)) {
        fail();
      } else {
        restClientErrorHandler.handleError(fakeResponse);
      }
      return null;
    });

    mockMvc.perform(get("/user/{username}", user))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value(fakeError.getMessage()));
  }
}

package agadgeff.branch.transformer;

import agadgeff.branch.external.model.GithubRepo;
import agadgeff.branch.model.Repo;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@UtilityClass
public class ModelTransformer {

  public List<Repo> githubReposToRepos(List<GithubRepo> userRepos) {
    return userRepos.stream()
        .map(githubRepo -> new Repo(githubRepo.getName(), githubRepo.getHtmlUrl()))
        .collect(Collectors.toList());
  }
}

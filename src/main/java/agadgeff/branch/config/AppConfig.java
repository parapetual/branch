package agadgeff.branch.config;

import agadgeff.branch.restclient.RestClientErrorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableCaching
public class AppConfig {

  @Autowired
  RestClientErrorHandler restClientErrorHandler;
  @Autowired
  RestTemplateBuilder restTemplateBuilder;

  @Bean
  public RestTemplate restTemplate() {
    return restTemplateBuilder.errorHandler(restClientErrorHandler).build();
  }
}

package agadgeff.branch.restclient;

import agadgeff.branch.external.model.GithubError;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Component
public class RestClientErrorHandler implements ResponseErrorHandler {

  final ObjectMapper objectMapper;

  RestClientErrorHandler(final ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public boolean hasError(ClientHttpResponse response) throws IOException {
    return response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError();
  }

  @Override
  public void handleError(ClientHttpResponse response) throws IOException {
    final String message = objectMapper.readValue(response.getBody(), GithubError.class).getMessage();
    throw new ResponseStatusException(response.getStatusCode(), message);
  }
}

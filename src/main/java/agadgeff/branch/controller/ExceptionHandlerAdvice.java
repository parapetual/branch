package agadgeff.branch.controller;

import agadgeff.branch.model.ApiError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
public class ExceptionHandlerAdvice {

  @ExceptionHandler(value = ResponseStatusException.class)
  @ResponseBody
  public ApiError handleNotFound(final ResponseStatusException ex, final HttpServletResponse response) {
    response.setStatus(ex.getRawStatusCode());
    switch (ex.getStatus()) {
      case NOT_FOUND:
        return new ApiError("User not found");
      case FORBIDDEN:
        return new ApiError(ex.getReason());
      default:
        return new ApiError("Unmapped error from downstream API: " + ex.getReason());
    }
  }
}

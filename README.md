This is simple application that returns some details for a GitHub user and a list of all their repositories,
as specified in the Coding Exercise PDF.

# Building and running

Building:  
`./gradlew build` (executes tests too)

Running tests:  
`./gradlew test`

Running the service:  
`./gradlew bootRun` (CTRL+C to terminate)

Test with given sample username:  
`curl localhost:8080/user/octocat | json_pp`

# Design Decisions

## Application

- The app is a based on Spring Boot 2, mostly due to my familiarity with it
    - Such a simple app would have likely been much faster to write in Python
- The classes are organized in a typical MVC layer fashion, with the native and external models (DTOs)
  being separated for clarity.
- The bulk of the interesting logic is in the service layer (`UserService`), which simply calls the
  2 GitHub API endpoints and merges their information into the response model/DTO.
    - The lone field transformation that was needed is handled in `ModelTransformer`.
      Theoretically, other transformations would go there.
- In order to mitigate issues with rate-limiting, I added simple (memory-based) caching using the
  common Spring caching abstraction. I cache only the final response model we would return from
  UserService, as I imagine both the user details and repositories for a user change infrequently, and
  this avoids reprocessing and saves some space. This would store 1 `UserOverview` object in the
  cache for each unique user, for the duration of the app runtime.
    - With more time, I would have implemented either:
        - memory-based caching with LRU cache eviction and a reasonable cache size (e.g. 1000)
        - multi-layer cache with a disk-based cache allowing even more responses
    - Realistically, these decisions would depend on requirements (if given), or usage metrics
      (if requirements gave uncertain dimensions for transaction frequency and user uniqueness, but we
      have previous API usage data)

## Testing

In the interest of time, I wrote only integration tests for the common scenarios. I believe they
provide far more value than unit tests, especially when the complexity of any single method is so
low, while the number of components/layers is significant enough.

The integration tests leverage:

- MockMvc for request/response checking
- A simple random POJO library for response data generation
- Mockito/@MockBean to intercept REST requests and feed the responses for each scenario

With more time (or had I remembered the big drawbacks of @MockBean and need for @DirtiesContext),
I would have gone straight to handcrafted GitHub JSON responses for each of the tested scenarios
(using WireMock), and have focused on simply testing the edge cases (`created_at` format conversion,
GitHub error responses, etc.). This would have
- Avoided some nasty mocking around RestTemplate
- Have made the tests less brittle by removing component mocking entirely
- Avoided having too much logic in the sunny-day scenario, which increases the risk of bug duplicated in
  both the implementation and test code.

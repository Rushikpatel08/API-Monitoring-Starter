# API Monitoring Starter

API Monitoring Starter is a Spring Boot starter that helps applications expose a lightweight monitoring UI and export API definitions for Bruno, Insomnia, Postman, and OpenAPI.

## Features

- Automatic API monitoring endpoints
- Monitoring UI served from the starter
- Export support for:
  - Bruno
  - Insomnia
  - Postman
  - OpenAPI
- Easy integration into Spring Boot applications

## Requirements

- Java 17+
- Maven 3.8+

## Getting Started

### Build the project

```bash
./mvnw clean install
```

### Run tests

```bash
./mvnw test
```

### Use in a Spring Boot application

Add the starter dependency to your application and enable the monitoring UI through your Spring Boot app configuration.

## Project Structure

- src/main/java - starter implementation
- src/main/resources - static UI assets and auto-configuration metadata
- src/test/java - regression and integration tests

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request.

## Contributors

See [CONTRIBUTORS.md](CONTRIBUTORS.md) for the current maintainers and contributors.

## Repository health

- Stars: ⭐️ help the project grow by starring the repository
- Issues: open an issue for bugs or feature requests
- Pull requests: follow the PR template in [.github/PULL_REQUEST_TEMPLATE.md](.github/PULL_REQUEST_TEMPLATE.md)

## Code of Conduct

Please review [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) before participating in this project.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

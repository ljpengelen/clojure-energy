# NRG

NRG is a small web app to manually filter and sort a fixed list of words.

## Prerequisites

- Java 8 or newer
- Leiningen

## Development mode

To start the Figwheel compiler, navigate to the project folder and run `lein figwheel`.
Figwheel will automatically push changes to the browser.
Once Figwheel starts up, the app is opened in the browser.

## Running tests

To run all tests and watch for changes, execute `lein test-watch`.

### Building for production

To create a production build, execute `lein package`.

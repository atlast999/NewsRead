### 1. Solution
This project, "NewsRead", is an Android application designed to fetch and display news content, it also allows user to download media files and summary the content.
*  **`News List` feature**: Fetches a list of news from a server-less backend
*  **`News Read` feature**: Leverage Android WebView to display news content
*  **`Media Download` feature**: Detects medias in WebView, downloads files via `DownloadManager` system service.
*  **`Content Summary` feature**: Request the content summary from a server-less backend

- The application utilizes a multi-module structure, separating concerns between data handling and UI presentation.
+ Room for local storage
+ Ktor for network request
+ Coroutine for asynchronous processing
+ Compose and Navigation3 for UI implementation
+ Koin for dependency injection
+ Material3 for design system

- The backend is powered by AWS Lambda function (url = https://aws.amazon.com/pm/lambda/), the implementation source is located at: Lambda directory.  
+ Use `cheerio` to parse html web page
+ Use `GoogleGenAI` for content summarization

### 2. Code architect
The project doesn't strictly follows any well known architectures but selectively adjust to match the complexity of the application 
image: docs/app_architecture.png

*   **`data` module**: Responsible for data sources (network and local storage) and data repositories. It handles fetching news data from network, caching in local database and exposing observable data for its consumer.
*   **`presentation` module**: Contains the UI logic and components, such as Activities, Composables, and ViewModels, responsible for displaying UI and delegate app logic to the data layer.
*   **`app` module**: The main application module that integrates the `data` and `presentation` modules, setting up dependency injection and application-level configurations.

This separation promotes maintainability and testability

### 3. Issues
*   **Data Provider**: No provided backend available, I need to implement my own one.
*   **Library Version Mismatch**: Take time to resolve dependencies conflict when using the latest versions
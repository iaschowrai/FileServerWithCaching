# File Server REST API

## Overview

The File Server REST API is a Java-based Spring Boot application that provides endpoints for uploading, downloading, and deleting files. It serves as a simple file storage and retrieval system with caching capabilities for improved performance.

## Purpose

The purpose of this project is to demonstrate how to implement a file server using Spring Boot, providing a RESTful API for managing files. Key features include:

- Uploading files: Clients can upload files to the server, which are then stored on the filesystem and cached in memory for faster access.
- Downloading files: Clients can download files from the server by specifying the filename, retrieving the file content either from the cache or directly from the filesystem.
- Deleting files: Clients can delete files from the server, removing both the file content and associated metadata from the filesystem and cache.

## Features

- **File Upload:** Clients can upload files to the server using the `/api/fileserver/upload` endpoint.
- **File Download:** Clients can download files from the server using the `/api/fileserver/download/{filename}` endpoint.
- **File Deletion:** Clients can delete files from the server using the `/api/fileserver/delete/{filename}` endpoint.
- **Caching:** Files uploaded to the server are cached in memory for faster retrieval, improving overall performance.
- **Asynchronous Initialization:** File metadata and content are loaded into the cache asynchronously during application startup, reducing initialization time.

## Technologies Used

- Java
- Spring Boot
- Spring MVC
- Spring Data JPA
- Lombok
- Postman (for API testing)

## How to Run

1. **Clone the Repository:** Clone the project repository to your local machine.
2. **Build the Project:** Navigate to the project directory and build the project using Maven:
   ```
   mvn clean install
   ```
3. **Run the Application:** Execute the generated JAR file to run the Spring Boot application:
   ```
   java -jar target/file-server-1.0.0.jar
   ```
4. **Access the API:** The API will be accessible at `http://localhost:8080/api/fileserver`. You can now use Postman or any other REST client to interact with the endpoints.

## How to Test with Postman

1. **Download Postman:** If you haven't already, download and install Postman from [https://www.postman.com/downloads/](https://www.postman.com/downloads/).
2. **Import Postman Collection:** Import the provided Postman collection (`File_Server_REST_API.postman_collection.json`) into Postman.
3. **Run Tests:** Run the predefined tests in the collection to interact with the API endpoints and verify their functionality.

---

Feel free to customize the README file further based on your project's specific details and requirements.

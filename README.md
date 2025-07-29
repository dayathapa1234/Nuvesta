# Nuvesta

This repository contains microservices for the Nuvesta project.

## Services

* **gateway-service** - Spring Boot API gateway.
* **auth-server** - Spring Boot authentication server using Spring Session.
* **python-ml-service** - Python microservice for ML tasks and calculations.

## 📦 How to add a New Microservice as a Git Module

To keep all microservices in separate repositories while managing them from this parent project (`Nuvesta`), follow these steps:

1. Open a terminal in the root of the `Nuvesta` project:

   ```bash
   cd path/to/Nuvesta
   ```

2. Add the new submodule:

   ```bash
   git submodule add https://github.com/YOUR_USERNAME/YOUR_REPO.git folder-name
   ```
   Example:
    ```bash
    git submodule add https://github.com/dayathapa1234/new-service.git new-service
    ```  
3. Commit the changes:
   ```bash
   git add .gitmodules folder-name
   git commit -m "Add new-service as a submodule"
   git push
   ```

🔄 Cloning the Project with Submodules

When cloning Nuvesta, use the following to also fetch submodules:
```bash
git clone --recurse-submodules https://github.com/dayathapa1234/Nuvesta.git
 ```
Or, if you’ve already cloned the repo:
```bash
git submodule update --init --recursive
 ```

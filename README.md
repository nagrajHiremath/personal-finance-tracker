# Personal Finance Tracker

A simple Spring Boot application to track personal finance transactions with user authentication and secure API access.

---

## APIs

1. **Register User**  
   - Endpoint to create a new user account.

2. **Login**  
   - Returns an **Auth Token** for secure access.

3. **Transaction APIs**  
   - All transaction-related APIs require the **Auth Token** in the request header.  
   - Example:  
     ```
     Authorization: Bearer <token>
     ```

---

## Database

This app uses **MySQL**. You can run it locally using Docker.

### Start MySQL Container

```bash
docker run -d \
  --name mysql_tracker \
  -e MYSQL_ROOT_PASSWORD=root123 \
  -e MYSQL_DATABASE=finance_tracker \
  -p 3306:3306 \
  -v mysql_data:/var/lib/mysql \
  mysql:8.0
```
### Access MySQL CLI
```
docker exec -it mysql_tracker mysql -uroot -proot123
```


### Postman Collection

Import the Postman collection to test APIs. The collection JSON file is located in the root directory of this project:

**File:** `Finance Tracker API.postman_collection.json`

package com.finance.tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.finance.tracker")
public class FinanceTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceTrackerApplication.class, args);
	}

	//docker run -d --name mysql_tracker -e MYSQL_ROOT_PASSWORD=root123 -e MYSQL_DATABASE=finance_tracker -p 3306:3306 -v mysql_data:/var/lib/mysql mysql:8.0
	//docker exec -it mysql_tracker mysql -uroot -proot123
}

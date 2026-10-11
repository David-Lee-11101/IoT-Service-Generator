package com.${packageName}.mpinterface.controller;

import iotruleservice.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import spring.annotation.RemoteCallReference;
@RestController
public class Controller {

/*@RemoteCallReference
private IUserService userService;

@GetMapping("getUsers")
public Map<String, User> getUsers() {
return userService.getUsers();
}这一段就是需要注入的代码需要模拟的
*/
${javaCode}

}


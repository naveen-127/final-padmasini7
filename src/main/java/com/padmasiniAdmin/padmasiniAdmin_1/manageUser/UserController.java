package com.padmasiniAdmin.padmasiniAdmin_1.manageUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.padmasiniAdmin.padmasiniAdmin_1.service.SignInService;

import jakarta.websocket.Session;

@RestController
public class UserController {
	Map<String, String> map=new HashMap<String, String>();
	@Autowired
	private UserService userService;
	@Autowired 
	private SignInService signInService;
@PostMapping("/newUser")
public ResponseEntity<?> addNewUser(@RequestBody  UserDTO user) {
	System.out.println("frontend connection to backend successful");
	System.out.println(user);
	if(userService.saveNewUser(user)) {
		System.out.println("code excecuted successfully");
		map.put("status", "pass");
	}
	else {
		map.put("status", "failed");
	}
	return ResponseEntity.ok(map);
}
@GetMapping("/getUsers")
public List<UserModel> getUsers(){
	List<UserModel> users=userService.getUsers();
	System.out.println(users);
	return users;
}
@PutMapping("updateUser/{gmail}")
public ResponseEntity<?> updateUser(@RequestBody UserDTO user,@PathVariable("gmail") String gmail) {
	  System.out.println("Deleting old user: " + gmail + ", adding new user: " + user.getUser().getGmail());
	userService.deleteUser(gmail);
	userService.saveNewUser(user);
	map.put("status", "pass");
	return ResponseEntity.ok(map);
}
@DeleteMapping("deleteUser/{gmail}")
public ResponseEntity<?> deleteUser(@PathVariable("gmail") String gmail) {
	System.out.println(gmail);
	userService.deleteUser(gmail);
	map.put("status", "pass");
	return ResponseEntity.ok(map);
}

}

package com.videogame.example.first.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.videogame.example.first.model.Client;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

@RestController
public class SubscriptionController {
	
	private List<Client> clients = new ArrayList<>();

	@GetMapping("/")
	public ResponseEntity<String> getHome() {
		String welcome = "Welcome to VideoGame Store!\nSubmit your request adding path /add/monthlysubscription or /add/annualsubscription\nWith name and surname of the new subscriber.";
		return new ResponseEntity<>(welcome, HttpStatus.OK);
	}
	
	@GetMapping("/registered") 
	public List<Client> getClients() {
		return clients;
	}

	@GetMapping("/health")
    	public ResponseEntity<String> checkStatus(HttpServletRequest request) {
	        RestTemplate restTemplate = new RestTemplate();
	        String url = request.getRequestURL().toString();
	        try {
	            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
	            if (response.getStatusCodeValue() == 200) {
	                return new ResponseEntity<>("Service is up", HttpStatus.OK);
	            } else {
	                return new ResponseEntity<>("Service is down", HttpStatus.SERVICE_UNAVAILABLE);
	            }
	        } catch (HttpStatusCodeException ex) {
	            return new ResponseEntity<>("Error occurred", HttpStatus.SERVICE_UNAVAILABLE);
	        }
    	}
	
	@PostMapping("/add/monthlysubscription")
	public Map<String, Client> addMontlyClientSubscription(@Validated @RequestBody Client client) {
		Map<String, Client> mapResponse = new HashMap<>();
		client.setTypeSubscription("Monthly");
		if (client.getName() == null || client.getSurname() == null) {
			mapResponse.put("Client not Added, maybe is empty. Try Again.\n", client);
		} else { 
			mapResponse.put("Client Successful Added", client);
			clients.add(client);
		}
		return mapResponse;
	}
	
	@PostMapping("/add/annualsubscription")
	public Map<String, Client> addAnnualClientSubscription(@Validated @RequestBody Client client) {
		Map<String, Client> mapResponse = new HashMap<>();
		client.setTypeSubscription("Annual");
		if (client.getName() == null || client.getSurname() == null) {
			mapResponse.put("Client not Added, maybe is empty. Try Again.\n", client);
		} else { 
			mapResponse.put("Client Successful Added", client);
			clients.add(client);
		}
		return mapResponse;
	}
	
}

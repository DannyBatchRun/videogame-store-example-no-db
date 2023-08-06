package com.videogame.example.first.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.videogame.example.first.model.Client;

import java.util.*;

@RestController
public class SubscriptionController {
	
	private List<Client> clients = new ArrayList<>();
	
	@GetMapping("/registered") 
	public List<Client> getClients() {
		return clients;
	}
	
	@PostMapping("/add/montlysubscription")
	public Map<String, Client> addMontlyClientSubscription(@Validated @RequestBody Client client) {
		Map<String, Client> mapResponse = new HashMap<>();
		client.setTypeSubscription("Montly");
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

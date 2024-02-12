package com.videogame.example.third.controller;

import java.util.*;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.videogame.example.third.model.*;

@RestController
public class VideogameController {
    
    private List<Client> clients = new ArrayList<>();
    private List<Videogame> videogames = new ArrayList<>();

    @GetMapping("/") 
    public String getHome() {
        return "Welcome to Videogame Store! Please use /syncronize for syncronize all databases\nAnd then, you can add a videogame to cart's client with /add/cart.\nIf you want to check total price, please use /allcarts endpoint.";
    }

    @GetMapping("/health")
    public String getHealth() {
        return "Service is up and running";
    }

    @GetMapping("/synchronize")
    public String synchronizeAll() {
        RestTemplate restTemplate = new RestTemplate();
        Videogame[] videogameArray = restTemplate.getForObject("http://localhost:8100/videogames", Videogame[].class);
        if (videogameArray != null) {
            List<Videogame> videogameList = Arrays.asList(videogameArray);
            videogames.addAll(videogameList);
        }
        Client[] clientArray = restTemplate.getForObject("http://localhost:8081/registered", Client[].class);
        if (clientArray != null) {
            List<Client> clientList = Arrays.asList(clientArray);
            clients.addAll(clientList);
        }
        return "Data synchronized successfully!";
    }

    @PostMapping("/add/cart")
    public Map<String, Client> addVideogameToCart(@Validated @RequestBody String videogameName, @Validated @RequestBody String clientName, @Validated @RequestBody String clientSurname) {
        Map<String, Client> responseMap = new HashMap<>();
        for (Videogame videogame : videogames) {
            if (videogame.getName().equals(videogameName)) {
                for (Client client : clients) {
                    if (client.getName().equals(clientName) && client.getSurname().equals(clientSurname)) {
                        client.getVideogames().add(videogame);
                        responseMap.put("Success", client);
                        return responseMap;
                    }
                }
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found");
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Videogame not found");
    }

    @GetMapping("/allcarts")
    public Map<String, Double> getTotalPricePerClient() {
        Map<String, Double> totalPricePerClient = new HashMap<>();
        for (Client client : clients) {
            double total = 0;
            for (Videogame videogame : client.getVideogames()) {
                total += videogame.getPrice();
            }
            totalPricePerClient.put(client.getName() + " " + client.getSurname(), total);
        }
        return totalPricePerClient;
    }
}

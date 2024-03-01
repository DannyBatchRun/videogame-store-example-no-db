package com.videogame.example.third.controller;

import java.util.*;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.videogame.example.third.model.*;

@RestController
public class VideogameController {
    
    private List<Client> clients = new ArrayList<>();
    private List<Videogame> videogames = new ArrayList<>();

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/") 
    public String getHome() {
        return "Welcome to Videogame Store! Please use /syncronize for syncronize all databases\nAnd then, you can add a videogame to cart's client with /add/cart.\nIf you want to check total price, please use /allcarts endpoint.";
    }

    @GetMapping("/health")
    public String getHealth() {
        return "Service is up and running";
    }

    @GetMapping("/videogames")
    public List<Videogame> getVideogamesSynched() {
        return videogames;
    }

    @GetMapping("/buyers")
    public List<Client> getBuyersWithItsCart() {
        return clients;
    }

    @GetMapping("/synchronize")
    public String synchronizeAll() {
        String urlVideogames = getServiceUrl("videogameproducts");
        String urlSubscription = getServiceUrl("usersubscription");
        if (url == null) {
            return "Failed to get service URL!";
        }
        Videogame[] videogameArray = restTemplate.getForObject(urlVideogames + "/videogames", Videogame[].class);
        Client[] clientArray = restTemplate.getForObject(urlSubscription + "/registered", Client[].class);
                if (videogameArray != null) {
            for (Videogame newVideogame : videogameArray) {
                boolean exists = false;
                for (Videogame existingVideogame : videogames) {
                    if (existingVideogame.getIdProduct() == newVideogame.getIdProduct()) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    videogames.add(newVideogame);
                }
            }
        }   
        if (clientArray != null) {
            for (Client newClient : clientArray) {
                boolean exists = false;
                for (Client existingClient : clients) {
                    if (existingClient.getName().equals(newClient.getName()) && existingClient.getSurname().equals(newClient.getSurname())) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    clients.add(newClient);
                }
            }
        }
        return "Data synchronized successfully!";   
    }

    private String getServiceUrl(String serviceName) {
        try {
            Process process = Runtime.getRuntime().exec("minikube service " + serviceName + " --url");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return reader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @PostMapping("/add/cart")
    public Map<String, Client> addVideogameToCart(@Validated @RequestBody Map<String, String> requestBody) {
        String videogameName = requestBody.get("videogameName");
        String clientName = requestBody.get("clientName");
        String clientSurname = requestBody.get("clientSurname");
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

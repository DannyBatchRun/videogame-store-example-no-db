const { Given, When, Then } = require('@cucumber/cucumber');
const axios = require('axios');
const pactum = require('pactum');
const assert = require('assert').strict;

let apiEndpoint;
let response;

Given('I set GET service api endpoint', function () {
  apiEndpoint = 'http://localhost:8081/registered';
});

Then('send a GET HTTP request', async function () {
    response = await axios.get(apiEndpoint);
});

Then('check if the list of videogames is not empty', async function () {
  console.log(response);
  if (typeof response == 'undefined') {
    throw new Error("List of videogames is empty.");
  }
});

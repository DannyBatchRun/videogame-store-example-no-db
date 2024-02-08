const { Given, When, Then, setDefaultTimeout } = require('@cucumber/cucumber');
const axios = require('axios');
const pactum = require('pactum');
const assert = require('assert').strict;

let apiEndpoint;
let requestBody;
let response;

setDefaultTimeout(60 * 1000);

Given('I set DELETE service api endpoint with id {string}', function (id) {
  idProduct = id;
  apiEndpoint = `http://localhost:8100/remove/${idProduct}`;
});

Then('send a DELETE HTTP request', {timeout: 120 * 1000}, async function () {
  response = await axios.post(apiEndpoint, requestBody);
  console.log(response);
});

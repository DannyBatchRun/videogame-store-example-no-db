const { Given, When, Then } = require('@cucumber/cucumber');
const axios = require('axios');
const pactum = require('pactum');
const assert = require('assert').strict;

let apiEndpoint;
let requestBody;
let response;

Given('I set POST service api endpoint', function () {
  apiEndpoint = 'http://localhost:8081/add/monthlysubscription';
});

When('set request BODY with following details:', function (dataTable) {
  requestBody = dataTable.rowsHash();
});

Then('send a POST HTTP request', async function () {
  response = await axios.post(apiEndpoint, requestBody);
});

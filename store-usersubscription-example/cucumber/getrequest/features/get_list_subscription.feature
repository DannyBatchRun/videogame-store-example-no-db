Feature: Get Request Feature

  Scenario: Making a GET request
    Given I set GET service api endpoint
    Then send a GET HTTP request
    And check if list of subscribers is not empty

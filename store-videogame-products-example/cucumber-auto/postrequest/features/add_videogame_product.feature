Feature: Video Game Product API

  Scenario Outline: Making a POST request for different video games
    Given I set POST service api endpoint
    And set request BODY with following details:
      | idProduct    | <idProduct>  |
      | name   | <name> |
      | type   | <type> |
      | price  | <price> |
    Then send a POST HTTP request

  Examples:
    | idProduct | name           | type        | price |
    | 1         | "Super Mario"  | "Platform"  | 59.99 |
    | 2         | "Zelda"        | "Adventure" | 59.99 |
    | 3         | "Minecraft"    | "Sandbox"   | 29.99 |
    | 4         | "Fortnite"     | "Shooter"   | 0.00  |
    | 5         | "Among Us"     | "Strategy"  | 4.99  |
    | 6         | "FIFA 2024"    | "Sports"    | 59.99 |
    | 7         | "Cyberpunk"    | "RPG"       | 59.99 |
    | 8         | "Overwatch"    | "Shooter"   | 39.99 |
    | 9         | "Rocket League"| "Sports"    | 19.99 |
    | 10        | "Fall Guys"    | "Platform"  | 19.99 |

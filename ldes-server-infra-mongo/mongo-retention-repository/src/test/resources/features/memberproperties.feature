Feature: MemberRepository
  As a user
  I want to interact with the MemberRepository to save and retrieve MemberProperties

  Scenario: Saving a MemberProperties without view
    Given The following MemberProperties
      | id                                      | collectionName      | versionOf                             | timestamp               | viewReference |
      | http://test-data/mobility-hindrances/1/1 | mobility-hindrances | http://test-data/mobility-hindrances/1 | 2023-07-05T15:28:49.665 | by-page |
    When I save the MemberProperties without view using the MemberPropertiesRepository
    And I retrieve the MemberProperties with id "http://test-data/mobility-hindrances/1/1"
    Then I have retrieved 1 MemberProperties
    And The retrieved MemberProperties contains MemberProperties 1 of the table
    And The retrieved MemberProperties does not have the view "by-page" as a property

  Scenario: Saving a MemberProperties with view
    Given The following MemberProperties
      | id                                      | collectionName      | versionOf                             | timestamp               | viewReference |
      | http://test-data/mobility-hindrances/1/1 | mobility-hindrances | http://test-data/mobility-hindrances/1 | 2023-07-05T15:28:49.665 | by-page |
    When I save the MemberProperties using the MemberPropertiesRepository
    And I retrieve the MemberProperties with id "http://test-data/mobility-hindrances/1/1"
    Then I have retrieved 1 MemberProperties
    And The retrieved MemberProperties contains MemberProperties 1 of the table

  Scenario: Retrieving MemberProperties with same VersionOf
    Given The following MemberProperties
      | id                                      | collectionName      | versionOf                             | timestamp               | viewReference |
      | http://test-data/mobility-hindrances/1/1 | mobility-hindrances | http://test-data/mobility-hindrances/1 | 2023-07-05T15:28:49.665 | by-page |
      | http://test-data/mobility-hindrances/1/2 | mobility-hindrances | http://test-data/mobility-hindrances/1 | 2023-07-05T15:28:49.665 | by-page |
      | http://test-data/mobility-hindrances/2/1 | mobility-hindrances | http://test-data/mobility-hindrances/2 | 2023-07-05T15:28:49.665 | by-page |
      | http://test-data/mobility-hindrances/1/3 | mobility-hindrances | http://test-data/mobility-hindrances/1 | 2023-07-05T15:28:49.665 | by-page |
    When I save the MemberProperties using the MemberPropertiesRepository
    And I retrieve all MemberProperties with versionOf "http://test-data/mobility-hindrances/1" from view "by-page"
    Then I have retrieved 3 MemberProperties
    And The retrieved MemberProperties contains MemberProperties 1 of the table
    And The retrieved MemberProperties contains MemberProperties 2 of the table
    And The retrieved MemberProperties contains MemberProperties 4 of the table

  Scenario: Deleting a MemberProperties
    Given The following MemberProperties
      | id                                      | collectionName      | versionOf                             | timestamp               |
      | http://test-data/mobility-hindrances/1/1 | mobility-hindrances | http://test-data/mobility-hindrances/1 | 2023-07-05T15:28:49.665 |
    When I save the MemberProperties using the MemberPropertiesRepository
    And I retrieve the MemberProperties with id "http://test-data/mobility-hindrances/1/1"
    Then I have retrieved 1 MemberProperties
    And The retrieved MemberProperties contains MemberProperties 1 of the table
    And I delete the MemberProperties with id "http://test-data/mobility-hindrances/1/1"
    And I retrieve the MemberProperties with id "http://test-data/mobility-hindrances/1/1"
    Then I have retrieved 0 MemberProperties

  Scenario: Removing a eventStream to MemberProperties
    Given The following MemberProperties
      | id                                      | collectionName      | versionOf                             | timestamp               |
      | http://test-data/mobility-hindrances/1/1 | mobility-hindrances | http://test-data/mobility-hindrances/1 | 2023-07-05T15:28:49.665 |
    When I save the MemberProperties using the MemberPropertiesRepository
    And I retrieve the MemberProperties with id "http://test-data/mobility-hindrances/1/1"
    Then I have retrieved 1 MemberProperties
    And The retrieved MemberProperties contains MemberProperties 1 of the table
    And I remove the eventStream with name "mobility-hindrances"
    And I retrieve the MemberProperties with id "http://test-data/mobility-hindrances/1/1"
    Then I have retrieved 0 MemberProperties
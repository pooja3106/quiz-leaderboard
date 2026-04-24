# Quiz Leaderboard System

## Overview
This project fetches quiz event data from an API, processes it, removes duplicates, calculates scores, and submits a leaderboard.

## Features
- Polls API multiple times
- Handles invalid server responses
- Deduplicates entries
- Calculates total scores per participant
- Sorts leaderboard (descending)
- Submits final result to server

##Tech Stack
- Java (JDK 11+)
- Maven
- Jackson (JSON parsing)
- HTTP Client API

## Project Structure
```
quiz-leaderboard
│── src/main/java/QuizLeaderboard.java
│── pom.xml
```

## How to Run
```bash
mvn clean install
mvn exec:java
```

## Notes
- API may return "no available server", handled in code
- Polling delay: 5 seconds (as required)

## Sample Output
```
{"regNo":"RA2311003020822","totalPollsMade":20,"submittedTotal":835}
```

## Author
Pooja

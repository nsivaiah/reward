# Reward Points Calculation – Spring Boot Application

This project demonstrates a simple **Reward Program** for a retailer using **Spring Boot**, without using any database.  
All data (customers & transactions) is stored **in-memory**, making it easy to run and test.

---

## 📌 Problem Statement

A retailer rewards customers based on their purchases:

- **2 points** for every dollar spent **over $100**  
- **1 point** for every dollar spent **between $50 and $100**

**Example:**  
A purchase of **$120**  
→ 2 × (120 − 100) = **40 points**  
→ 1 × (100 − 50) = **50 points**  
→ **Total = 90 points**

Given all transactions for a **three-month period**, calculate:

- Reward points **per month**
- **Total reward points** for the customer
- Include transaction-level reward breakdown

The system should calculate rewards dynamically for any date range.

---

## 🏗️ Features

✔ Spring Boot REST API  
✔ No database — in-memory data  
✔ Calculates:
- Monthly reward points
- Total reward points
- Reward points per transaction  
✔ Returns complete customer + transaction details  
✔ Simple, clean, and scalable design

---

## 📡 REST Endpoint
http://localhost:8080/rewards/{customerId}?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
Example:
http://localhost:8080/rewards/1?startDate=2025-01-01&endDate=2025-03-31


### Response Includes:
- Customer details  
- Total reward points  
- Monthly reward points  
- Detailed transactions with computed points  

## 📁 Project Structure 
src/main/java/com.offer/
│
├── controller/
│ └── RewardController.java
│
├── service/
│ └── RewardService.java
│
├── model/
│ ├── Customer.java
│ └── Transaction.java
│
├── repository/
│ └── TransactionRepository.java (in-memory dataset)
│
└── dto/
├── RewardSummary.java
└── TransactionWithPoints.java


## 🧮 Reward Calculation Logic

if amount > 100:
points += (amount - 100) * 2
if amount > 50:
points += (amount - 50)

## 🛠️ Technologies Used

- Java 17  
- Spring Boot 3.3.0
- Maven  
- SLF4J Logging  
- In-memory repository

  ## 📞 Example Response (Simplified)
{
    "customerId": "1",
    "customerName": "Neelima Sivaiahgari",
    "customerMail": "neelima@gmail.com",
    "pointsPerMonth": {
        "JANUARY": 90,
        "MARCH": 250,
        "FEBRUARY": 25
    },
    "totalPoints": 365,
    "transactions":  [
    
        {
           "transactionId": "486d5400-e155-4fab-8ccd-46afc25941d7",
            "date": "2025-01-10",
            "amount": 120,
            "points": 90
        },
        {
            "transactionId": "6db68bad-e981-4f9e-a4aa-5830eac34e6e",
            "date": "2025-02-05",
            "amount": 75,
            "points": 25
        },
        {
            "transactionId": "bae96c21-d97f-46b8-b4dc-3629ca4f681b",
            "date": "2025-03-12",
            "amount": 200,
            "points": 250
        }
        
   ]
}








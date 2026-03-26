# 🚀 PermGuard

### A Web-Based Real-Time Student Permission Management & Fraud Detection System

---

## 📌 Overview

PermGuard is a full-stack web application designed to digitize and secure student permission workflows in educational institutions. It replaces traditional paper-based systems with a **QR-based access system**, combined with **real-time analytics and fraud detection**.

The system enables students to request permissions online, allows faculty to approve/reject them, and helps security verify access using QR codes — all while monitoring suspicious behavior using machine learning.

---

## 🎯 Key Features

* 📄 **Digital Permission Requests**

  * Students can apply for permissions online
  * Eliminates paper-based approvals

* ✅ **Faculty Approval System**

  * Easy approve/reject workflow
  * Real-time status updates

* 🔐 **QR Code-Based Access**

  * Auto-generated temporary QR codes
  * One-time use & expiry validation

* 🛡️ **Fraud Detection (ML)**

  * Uses Isolation Forest algorithm
  * Detects abnormal behavior patterns

* 📊 **Admin Dashboard**

  * View analytics & risk scores
  * Monitor suspicious activities

* 🎥 **Gate Scanning System**

  * QR scanning using camera
  * Entry/exit tracking

* ⚡ **Real-Time Processing**

  * Kafka-based event streaming
  * Instant alert generation

---

## 🏗️ Tech Stack

### Frontend

* React.js (Vite)
* Recharts (Data Visualization)

### Backend

* Spring Boot
* REST APIs
* JWT Authentication

### Database

* MySQL

### Other Technologies

* Apache Kafka (Event Streaming)
* Isolation Forest (ML Algorithm)
* BCrypt (Password Hashing)
* HMAC-SHA256 (QR Token Security)

---

## 🧱 System Architecture

* **Presentation Layer** → React UI (Student, Faculty, Admin, Security dashboards)
* **Application Layer** → Spring Boot backend (modular services)
* **Data Layer** → MySQL database + Kafka streams

---

## 🔄 Workflow

1. Student submits permission request
2. Faculty reviews & approves/rejects
3. QR code is generated upon approval
4. Security scans QR at entry/exit
5. System logs activity
6. ML module detects anomalies
7. Admin receives alerts if suspicious behavior is found

---

## 📊 Performance

* ⚡ Average API Response Time: ~380 ms
* 🔍 QR Validation Time: < 0.5 sec
* 🤖 Fraud Detection Accuracy: ~94%
* 👥 Tested with 50 concurrent users

---

## 🔐 Security Features

* JWT-based authentication
* Role-based access control
* QR token integrity using HMAC-SHA256
* Password hashing with BCrypt
* Secure communication via HTTPS

---

## 📁 Project Structure

```
permguard/
│
├── frontend/        # React application
├── backend/         # Spring Boot APIs
├── database/        # MySQL schema
├── ml-module/       # Fraud detection logic
└── README.md
```

---

## ⚙️ Installation & Setup

### 1. Clone the repository

```
git clone https://github.com/your-username/permguard.git
cd permguard
```

### 2. Setup Backend

```
cd backend
mvn clean install
mvn spring-boot:run
```

### 3. Setup Frontend

```
cd frontend
npm install
npm run dev
```

### 4. Configure Database

* Create MySQL database
* Update application.properties with DB credentials

---

## 🌐 Future Enhancements

* 📱 Mobile application
* 🔔 Push notifications
* 📷 Face recognition verification
* 📈 Advanced ML models (supervised learning)
* 🏫 Integration with university ERP systems

---

## 👨‍💻 Authors

* K. Abyudhay
* K. Mallikarjunarao
* M. Kavya
* K. Nani

---

## 📜 License

This project is developed for academic and research purposes.

---

## ⭐ Support

If you like this project, give it a ⭐ on GitHub!

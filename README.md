# PathMaster – Android App for Scientific Discovery

**PathMaster** is a mobile application designed to simplify the exploration, consultation, and management of scientific research articles. Developed using **Android Studio (Jetpack Compose)** and integrated with **Firebase**, it offers users a sleek and intuitive way to browse, organize, and favorite research content, particularly in the fields of computer science and AI.

---

## Team Members 

- Charles Radolanirina
- Éloïse Baldet
- Martin Gatbois
- Killian Perrot
  
## Overview

PathMaster allows users to:

- Explore categorized scientific articles (from platforms : **arXiv**, **IEEE Xplore**, **Scopus** and **Google Scholar**).
- Add articles to a favorites list with a **drag-and-drop gesture**.
- Access additional information such as abstracts, PDF links, and publication sources.
- Receive real-time notifications via **Firebase** when interacting with content.
- Authenticate with **email/password** or **Google Sign-In**.
- View and edit their **profile**, including username and profile picture.
- Analyze graphics on several points like the number of articles added in favorite for each categories.. 

---

## Article Collection – External Scraper

The research articles featured in the app are **not manually added**. Instead, they are dynamically collected using a Python web scraper developed in a separate repository:

➡ **[PathMaster Scraper (Python Repo)](https://github.com/CharlesRado/PathMaster_Scraper.git)**

This Python script scrapes data from four academic sources, structures it, and feeds it into **Firebase Firestore** using GitHub Actions. The mobile app retrieves this data in real time.

---

## Tech Stack

| Layer           | Technology                       |
|----------------|-----------------------------------|
| Mobile UI      | Jetpack Compose (Android Studio)  |
| Backend/Auth   | Firebase Authentication & Firestore |
| Notifications  | Firebase Cloud Messaging (FCM)    |
| Article Scraping | Python + GitHub Actions |
| UI Assets      | Custom vector icons & Material 3  |

---

## Features Highlight

- **Smooth Navigation** with modern Material UI and top/bottom bars.
- **Article Drag & Drop** to favorites using Compose gestures.
- **Real-Time Updates** via Firestore syncing and state flows.
- **Profile Management** with editable user data and profile pictures (Base64).
- **Secure Auth** through Firebase + Google OAuth2.
- **Statistics Overview** with animations. 

---

## 🧑‍🤝‍🧑 Team & Contributions

|  Project Component  |                                 Description                                  |  CR  |  EB  |  MG  |  KP  |
|---------------------|------------------------------------------------------------------------------|------|------|------|------|
| Documentation       | Documenting the code and project progress                                    |  30  |  40  |  20  |  10  |
| Code                | Contributing code for the tasks of the project                               |  80  |   0  |  20  |   0  |
| Communication       | Communication with stakeholders and facilitator role within the team         |  30  |  30  |  30  |  10  |
| Research            | Module-specific research to direct the development                           |  55  |  20  |  20  |   5  |
| Presentation        | Presentation of the project results                                          |  25  |  40  |  25  |   5  |
| Management          | Planning individual work as well as collaboration within the assigned module |  25  |  35  |  30  |  10  |
| Overall             | Involvement optimality score                                                 | 1.63 | 1.10 | 0.96 | 0.26 | 

## How to Run 

1. Clone this repository
2. Open in Android Studio (Bumblebee or later).
3. Configure your google-services.json for Firebase integration.
4. Build and run on an Android emulator or device (API 28+).

⚠️ Make sure you also configure Firestore rules and enable authentication providers in your Firebase project.

## Licence

MIT

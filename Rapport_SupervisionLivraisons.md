# Rapport de Projet
## Application Mobile — Supervision des Livraisons

---

**Module :** Développement Mobile  
**Filière :** 2ème année Ingénierie Informatique  
**Année universitaire :** 2025 – 2026  
**Référence projet :** Projet_Supervision_Livraison  

---

## Table des matières

1. [Description du projet](#1-description-du-projet)
2. [Diagramme de cas d'utilisation](#2-diagramme-de-cas-dutilisation)
3. [Diagramme de classes](#3-diagramme-de-classes)
4. [Base de données et API de connexion](#4-base-de-données-et-api-de-connexion)
5. [Arbre des interfaces](#5-arbre-des-interfaces)
6. [Outils utilisés et difficultés rencontrées](#6-outils-utilisés-et-difficultés-rencontrées)
7. [Les interfaces](#7-les-interfaces)
8. [Annexe — Code des parties importantes](#8-annexe--code-des-parties-importantes)

---

## 1. Description du projet

### 1.1 Contexte général

Dans un contexte logistique où la réactivité et la traçabilité des livraisons sont essentielles, ce projet propose une **application mobile Android** dédiée à la **supervision et au suivi en temps réel des livraisons**.  
L'application répond au cahier des charges académique *Projet_Supervision_Livraison (AU 2025-2026, 2ING INFO)* et vise à remplacer les processus manuels de suivi par une solution numérique centralisée.

### 1.2 Objectifs

- Permettre au **contrôleur** de suivre l'ensemble des livraisons, de filtrer/trier les données et de consulter un tableau de bord synthétique.
- Permettre au **livreur** de gérer ses tournées quotidiennes : consultation, mise à jour d'état et communication d'urgence.
- Assurer une **communication temps réel** bidirectionnelle entre contrôleurs et livreurs via messagerie instantanée.
- Garantir un fonctionnement **offline-first** côté livreur (livraisons mises en cache local, re-synchronisation automatique).

### 1.3 Périmètre fonctionnel

L'application est accessible à deux types d'utilisateurs :

#### Fonctionnalités Contrôleur

| Fonctionnalité | Détail |
|---|---|
| Liste totale des livraisons | Sur une période donnée, avec état, livreur, date, numéro et montant de la commande |
| Recherche de livraisons | Par date de livraison ou par livreur |
| Livraisons du jour | Consultation et tri par état, livreur, client ou numéro de commande |
| Tableau de bord | Nombre de livraisons par livreur × état ; par client × état |
| Messagerie temps réel | Envoi de messages d'information aux livreurs en tournée |

#### Fonctionnalités Livreur

| Fonctionnalité | Détail |
|---|---|
| Liste du jour | Ordre, numéro de commande, nom client, téléphone, ville |
| Détail d'une livraison | Contact client, adresse + lien Google Maps, articles, montant, mode de paiement |
| Modification d'état | EN_ATTENTE → EN_COURS → LIVREE / NON_LIVREE (avec remarque obligatoire si non livrée) |
| Messagerie d'urgence | Envoi au contrôleur avec contact client et numéro de commande automatiquement joints |

### 1.4 Architecture technique

L'application suit une architecture **3-tiers** :

```
┌─────────────────────────────┐         ┌──────────────────────────┐
│   Android (Java, MVVM)      │  HTTP   │   ExpressJS (REST + WS)  │
│                             │ <─────> │   Architecture en couches│
│  View → ViewModel →         │  WS     │   routes / controllers / │
│  Repository → Retrofit /    │         │   services / config      │
│  Room (cache offline)       │         └────────────┬─────────────┘
└─────────────────────────────┘                      │ pg (pool)
                                                     ▼
                                           ┌──────────────────┐
                                           │  PostgreSQL 16   │
                                           │  (Docker volume) │
                                           └──────────────────┘
```

- **Couche présentation** : Android Studio (Java), patron MVVM, ViewBinding, Material Design 3.
- **Couche métier / API** : Node.js + ExpressJS 4, authentification JWT, messagerie temps réel via Socket.IO.
- **Couche données** : PostgreSQL 16 (schéma `BDG_LivraisonCom_25`), Room (cache local Android).
- **Infrastructure** : Docker Compose orchestrant le service `api` et le service `db`.

---

## 2. Diagramme de cas d'utilisation

```
┌────────────────────────────────────────────────────────────────────┐
│                  Système : SupervisionLivraisons                   │
│                                                                    │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │                      <<include>>                            │  │
│  │  (S'authentifier) ◄────────── Tous les cas d'utilisation   │  │
│  └─────────────────────────────────────────────────────────────┘  │
│                                                                    │
│  ╔══════════╗   ──── Consulter liste livraisons (période)          │
│  ║          ║   ──── Rechercher livraisons (date / livreur)        │
│  ║Contrôleur║   ──── Consulter livraisons du jour (trier)          │
│  ║          ║   ──── Consulter tableau de bord                     │
│  ╚══════════╝   ──── Envoyer message INFO au livreur               │
│                 ──── Consulter messagerie                          │
│                                                                    │
│  ╔═════════╗    ──── Consulter ses livraisons du jour              │
│  ║         ║    ──── Consulter détail d'une livraison              │
│  ║ Livreur ║    ──── Modifier état d'une livraison                 │
│  ║         ║    ──── Envoyer message URGENCE au contrôleur         │
│  ╚═════════╝    ──── Consulter messagerie                          │
│                                                                    │
└────────────────────────────────────────────────────────────────────┘
```

### 2.1 Description des cas d'utilisation principaux

**UC1 — S'authentifier**
- Acteur : Contrôleur, Livreur
- Précondition : L'utilisateur dispose d'un login/mot de passe valide
- Scénario nominal : saisie des identifiants → vérification JWT → redirection selon le rôle
- Postcondition : token JWT stocké en session locale

**UC2 — Consulter liste des livraisons (Contrôleur)**
- Acteur : Contrôleur
- Précondition : authentifié
- Paramètres de filtre : dateFrom, dateTo, état, livreur, client, numéro commande
- Scénario nominal : saisie des critères → appel `GET /api/deliveries` → affichage de la liste triée par date décroissante

**UC3 — Modifier l'état d'une livraison (Livreur)**
- Acteur : Livreur
- Précondition : authentifié, livraison appartient au livreur courant
- Contrainte : si l'état est `NON_LIVREE`, une remarque est obligatoire
- Postcondition : état mis à jour en base + notification temps réel envoyée aux contrôleurs connectés

**UC4 — Messagerie temps réel**
- Acteur : Contrôleur (INFO), Livreur (URGENCE)
- Scénario : message posté via REST → persisté en base → diffusé via Socket.IO à la salle cible (`role:Livreur` ou `role:Controleur`)

---

## 3. Diagramme de classes

### 3.1 Modèle de données (classes métier)

```
┌────────────────────┐       ┌──────────────────────┐
│     Personnel      │       │        Postes        │
├────────────────────┤       ├──────────────────────┤
│ idpers : int (PK)  │N─────1│ codeposte : int (PK) │
│ nompers : String   │       │ libelle : String     │
│ prenompers : String│       │ indice : int         │
│ adrpers : String   │       └──────────────────────┘
│ villepers : String │
│ telpers : String   │
│ d_embauche : Date  │
│ login : String     │
│ motP : String      │
│ codeposte : int(FK)│
└────────────────────┘
         │1
         │ (livreur)
         │N
┌────────────────────┐       ┌──────────────────────┐
│  LivraisonCom      │       │      Commandes       │
├────────────────────┤       ├──────────────────────┤
│ nocde : int (PK,FK)│N─────1│ nocde : int (PK)     │
│ dateliv : Date     │       │ noclt : int (FK)     │
│ livreur : int (FK) │       │ datecde : Date       │
│ modepay : String   │       │ etatcde : String     │
│ etatliv : String   │       └──────────────────────┘
│ remarque : Text    │                │1
└────────────────────┘                │N
                               ┌──────────────────────┐
┌────────────────────┐         │       LigCdes        │
│      Clients       │         ├──────────────────────┤
├────────────────────┤         │ nocde : int (FK,PK)  │
│ noclt : int (PK)   │1────N─┐ │ refart : int (FK,PK) │
│ nomclt : String    │       │ │ qtecde : int         │
│ prenomclt : String │       │ └──────────────────────┘
│ adrclt : String    │       │          │N
│ villeclt : String  │       │          │1
│ code_postal : String│      │  ┌──────────────────────┐
│ telclt : String    │       │  │       Articles       │
│ adrmail : String   │       │  ├──────────────────────┤
└────────────────────┘       │  │ refart : int (PK)    │
         ▲                   │  │ designation : String │
         └───────────────────┘  │ prixA : Numeric      │
                                │ prixV : Numeric      │
                                │ codetva : String     │
                                │ categorie : String   │
                                │ qtestk : int         │
                                └──────────────────────┘

┌────────────────────────────────────────┐
│               Messages                 │
├────────────────────────────────────────┤
│ id : int (PK)                          │
│ sender_id : int (FK → Personnel)       │
│ receiver_id : int (FK → Personnel, ?)  │
│ nocde : int (FK → Commandes, ?)        │
│ type : String  ['INFO' | 'URGENCE']    │
│ content : Text                         │
│ created_at : Timestamp                 │
│ is_read : Boolean                      │
└────────────────────────────────────────┘
```

### 3.2 Classes Android (couche data)

```
┌──────────────────────┐     ┌──────────────────────────┐
│      Delivery        │     │      DeliveryDetail      │
├──────────────────────┤     ├──────────────────────────┤
│ nocde : int          │     │ nocde : int              │
│ dateliv : String     │     │ dateliv : String         │
│ etatliv : String     │     │ etatliv : String         │
│ modepay : String     │     │ modepay : String         │
│ livreur_id : int     │     │ remarque : String        │
│ livreur_nom : String │     │ nomclt : String          │
│ noclt : int          │     │ adrclt : String          │
│ client_nom : String  │     │ villeclt : String        │
│ villeclt : String    │     │ telclt : String          │
│ telclt : String      │     │ maps_url : String        │
│ montant : double     │     │ articles : List<Article> │
│ ordre : int          │     │ nb_articles : int        │
└──────────────────────┘     │ montant : double         │
                             └──────────────────────────┘

┌──────────────────┐     ┌──────────────────────┐
│     Message      │     │    StatusUpdate      │
├──────────────────┤     ├──────────────────────┤
│ id : int         │     │ etatliv : String     │
│ sender_id : int  │     │ remarque : String    │
│ receiver_id : int│     └──────────────────────┘
│ nocde : int      │
│ type : String    │     ┌──────────────────────┐
│ content : String │     │      NamedRef        │
│ created_at :String│    ├──────────────────────┤
│ sender_nom:String│     │ id : int             │
└──────────────────┘     │ nom : String         │
                         └──────────────────────┘
```

---

## 4. Base de données et API de connexion

### 4.1 Schéma de la base de données `BDG_LivraisonCom_25`

```sql
Postes     (codeposte PK, libelle, indice)
Personnel  (idpers PK, nompers, prenompers, adrpers, villepers, telpers,
            d_embauche, login UNIQUE, motP, codeposte FK→Postes)
Articles   (refart PK, designation, prixA, prixV, codetva, categorie, qtestk)
Clients    (noclt PK, nomclt, prenomclt, adrclt, villeclt, code_postal, telclt, adrmail)
Commandes  (nocde PK, noclt FK→Clients, datecde, etatcde)
LigCdes    (nocde FK→Commandes, refart FK→Articles)  PK(nocde, refart)
LivraisonCom (nocde PK FK→Commandes, dateliv, livreur FK→Personnel,
              modepay, etatliv, remarque)
Messages   (id PK, sender_id FK→Personnel, receiver_id FK→Personnel?,
            nocde FK→Commandes?, type, content, created_at, is_read)
```

**Index créés pour les performances :**

| Index | Colonne(s) |
|---|---|
| `idx_livraison_dateliv` | `LivraisonCom(dateliv)` |
| `idx_livraison_livreur` | `LivraisonCom(livreur)` |
| `idx_livraison_etat` | `LivraisonCom(etatliv)` |
| `idx_commandes_noclt` | `Commandes(noclt)` |
| `idx_messages_receiver` | `Messages(receiver_id, is_read)` |

**Valeurs d'état de livraison :** `EN_ATTENTE` · `EN_COURS` · `LIVREE` · `NON_LIVREE`  
**Modes de paiement :** `ESPECE` · `CB` · `CHEQUE`

### 4.2 API REST — Référence complète

> Base URL : `http://<host>:3000/api`  
> Toutes les routes (sauf `/api/auth/login`) requièrent l'en-tête `Authorization: Bearer <token>`.

#### Authentification

| Méthode | Route | Rôle | Description |
|---|---|---|---|
| POST | `/api/auth/login` | Public | Connexion, retourne un JWT |

Corps de la requête :
```json
{ "login": "controleur", "motP": "password" }
```
Réponse :
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": { "id": 1, "nom": "Admin Controle", "role": "Controleur" }
}
```

#### Livraisons

| Méthode | Route | Rôle | Description |
|---|---|---|---|
| GET | `/api/deliveries` | Contrôleur | Liste filtrée (`dateFrom`, `dateTo`, `etat`, `livreur`, `noclt`, `nocde`) |
| GET | `/api/deliveries/today` | Contrôleur | Liste du jour, trié par `etatliv\|livreur\|client\|nocde` |
| GET | `/api/deliveries/my-today` | Livreur | Ses livraisons du jour |
| GET | `/api/deliveries/:nocde` | Les deux | Détail complet d'une livraison |
| PATCH | `/api/deliveries/:nocde/status` | Livreur | Modifier l'état + remarque |

Corps du PATCH :
```json
{ "etatliv": "NON_LIVREE", "remarque": "Client absent à domicile" }
```

#### Tableau de bord

| Méthode | Route | Rôle | Description |
|---|---|---|---|
| GET | `/api/dashboard/by-driver` | Contrôleur | Nb livraisons par livreur × état |
| GET | `/api/dashboard/by-client` | Contrôleur | Nb livraisons par client × état |

#### Messagerie

| Méthode | Route | Rôle | Description |
|---|---|---|---|
| POST | `/api/messages/info` | Contrôleur | Message INFO vers un livreur (ou broadcast) |
| POST | `/api/messages/urgence` | Livreur | Message URGENCE vers les contrôleurs |
| GET | `/api/messages` | Les deux | Boîte de réception de l'utilisateur courant |

#### Utilitaires

| Méthode | Route | Rôle | Description |
|---|---|---|---|
| GET | `/api/drivers` | Les deux | Liste des livreurs (id + nom) |
| GET | `/api/clients` | Contrôleur | Liste des clients (id + nom) |

### 4.3 Événements Socket.IO (temps réel)

| Événement | Émetteur | Salle ciblée | Déclencheur |
|---|---|---|---|
| `delivery:updated` | Backend | `role:Controleur` | Après PATCH status |
| `message:new` | Backend | `user:<id>` ou `role:Livreur` / `role:Controleur` | Après POST /messages |

Le handshake Socket.IO est authentifié avec le même token JWT que les requêtes REST.

### 4.4 Sécurité de l'API

- Mots de passe hachés via **bcryptjs** (coût 10).
- Routes protégées par **JWT** (header `Authorization: Bearer`) avec vérification du rôle (`requireRole` middleware).
- Un livreur ne peut modifier que **ses propres** livraisons (vérification côté serveur avant `UPDATE`).

---

## 5. Arbre des interfaces

```
[Écran de connexion]
        │
        ├── Rôle = Contrôleur ──► [Accueil Contrôleur]
        │                              │
        │                    ┌─────────┼─────────────┬─────────────┐
        │                    ▼         ▼             ▼             ▼
        │         [Liste livraisons] [Livraisons  [Tableau     [Messagerie]
        │         (avec filtres)      du jour]   de bord]
        │                    │         │
        │                    └────┬────┘
        │                         ▼
        │              [Détail d'une livraison]
        │
        │                    [Envoyer message INFO]
        │
        └── Rôle = Livreur ──► [Accueil Livreur]
                                    │
                         ┌──────────┴──────────┐
                         ▼                     ▼
               [Mes livraisons du jour]   [Messagerie]
                         │
                         ▼
               [Détail d'une livraison]
                         │
                    ┌────┴─────┐
                    ▼          ▼
          [Modifier état]  [Envoyer
          + remarque        message
          (si NON_LIVREE)   URGENCE]
```

### Description des écrans

| Écran | Activité Android | Rôle |
|---|---|---|
| Connexion | `LoginActivity` | Les deux |
| Accueil Contrôleur | `ControllerHomeActivity` | Contrôleur |
| Liste des livraisons | `DeliveriesListActivity` | Contrôleur |
| Tableau de bord | `DashboardActivity` | Contrôleur |
| Envoyer message INFO | `SendInfoActivity` | Contrôleur |
| Accueil Livreur | `DriverHomeActivity` | Livreur |
| Détail livraison | `DeliveryDetailActivity` | Livreur |
| Envoyer message URGENCE | `SendUrgenceActivity` | Livreur |
| Messagerie (inbox) | `MessagesActivity` | Les deux |

---

## 6. Outils utilisés et difficultés rencontrées

### 6.1 Outils utilisés

#### Côté backend

| Outil / Librairie | Version | Rôle |
|---|---|---|
| Node.js | 18 LTS | Environnement d'exécution JavaScript |
| Express.js | 4.x | Framework HTTP REST |
| Socket.IO | 4.x | Messagerie temps réel (WebSocket) |
| pg (node-postgres) | 8.x | Pool de connexion PostgreSQL |
| jsonwebtoken | 9.x | Génération et vérification des tokens JWT |
| bcryptjs | 2.x | Hachage sécurisé des mots de passe |
| Morgan | 1.x | Journalisation des requêtes HTTP |
| dotenv | 16.x | Gestion des variables d'environnement |
| PostgreSQL | 16 Alpine | Base de données relationnelle |
| Docker / Docker Compose | - | Conteneurisation et orchestration des services |

#### Côté mobile Android

| Outil / Librairie | Version | Rôle |
|---|---|---|
| Android Studio Hedgehog | AGP 8.4 | IDE de développement |
| Java | JDK 17 | Langage de programmation |
| Retrofit 2 | 2.9+ | Client HTTP pour appeler l'API REST |
| OkHttp 3 | 4.x | Intercepteur HTTP (ajout du token JWT) |
| Gson | 2.x | Sérialisation / désérialisation JSON |
| Room | 2.x | Base de données locale (cache offline) |
| Socket.IO Client (Java) | 2.x | Réception des événements temps réel |
| Material Design 3 | - | Composants UI modernes |
| ViewBinding | - | Accès sûr aux vues XML |
| LiveData / ViewModel | Jetpack | Architecture MVVM réactive |

### 6.2 Difficultés rencontrées

#### Synchronisation offline-first (Room + Retrofit)

La principale difficulté technique a été la gestion du **mode hors connexion** pour le livreur.  
Un champ `pendingEtatliv` a été ajouté à l'entité Room `DeliveryEntity` pour conserver les changements d'état effectués sans réseau. À la reconnexion, le `DeliveryRepository` rejoue les PATCH en attente avant de rafraîchir le cache depuis le serveur.  
Sans ce marqueur, une panne réseau pendant le PATCH aurait entraîné une perte silencieuse du changement d'état.

#### Conflit de classpath Socket.IO / Android

L'intégration de la bibliothèque `socket.io-client-java` a posé un conflit de dépendances : la bibliothèque embarque sa propre implémentation de `org.json`, qui entre en collision avec celle fournie par Android. La solution a consisté à **exclure explicitement `org.json`** du classpath dans `app/build.gradle` :

```gradle
implementation('io.socket:socket.io-client:2.1.0') {
    exclude group: 'org.json', module: 'json'
}
```

#### Volume Docker et ré-initialisation de la base

Pour maintenir la persistance des données entre redémarrages de conteneur, un **volume nommé** Docker (`supervision_pgdata`) a été configuré. Le script `init.sql` n'est rejoué que lorsque le volume est vide (premier démarrage). Pour ré-initialiser complètement les données (phase de test), la commande `docker compose down -v` est nécessaire — ce comportement a nécessité une documentation explicite pour éviter des pertes de données involontaires en équipe.

#### UX outdoor pour le livreur

L'écran du livreur étant utilisé en conditions extérieures (luminosité variable, mains occupées), des contraintes UX spécifiques ont été appliquées : contrastes Material 3 élevés, taille de police augmentée, boutons d'action de 56–64dp minimum. Ces contraintes ont allongé la phase de design des layouts XML.

---

## 7. Les interfaces

### 7.1 Écran de connexion — `LoginActivity`

L'écran d'accueil présente un formulaire login / mot de passe. À la validation, l'application appelle `POST /api/auth/login`. En cas de succès, le token JWT et le rôle sont stockés dans `SessionManager` (SharedPreferences chiffrées), et l'utilisateur est redirigé vers l'activité correspondant à son rôle (`ControllerHomeActivity` ou `DriverHomeActivity`).

### 7.2 Accueil Contrôleur — `ControllerHomeActivity`

Tableau de navigation proposant quatre actions :
- **Toutes les livraisons** (avec filtres date/livreur/état/client/commande)
- **Livraisons du jour** (avec tri dynamique)
- **Tableau de bord** (statistiques agrégées)
- **Messagerie** (envoi et réception de messages)

Un badge de notification non lue est mis à jour en temps réel via Socket.IO.

### 7.3 Liste des livraisons — `DeliveriesListActivity`

Affiche un `RecyclerView` peuplé par `DeliveryAdapter`. Les filtres (période, état, livreur) sont accessibles depuis un panneau expandable. Chaque ligne affiche : numéro de commande, client, livreur, date, état (chip coloré), montant.

### 7.4 Tableau de bord — `DashboardActivity`

Deux sections :
1. **Par livreur** : tableau croisé `livreur_nom × etatliv → count`
2. **Par client** : tableau croisé `client_nom × etatliv → count`

Les données sont chargées depuis `GET /api/dashboard/by-driver` et `GET /api/dashboard/by-client`.

### 7.5 Accueil Livreur — `DriverHomeActivity`

Liste du jour chargée depuis `GET /api/deliveries/my-today` et mise en cache Room. Chaque item (via `DriverDeliveryAdapter`) affiche : ordre, numéro commande, nom client, téléphone (cliquable), ville, état.  
Un `SwipeRefreshLayout` permet la synchronisation manuelle.

### 7.6 Détail d'une livraison — `DeliveryDetailActivity`

Affiche la fiche complète :
- Informations client (nom, téléphone, adresse, e-mail)
- Bouton **Google Maps** (Intent vers l'URL `maps_url` retournée par l'API)
- Liste des articles (RecyclerView imbriqué)
- Total (nb articles, montant, mode de paiement)
- Section modification d'état : Spinner + champ remarque (affiché uniquement si `NON_LIVREE` sélectionné)
- Bouton **Envoyer message d'urgence**

### 7.7 Messagerie — `MessagesActivity`

Interface partagée entre les deux rôles, adaptée via le type de message :
- **Contrôleur** : reçoit les messages URGENCE des livreurs, peut envoyer des messages INFO.
- **Livreur** : reçoit les messages INFO du contrôleur, peut envoyer des messages URGENCE.

La réception temps réel est assurée par `RealtimeClient` (wrapper Socket.IO) qui émet des événements LiveData captés par le ViewModel.

---

## 8. Annexe — Code des parties importantes

### A. Script d'initialisation de la base de données (`backend/db/init.sql`)

```sql
CREATE TABLE IF NOT EXISTS LivraisonCom (
    nocde        INTEGER PRIMARY KEY REFERENCES Commandes(nocde) ON DELETE CASCADE,
    dateliv      DATE NOT NULL,
    livreur      INTEGER NOT NULL REFERENCES Personnel(idpers),
    modepay      VARCHAR(50) NOT NULL DEFAULT 'ESPECE',
    etatliv      VARCHAR(50) NOT NULL DEFAULT 'EN_ATTENTE',
    remarque     TEXT
);

CREATE TABLE IF NOT EXISTS Messages (
    id           SERIAL PRIMARY KEY,
    sender_id    INTEGER NOT NULL REFERENCES Personnel(idpers),
    receiver_id  INTEGER REFERENCES Personnel(idpers),
    nocde        INTEGER REFERENCES Commandes(nocde),
    type         VARCHAR(20) NOT NULL DEFAULT 'INFO',
    content      TEXT NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    is_read      BOOLEAN NOT NULL DEFAULT FALSE
);
```

### B. Middleware d'authentification JWT (`backend/src/middleware/auth.js`)

```javascript
const jwt = require('jsonwebtoken');

function authenticate(req, res, next) {
    const header = req.headers.authorization || '';
    const token  = header.startsWith('Bearer ') ? header.slice(7) : null;
    if (!token) return res.status(401).json({ error: 'No token' });

    try {
        req.user = jwt.verify(token, process.env.JWT_SECRET);
        next();
    } catch {
        res.status(401).json({ error: 'Invalid token' });
    }
}

function requireRole(...roles) {
    return (req, res, next) => {
        if (!roles.includes(req.user?.role))
            return res.status(403).json({ error: 'Forbidden' });
        next();
    };
}

module.exports = { authenticate, requireRole };
```

### C. Modification de l'état d'une livraison — backend (`deliveryController.js`)

```javascript
async function updateDeliveryStatus(req, res) {
    const { nocde } = req.params;
    const { etatliv, remarque } = req.body || {};

    const allowed = ['EN_ATTENTE', 'EN_COURS', 'LIVREE', 'NON_LIVREE'];
    if (!allowed.includes(etatliv))
        return res.status(400).json({ error: 'Invalid etatliv' });
    if (etatliv === 'NON_LIVREE' && !remarque)
        return res.status(400).json({ error: 'remarque is required for NON_LIVREE' });

    // Drivers can only update their own deliveries
    if (req.user.role === 'Livreur') {
        const own = await db.query(
            'SELECT 1 FROM LivraisonCom WHERE nocde = $1 AND livreur = $2',
            [nocde, req.user.id]
        );
        if (own.rowCount === 0)
            return res.status(403).json({ error: 'Not your delivery' });
    }

    const r = await db.query(
        `UPDATE LivraisonCom SET etatliv = $1, remarque = COALESCE($2, remarque)
         WHERE nocde = $3 RETURNING nocde, etatliv, remarque`,
        [etatliv, remarque || null, nocde]
    );

    // Notify connected controllers in real time
    const io = req.app.get('io');
    if (io) io.to('role:Controleur').emit('delivery:updated', r.rows[0]);

    res.json(r.rows[0]);
}
```

### D. Message d'urgence — enrichissement automatique (`messageController.js`)

```javascript
async function sendUrgence(req, res) {
    const { nocde, content } = req.body;

    // Enrich message with client contact as required by the spec
    const ctx = await db.query(
        `SELECT cl.telclt, (cl.prenomclt || ' ' || cl.nomclt) AS client_nom
         FROM Commandes c JOIN Clients cl ON cl.noclt = c.noclt
         WHERE c.nocde = $1`, [nocde]
    );
    const clientInfo = ctx.rows[0]
        ? ` [Client ${ctx.rows[0].client_nom} - ${ctx.rows[0].telclt}]` : '';

    const r = await db.query(
        `INSERT INTO Messages (sender_id, nocde, type, content)
         VALUES ($1, $2, 'URGENCE', $3) RETURNING *`,
        [req.user.id, nocde, content + clientInfo]
    );

    const io = req.app.get('io');
    if (io) io.to('role:Controleur').emit('message:new', r.rows[0]);
    res.status(201).json(r.rows[0]);
}
```

### E. Configuration Docker Compose (`docker-compose.yml`)

```yaml
services:
  db:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB:       bdg_livraisoncom_25
      POSTGRES_USER:     supervision
      POSTGRES_PASSWORD: supervision_pass
    volumes:
      - pgdata:/var/lib/postgresql/data
      - ./backend/db/init.sql:/docker-entrypoint-initdb.d/01-init.sql:ro

  api:
    build: ./backend
    ports:
      - "3000:3000"
    environment:
      DATABASE_URL: postgres://supervision:supervision_pass@db:5432/bdg_livraisoncom_25
      JWT_SECRET:   supersecretkey
    depends_on:
      - db

volumes:
  pgdata:
    name: supervision_pgdata
```

---

*Rapport généré dans le cadre du projet académique — 2ING INFO, AU 2025-2026.*

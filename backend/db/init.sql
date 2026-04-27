-- ============================================================
-- BDG_LivraisonCom_25 - Initialization Script
-- Database for "Supervision des Livraisons" mobile app
-- ============================================================

-- ---------- TABLES ----------

CREATE TABLE IF NOT EXISTS Postes (
    codeposte   SERIAL PRIMARY KEY,
    libelle     VARCHAR(100) NOT NULL,
    indice      INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS Personnel (
    idpers       SERIAL PRIMARY KEY,
    nompers      VARCHAR(100) NOT NULL,
    prenompers   VARCHAR(100) NOT NULL,
    adrpers      VARCHAR(255),
    villepers    VARCHAR(100),
    telpers      VARCHAR(20),
    d_embauche   DATE,
    login        VARCHAR(50) UNIQUE NOT NULL,
    motp         VARCHAR(255) NOT NULL,
    codeposte    INTEGER REFERENCES Postes(codeposte) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS Articles (
    refart       SERIAL PRIMARY KEY,
    designation  VARCHAR(255) NOT NULL,
    prixA        NUMERIC(10,2) NOT NULL DEFAULT 0,
    prixV        NUMERIC(10,2) NOT NULL DEFAULT 0,
    codetva      VARCHAR(20),
    categorie    VARCHAR(100),
    qtestk       INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS Clients (
    noclt        SERIAL PRIMARY KEY,
    nomclt       VARCHAR(100) NOT NULL,
    prenomclt    VARCHAR(100),
    adrclt       VARCHAR(255),
    villeclt     VARCHAR(100),
    code_postal  VARCHAR(20),
    telclt       VARCHAR(20),
    adrmail      VARCHAR(150)
);

CREATE TABLE IF NOT EXISTS Commandes (
    nocde        SERIAL PRIMARY KEY,
    noclt        INTEGER NOT NULL REFERENCES Clients(noclt) ON DELETE CASCADE,
    datecde      DATE NOT NULL,
    etatcde      VARCHAR(50) NOT NULL DEFAULT 'EN_COURS'
);

CREATE TABLE IF NOT EXISTS LigCdes (
    nocde        INTEGER NOT NULL REFERENCES Commandes(nocde) ON DELETE CASCADE,
    refart       INTEGER NOT NULL REFERENCES Articles(refart),
    qtecde       INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (nocde, refart)
);

CREATE TABLE IF NOT EXISTS LivraisonCom (
    nocde        INTEGER PRIMARY KEY REFERENCES Commandes(nocde) ON DELETE CASCADE,
    dateliv      DATE NOT NULL,
    livreur      INTEGER NOT NULL REFERENCES Personnel(idpers),
    modepay      VARCHAR(50) NOT NULL DEFAULT 'ESPECE',
    etatliv      VARCHAR(50) NOT NULL DEFAULT 'EN_ATTENTE',
    remarque     TEXT
);

-- Real-time messaging between Controllers and Drivers
CREATE TABLE IF NOT EXISTS Messages (
    id           SERIAL PRIMARY KEY,
    sender_id    INTEGER NOT NULL REFERENCES Personnel(idpers),
    receiver_id  INTEGER REFERENCES Personnel(idpers),
    nocde        INTEGER REFERENCES Commandes(nocde),
    type         VARCHAR(20) NOT NULL DEFAULT 'INFO',  -- INFO | URGENCE
    content      TEXT NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    is_read      BOOLEAN NOT NULL DEFAULT FALSE
);

-- ---------- INDEXES ----------
CREATE INDEX IF NOT EXISTS idx_livraison_dateliv ON LivraisonCom(dateliv);
CREATE INDEX IF NOT EXISTS idx_livraison_livreur ON LivraisonCom(livreur);
CREATE INDEX IF NOT EXISTS idx_livraison_etat   ON LivraisonCom(etatliv);
CREATE INDEX IF NOT EXISTS idx_commandes_noclt  ON Commandes(noclt);
CREATE INDEX IF NOT EXISTS idx_messages_receiver ON Messages(receiver_id, is_read);

-- ---------- SEED DATA ----------
INSERT INTO Postes (libelle, indice) VALUES
    ('Controleur', 100),
    ('Livreur',     50)
ON CONFLICT DO NOTHING;

-- Default password is "password" hashed with bcrypt (cost 10).
-- Hash verified with: bcrypt.compareSync('password', hash) === true
INSERT INTO Personnel (nompers, prenompers, adrpers, villepers, telpers, d_embauche, login, motp, codeposte) VALUES
    ('Admin',  'Controle', '12 rue Centrale', 'Tunis',   '+21620000001', '2020-01-01', 'controleur', '$2a$10$Q7HjrsHZfMd2UpZdXgQn0.JC3JIaMcXsI9mdJ0LFoFTA9KckOVZuK', 1),
    ('Trabelsi','Mohamed', '5 av. Habib',     'Sfax',    '+21620000002', '2022-03-15', 'livreur1',   '$2a$10$Q7HjrsHZfMd2UpZdXgQn0.JC3JIaMcXsI9mdJ0LFoFTA9KckOVZuK', 2),
    ('Ben Ali','Sami',    '8 rue de la paix','Sousse',  '+21620000003', '2023-05-20', 'livreur2',   '$2a$10$Q7HjrsHZfMd2UpZdXgQn0.JC3JIaMcXsI9mdJ0LFoFTA9KckOVZuK', 2)
ON CONFLICT DO NOTHING;

INSERT INTO Articles (designation, prixA, prixV, codetva, categorie, qtestk) VALUES
    ('Smartphone X10',   400.00, 650.00, 'TVA19', 'Electronique', 25),
    ('Casque Bluetooth',  35.00,  79.90, 'TVA19', 'Electronique', 100),
    ('Sac a dos',         18.00,  45.00, 'TVA07', 'Mode',         60),
    ('Cafetiere',         55.00, 119.00, 'TVA19', 'Maison',       30),
    ('Livre roman',        6.00,  14.50, 'TVA07', 'Culture',     150)
ON CONFLICT DO NOTHING;

INSERT INTO Clients (nomclt, prenomclt, adrclt, villeclt, code_postal, telclt, adrmail) VALUES
    ('Mansour', 'Ines',   '22 rue Carthage',   'Tunis',  '1000', '+21622111111', 'ines@example.com'),
    ('Hamdi',   'Karim',  '7 av. Bourguiba',   'Sfax',   '3000', '+21622222222', 'karim@example.com'),
    ('Saidi',   'Lina',   '14 rue des fleurs', 'Sousse', '4000', '+21622333333', 'lina@example.com')
ON CONFLICT DO NOTHING;

INSERT INTO Commandes (noclt, datecde, etatcde) VALUES
    (1, CURRENT_DATE,            'CONFIRMEE'),
    (2, CURRENT_DATE,            'CONFIRMEE'),
    (3, CURRENT_DATE - 1,        'CONFIRMEE'),
    (1, CURRENT_DATE - 2,        'CONFIRMEE')
ON CONFLICT DO NOTHING;

INSERT INTO LigCdes (nocde, refart, qtecde) VALUES
    (1, 1, 1),
    (1, 2, 2),
    (2, 3, 1),
    (2, 5, 3),
    (3, 4, 1),
    (4, 2, 1)
ON CONFLICT DO NOTHING;

INSERT INTO LivraisonCom (nocde, dateliv, livreur, modepay, etatliv, remarque) VALUES
    (1, CURRENT_DATE,     2, 'ESPECE',  'EN_ATTENTE', NULL),
    (2, CURRENT_DATE,     3, 'CB',      'EN_COURS',   NULL),
    (3, CURRENT_DATE - 1, 2, 'ESPECE',  'LIVREE',     NULL),
    (4, CURRENT_DATE - 2, 3, 'CHEQUE',  'NON_LIVREE', 'Client absent')
ON CONFLICT DO NOTHING;

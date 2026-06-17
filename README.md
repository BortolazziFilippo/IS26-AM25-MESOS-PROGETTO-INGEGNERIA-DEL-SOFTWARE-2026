# Mesos — Documentazione del Progetto

## IS25-AM25

**Gruppo:** AM25  
**Membri del Team:**

Filippo Bortolazzi<br> 
Daniele Delgrossi<br> 
Sara Caliment <br>
Robert Gabriel Anton <br>

**Valutazione target:** 30L

---

## Installazione e Avvio

### Requisiti di Sistema

- **Java 25** o superiore
- Sistema operativo: Windows, macOS, Linux

### Build dal Sorgente

Per compilare il progetto e generare i JAR eseguibili:

```bash
./mvnw package -DskipTests
```

I JAR vengono prodotti nella cartella `jar/`:

| File | Descrizione |
|------|-------------|
| `jar/am25-Server-1.0-SNAPSHOT.jar` | Eseguibile del server |
| `jar/am25-Client-1.0-SNAPSHOT.jar` | Eseguibile del client |

### Avvio del Server

```bash
java -jar jar/am25-Server-1.0-SNAPSHOT.jar
```

Il server si mette in ascolto sulla porta `1099` (RMI) e `6969` (Socket).

### Avvio del Client

```bash
java -jar jar/am25-Client-1.0-SNAPSHOT.jar
```

All'avvio il client chiederà di scegliere:
1. Il **protocollo di rete** (RMI o Socket)
2. L'**indirizzo IP** del server
3. La **modalità di gioco** (GUI o TUI)

> **Nota:** per giocare in locale, usare come indirizzo IP `localhost` o `127.0.0.1`.

---

## Tabella di Valutazione

| Requisiti Soddisfatti | Voto Massimo |
|---|---|
| Regole Semplificate + TUI + RMI o Socket | 18 |
| Regole Complete + TUI + RMI o Socket | 20 |
| Regole Complete + TUI + RMI o Socket + 1 FA | 22 |
| Regole Complete + TUI + GUI + RMI o Socket + 1 FA | 24 |
| Regole Complete + TUI + GUI + RMI + Socket + 1 FA | 27 |
| Regole Complete + TUI + GUI + RMI + Socket + 2 FA | 30 |
| **Regole Complete + TUI + GUI + RMI + Socket + 3 FA** | **30L** |

### Stato Implementazione (Target: 30L)

#### Requisiti Base

| Funzionalità | Stato | Note |
|---|---|---|
| **Regole Complete** | ✅ | Implementazione completa del regolamento di Mesos |
| **TUI** | ✅ | Interfaccia testuale per terminale con rendering a colori |
| **GUI** | ✅ | Interfaccia grafica JavaFX |
| **Socket TCP** | ✅ | Comunicazione client-server con serializzazione Java |
| **RMI** | ✅ | Sistema distribuito con heartbeat monitoring |

#### Funzionalità Avanzate (3/4 implementate)

| Funzionalità Avanzata | Stato | Descrizione |
|---|---|---|
| **Classifica su DB** | ✅ | Storico partite su AWS RDS MySQL; classifica per numero di giocatori |
| **Partite Multiple** | ❌ | Non implementato — una partita per istanza server |
| **Persistenza** | ✅ | Salvataggio periodico dello stato (JSON) e ripresa dopo crash del server |
| **Resilienza Disconnessioni** | ✅ | Riconnessione giocatori, skip turni, watchdog heartbeat |

---

## Documentazione Tecnica

Tutta la documentazione tecnica si trova nella cartella [`Documentazione_AM25/`](Documentazione_AM25/).

| Cartella | Contenuto |
|----------|-----------|
| [`Documentazione_AM25/UML/`](Documentazione_AM25/UML/) | Diagrammi UML di alto livello e di dettaglio |
| [`Documentazione_AM25/Design_Pattern/`](Documentazione_AM25/Design_Pattern/) | Report sui design pattern adottati |
| [`Documentazione_AM25/Protocollo_di_Rete/`](Documentazione_AM25/Protocollo_di_Rete/) | Report sul protocollo di comunicazione client-server |
| [`Documentazione_AM25/Javadoc/`](Documentazione_AM25/Javadoc/) | Javadoc generata dal codice sorgente |

### Test

Il progetto include una suite di **36 classi di test** (JUnit 5 + Mockito) con copertura di:

- Logica delle carte (Shaman, Builder, Hunter, Artist, Inventor, Gatherer, Event, Building)
- Flusso di gioco (Game, Market, TurnManager, Controller)
- Effetti di edifici ed eventi
- Persistenza (salvataggio e ripristino dello stato)
- Mappatura dei DTO di rete

La copertura del model lato server (logica di gioco) è circa **87%** delle istruzioni (rilevata con JaCoCo). GUI e layer di rete sono esclusi dalla misurazione in quanto non soggetti a unit test.

Per eseguire i test:

```bash
./mvnw test
```

> **Nota:** la funzionalità di **Resilienza alle Disconnessioni** è stata testata in locale tramite Docker Compose, isolando il network di singoli container (client o server) per simulare a tutti gli effetti il calo improvviso della connessione di rete, senza dover intervenire sulla rete fisica della macchina.

### Architettura ad Alto Livello

Il sistema segue il pattern **MVC distribuito**:

- **Model** — logica di gioco (Game, Board, Player, Card, Market)
- **View** — TUI e GUI sul lato client; `ServerVirtualView` sul lato server (Observer pattern)
- **Controller** — coordina model e view; esposto via RMI e Socket

**Design pattern principali:** MVC, Observer, Memento, Factory, Strategy, Proxy.

**Struttura dei package:**

```
it.polimi.ingsw.am25/
├── server/
│   ├── model/         (Game, Board, Card, Player, Controller, Persistence, DB)
│   └── webLayer/      (RMI, Socket)
└── client/
    ├── GUI/           (JavaFX controllers e viste)
    ├── TUI/           (interfaccia testuale)
    └── webLayer/      (RMI, Socket)
```

---

## Strumenti di Sviluppo

#### Ambiente di Sviluppo

- **[IntelliJ IDEA](https://www.jetbrains.com/idea/)** — IDE principale per sviluppo Java
- **[Git](https://git-scm.com/)** — controllo versione con workflow collaborativo su GitHub
- **[Maven](https://maven.apache.org/)** — build automation e gestione dipendenze

#### Testing e Qualità

- **[JUnit 5](https://junit.org/junit5/)** — framework per unit test
- **[Mockito](https://site.mockito.org/)** — mocking per test di unità
- **[JaCoCo](https://www.jacoco.org/)** — code coverage

#### Documentazione e Design

- **[StarUML](https://staruml.io/)** — modellazione UML
- **JavaDoc** — documentazione API generata dal codice sorgente

---

_Politecnico di Milano — Ingegneria del Software 2025/2026_

# javul_graph_gen
The java project code for the generation of the AST, CFG, DFG graphs from the existing dataset
# Java Static Graph Extraction Pipeline

A Java-based pipeline for extracting **AST**, **CFG**, and **DFG** graphs from real-world and benchmark vulnerability datasets.  
Supports **CVEfixes**, **Juliet Test Suite**, and **OWASP Benchmark** — enabling large-scale semantic structure extraction for machine learning and vulnerability research.

---

## Overview

This project parses Java code snippets or full classes and generates three types of graphs:

- **AST (Abstract Syntax Tree)**
- **CFG (Control Flow Graph)**
- **DFG (Data Flow Graph)**

These graphs are stored in PostgreSQL as `jsonb` to support efficient ML-ready retrieval.

Datasets supported:

| Dataset            | Input Format                |
|--------------------|-----------------------------|
| CVEfixes           | Method-level Java snippets |
| Juliet Test Suite  | Full Java classes          |
| OWASP Benchmark    | Java servlet classes       |

---

## Features

- Automated Java parsing pipeline  
- AST/CFG/DFG extraction with safe JSON serialization  
- Separate workers for method-level and class-level parsing  
- Supports multiple datasets using a unified processing flow  
- Writes graph representations directly to PostgreSQL  

---

## Requirements

- **Java 17+**  
- **Maven 3.8+**  
- **PostgreSQL 13+**  
- **Git**

## Architecture

Each worker performs the following steps:

1. Loads rows from PostgreSQL (`raw_code` column)
2. Selects the correct parser:
   - `MethodWorker` → `MethodLevelParser`
   - `ClassWorker` → `ClassLevelParser`
3. Extracts:
   - **AST graph**
   - **CFG graph**
   - **DFG graph**
4. Serializes graphs to JSON
5. Stores results back to PostgreSQL *(successful parses only)*

---

## Database Schema

Create a table named `javul` (or any name you prefer):

```sql
CREATE TABLE javul (
    id         UUID PRIMARY KEY,
    raw_code   TEXT NOT NULL,
    source     TEXT,
    ast_graph  JSONB,
    cfg_graph  JSONB,
    dfg_graph  JSONB
);
```
## Configuration
- Set your PostgreSQL connection details in:  src/main/resources/application.properties
- Use Maven to build: mvn clean package

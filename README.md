# First Java CRUD

A small customer manager written in Java for the Git and GitHub assignment.

Student: **Rana Karahan - 72546**  
Teammate: **Eyup Ensar Demirel** (`EyupEnsarDemirel74062`)

## Features

- Add a customer with first name, last name and email.
- Show the customer list and search by name.
- Edit a customer and save changes.
- Delete a customer.
- Keep the data in a CSV file after closing the app.

## Run the app

Install **JDK 17 or newer** and check that `java -version` works.
No additional Java libraries are required.

Download or clone this repository and open a terminal in the project folder.

**Windows:**

```bat
run.bat
```

**macOS / Linux:**

```sh
sh run.sh
```

Then open **http://127.0.0.1:8080/** in your browser. Keep the terminal open while
using the app. Press **Ctrl+C** in the terminal to stop it.

You can also use the standard JDK commands:

```sh
javac -encoding UTF-8 -d out *.java
java -cp out course.crud.Application
```

If port 8080 is busy, run the compiled app with
`java -Dport=8082 -cp out course.crud.Application` and open port 8082 instead.

## Main files

| File | What it does |
| --- | --- |
| `Application.java` | Starts the server and handles requests from the forms. |
| `Customer.java` | Stores the ID, first name, last name and email of one customer. |
| `CustomerRepository.java` | Reads, saves, finds and deletes customer records. |
| `CustomerView.java` | Creates the HTML form and customer table. |

The browser sends a POST request to `/save` when the form is submitted. An ID of
0 means a new customer; another ID means an existing customer is updated.
The Delete button submits the record's ID to `/delete`. Deletion is immediate.

The two starting customers are fictional examples. The CSV file is created in
`data/`, which is ignored by Git. The application is for local practice, not for
storing real customer information on a public server.

## Test

After compiling, run `python3 test_crud.py` if Python 3 is installed. The test
uses a separate server on port 8088 and temporary data. It checks CRUD, search,
validation, HTML escaping and data persistence without changing the main data.

## Pair work and documentation

Both students need their own working application and repository. Each student
makes a small change in the other student's project through a pull request.
The owner reviews and merges it, keeping both contributors in the history.

Real application screenshots, startup-console screenshots and the final GitHub
history screenshot belong in `doc/`. See `doc/README.md` for the evidence checklist.

Pair-work status: **not completed yet**. A partner commit or merged pull request
has not been added to this repository yet.

The teacher's Platon instructions say to submit **only the public repository
link**, using **Online text submission**. Do not upload the screenshots separately.

The optional Spring/Vaadin tutorial is:
https://spring.io/guides/gs/crud-with-vaadin/

This version implements the CRUD functions using Java's built-in HTTP server
and a CSV file instead of Spring/Vaadin.
<img width="1730" height="967" alt="image" src="https://github.com/user-attachments/assets/de47a588-3945-4511-86c5-80ebc86161eb" />

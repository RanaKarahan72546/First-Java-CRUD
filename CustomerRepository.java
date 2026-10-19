package course.crud;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

// Stores the customers in a small CSV file. No separate database is needed.
public class CustomerRepository {
    private final Path file;
    private final List<Customer> customers = new ArrayList<>();
    private long nextId = 1;

    public CustomerRepository(Path file) throws IOException {
        this.file = file;
        if (Files.exists(file)) {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (int i = 1; i < lines.size(); i++) {
                List<String> values = readCsvRow(lines.get(i));
                if (values.size() != 4) throw new IOException("Invalid customer CSV row");
                long id = Long.parseLong(values.get(0));
                customers.add(new Customer(id, values.get(1), values.get(2), values.get(3)));
                nextId = Math.max(nextId, id + 1);
            }
        } else {
            customers.add(new Customer(nextId++, "Alex", "Green", "alex@example.com"));
            customers.add(new Customer(nextId++, "Maya", "Brown", "maya@example.com"));
            write(customers);
        }
    }

    public synchronized List<Customer> findAll(String search) {
        String query = search.toLowerCase(java.util.Locale.ROOT);
        return customers.stream().filter(c -> (c.firstName() + " " + c.lastName())
                .toLowerCase(java.util.Locale.ROOT).contains(query)).toList();
    }

    public synchronized Customer findById(long id) {
        return customers.stream().filter(c -> c.id() == id).findFirst().orElse(null);
    }

    public synchronized boolean save(long id, String firstName, String lastName, String email)
            throws IOException {
        List<Customer> updated = new ArrayList<>(customers);
        if (id == 0) {
            updated.add(new Customer(nextId, firstName, lastName, email));
        } else {
            Customer existing = findById(id);
            if (existing == null) return false;
            updated.set(updated.indexOf(existing), new Customer(id, firstName, lastName, email));
        }
        write(updated); // Only change the in-memory list after the disk write succeeds.
        customers.clear();
        customers.addAll(updated);
        if (id == 0) nextId++;
        return true;
    }

    public synchronized boolean delete(long id) throws IOException {
        List<Customer> updated = new ArrayList<>(customers);
        if (!updated.removeIf(c -> c.id() == id)) return false;
        write(updated);
        customers.clear();
        customers.addAll(updated);
        return true;
    }

    private void write(List<Customer> values) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        List<String> lines = new ArrayList<>();
        lines.add("id,firstName,lastName,email");
        for (Customer c : values) {
            lines.add(c.id() + "," + quote(c.firstName()) + "," + quote(c.lastName())
                    + "," + quote(c.email()));
        }
        Path temporary = Files.createTempFile(parent, "customers-", ".tmp");
        try {
            Files.write(temporary, lines, StandardCharsets.UTF_8);
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private String quote(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private List<String> readCsvRow(String line) throws IOException {
        List<String> result = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    value.append('"');
                    i++;
                } else quoted = !quoted;
            } else if (ch == ',' && !quoted) {
                result.add(value.toString());
                value.setLength(0);
            } else value.append(ch);
        }
        if (quoted) throw new IOException("Unclosed quote in customer CSV");
        result.add(value.toString());
        return result;
    }
}

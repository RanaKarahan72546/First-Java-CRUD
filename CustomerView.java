package course.crud;

import java.util.List;

// Builds the HTML page. All values from a customer are escaped before displaying them.
public class CustomerView {
    public static String page(List<Customer> customers, Customer selected,
                              String query, String message, String error) {
        StringBuilder rows = new StringBuilder();
        for (Customer c : customers) {
            rows.append("<tr><td>").append(c.id()).append("</td><td>")
                    .append(escape(c.firstName())).append(" ").append(escape(c.lastName()))
                    .append("</td><td>").append(escape(c.email()))
                    .append("</td><td><a class='edit' href='/?edit=").append(c.id())
                    .append("'>Edit</a><form class='inline' method='post' action='/delete'>")
                    .append("<input type='hidden' name='id' value='").append(c.id())
                    .append("'><button class='delete' type='submit'>Delete</button></form></td></tr>");
        }
        if (customers.isEmpty()) rows.append("<tr><td colspan='4'>No customers found.</td></tr>");
        Customer c = selected == null ? new Customer(0, "", "", "") : selected;
        String notice = message.isBlank() ? "" : "<p class='notice' role='status'>" + escape(message) + "</p>";
        if (!error.isBlank()) notice = "<p class='error' role='alert'>" + escape(error) + "</p>";
        return """
                <!doctype html><html lang="en"><head><meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>Customer Manager</title><style>
                *{box-sizing:border-box}body{margin:0;background:#f4f6f8;color:#243247;font:16px Arial,sans-serif}
                header{background:#244c78;color:white;padding:25px 0}header div,main{max-width:1000px;margin:auto}
                h1{font-size:27px;margin:0 0 7px}header p{margin:0;color:#e0e9f4}main{padding:24px 12px}
                section{background:white;border:1px solid #dce1e7;border-radius:7px;padding:22px;margin-bottom:22px}
                h2{font-size:20px;margin:0 0 20px}label{display:block;margin:0 0 7px;font-weight:bold;font-size:14px}
                input{font:inherit;padding:10px;border:1px solid #aebbc8;border-radius:4px;width:100%;background:white}
                .fields{display:grid;grid-template-columns:1fr 1fr 1.5fr;gap:18px}.actions{margin-top:18px}
                button,.button{font:inherit;background:#275d94;color:white;border:0;border-radius:4px;padding:10px 17px;cursor:pointer;text-decoration:none}
                a{color:#245c97}.secondary{margin-left:14px}.search{display:flex;gap:12px;max-width:560px;margin-bottom:18px}
                table{border-collapse:collapse;width:100%;text-align:left}th,td{padding:14px 10px;border-bottom:1px solid #e0e5eb}
                th{background:#f1f4f7;font-size:14px}td:last-child{white-space:nowrap}.inline{display:inline;margin-left:15px}
                .delete{background:white;color:#b22b35;border:1px solid #dcb0b4;padding:6px 10px;font-size:14px}
                .notice,.error{padding:12px;border-radius:4px;margin:0 0 20px}.notice{background:#e3f2e7;color:#235a32}.error{background:#fce5e5;color:#9b2828}
                .count{font-size:14px;color:#617185;margin-top:17px}footer{font-size:13px;color:#667485;padding:0 0 15px}
                @media(max-width:700px){header div{padding:0 15px}.fields{grid-template-columns:1fr}section{overflow:auto}}
                </style></head><body><header><div><h1>Customer Manager</h1>
                <p>A simple Java CRUD application</p></div></header><main>
                """ + notice + "<section><h2>" + (c.id() == 0 ? "Add customer" : "Edit customer #" + c.id())
                + "</h2><form method='post' action='/save'><input type='hidden' name='id' value='" + c.id() + "'>"
                + "<div class='fields'><div><label for='firstName'>First name</label><input id='firstName' name='firstName' maxlength='60' required value='" + escape(c.firstName()) + "'></div>"
                + "<div><label for='lastName'>Last name</label><input id='lastName' name='lastName' maxlength='60' required value='" + escape(c.lastName()) + "'></div>"
                + "<div><label for='email'>Email</label><input id='email' name='email' type='email' maxlength='120' required value='" + escape(c.email()) + "'></div></div>"
                + "<div class='actions'><button type='submit'>" + (c.id() == 0 ? "Add customer" : "Save changes")
                + "</button><a class='secondary' href='/'>Clear / cancel</a></div></form></section>"
                + "<section><h2>Customer list</h2><form class='search' method='get' action='/'>"
                + "<input name='q' aria-label='Search by name' placeholder='Search by name' value='" + escape(query)
                + "'><button type='submit'>Search</button><a class='button' href='/'>Reset</a></form>"
                + "<table><thead><tr><th>ID</th><th>Name</th><th>Email</th><th>Actions</th></tr></thead><tbody>"
                + rows + "</tbody></table><p class='count'>Customers shown: " + customers.size()
                + "</p></section><footer>Java CRUD coursework</footer></main></body></html>";
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}

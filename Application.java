package course.crud;

import java.util.List;

public class CustomerView {

    public static String page(List<Customer> customers, Customer selected, String query, String message, String error) {
        boolean isEditing = selected != null && selected.id() != 0;
        long editId = isEditing ? selected.id() : 0;
        String firstName = selected != null ? selected.firstName() : "";
        String lastName = selected != null ? selected.lastName() : "";
        String email = selected != null ? selected.email() : "";

        StringBuilder rows = new StringBuilder();
        int index = 1;
        for (Customer c : customers) {
            rows.append("<tr>")
                .append("<td>").append(index++).append("</td>")
                .append("<td>").append(escape(c.firstName())).append("</td>")
                .append("<td>").append(escape(c.lastName())).append("</td>")
                .append("<td>").append(escape(c.email())).append("</td>")
                .append("<td>")
                .append("<a href='/?edit=").append(c.id()).append("&q=").append(escape(query)).append("' style='margin-right:8px; text-decoration:none; color:#0066cc;'>Düzenle</a>")
                .append("<form action='/delete' method='POST' style='display:inline;' onsubmit=\"return confirm('Bu müşteriyi silmek istediğinize emin misiniz?');\">")
                .append("<input type='hidden' name='id' value='").append(c.id()).append("'>")
                .append("<button type='submit' style='background:#dc3545; color:white; border:none; padding:3px 8px; border-radius:3px; cursor:pointer;'>Sil</button>")
                .append("</form>")
                .append("</td>")
                .append("</tr>");
        }

        return """
            <!DOCTYPE html>
            <html lang="tr">
            <head>
                <meta charset="UTF-8">
                <title>Customer Manager</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; background: #f8f9fa; color: #333; }
                    .container { max-width: 800px; margin: auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                    input[type="text"], input[type="email"] { padding: 8px; width: calc(100%% - 18px); margin-bottom: 10px; border: 1px solid #ccc; border-radius: 4px; }
                    button, .btn { padding: 8px 15px; background: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; text-decoration: none; display: inline-block; }
                    button:hover, .btn:hover { background: #0056b3; }
                    .btn-secondary { background: #6c757d; }
                    .btn-secondary:hover { background: #5a6268; }
                    table { width: 100%%; border-collapse: collapse; margin-top: 20px; }
                    th, td { padding: 10px; border-bottom: 1px solid #ddd; text-align: left; }
                    th { background: #f1f3f5; }
                    .message { padding: 10px; background: #d4edda; color: #155724; border-radius: 4px; margin-bottom: 15px; }
                    .error { padding: 10px; background: #f8d7da; color: #721c24; border-radius: 4px; margin-bottom: 15px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h2>Müşteri Yönetim Sistemi</h2>
                    
                    %s
                    %s

                    <!-- Arama ve Filtre Sıfırlama Alanı -->
                    <form method="GET" action="/" style="margin-bottom: 20px;">
                        <input type="text" name="q" value="%s" placeholder="Müşteri ara (Ad, Soyad, Email)..." style="width: 70%%; display: inline-block;">
                        <button type="submit" style="width: 10%%;">Ara</button>
                        <a href="/" class="btn btn-secondary" style="padding: 8px 10px; font-size: 13px;">Tümünü Göster</a>
                    </form>

                    <!-- Ekleme / Düzenleme Formu -->
                    <form action="/save" method="POST" style="background: #fdfdfe; padding: 15px; border: 1px solid #e1e4e8; border-radius: 6px;">
                        <h3>%s</h3>
                        <input type="hidden" name="id" value="%d">
                        <label>Ad:</label>
                        <input type="text" name="firstName" value="%s" required>
                        <label>Soyad:</label>
                        <input type="text" name="lastName" value="%s" required>
                        <label>E-posta:</label>
                        <input type="email" name="email" value="%s" required>
                        <br>
                        <button type="submit">%s</button>
                        %s
                    </form>

                    <!-- Müşteri Listesi Tablosu -->
                    <table>
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Ad</th>
                                <th>Soyad</th>
                                <th>E-posta</th>
                                <th>İşlemler</th>
                            </tr>
                        </thead>
                        <tbody>
                            %s
                        </tbody>
                    </table>
                </div>
            </body>
            </html>
            """.formatted(
                message.isBlank() ? "" : "<div class='message'>" + escape(message.replace("+", " ")) + "</div>",
                error.isBlank() ? "" : "<div class='error'>" + escape(error) + "</div>",
                escape(query),
                isEditing ? "Müşteriyi Düzenle" : "Yeni Müşteri Ekle",
                editId,
                escape(firstName),
                escape(lastName),
                escape(email),
                isEditing ? "Güncelle" : "Kaydet",
                isEditing ? "<a href='/' class='btn btn-secondary' style='margin-left: 10px;'>Vazgeç</a>" : "",
                rows.toString()
            );
    }

    private static String escape(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;");
    }
}

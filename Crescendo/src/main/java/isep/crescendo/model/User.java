package isep.crescendo.model;

import isep.crescendo.util.Preconditions;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class User {
    private int id;
    private String email;
    private String nome;
    private String passwordHash;
    private boolean isAdmin;
    private static final String specialCharsRegex = ".*[!@#$%^&*()_+\\-=\\[\\]{}|;:'\",.<>/?].*";

    public User(String email, String nome, String plainPassword) {
        Preconditions.ensure(email != null && email.contains("@"), "O email é inválido.");
        Preconditions.ensure(nome != null && !nome.trim().isEmpty(), "O nome é inválido.");
        Preconditions.ensure(plainPassword != null, "A password não pode ser vazia.");
        Preconditions.ensure(plainPassword.length() >= 10, "A password deve ter pelo menos 10 caracteres.");
        Preconditions.ensure(plainPassword.matches(".*[A-Z].*"), "A password deve conter pelo menos uma letra maiúscula.");
        Preconditions.ensure(plainPassword.matches(".*\\d.*"), "A password deve conter pelo menos um número.");
        Preconditions.ensure(
                plainPassword.matches(specialCharsRegex),
                "A password deve conter pelo menos um caractere especial."
        );
        this.email = email;
        this.nome = nome;
        this.passwordHash = hashPassword(plainPassword);
    }

    public User(int id, String email, String nome, String passwordHash) {
        this.id = id;
        this.email = email;
        this.nome = nome;
        this.passwordHash = passwordHash;
    }

    public User(int id, String email, String nome, String password, boolean isAdmin) {
        this.id = id;
        this.email = email;
        this.nome = nome;
        this.passwordHash = password;
        this.isAdmin = isAdmin;
    }


    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : encoded) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao fazer hash da password", e);
        }
    }

    public boolean verificarPassword(String plainPassword) {
        return Objects.equals(this.passwordHash, hashPassword(plainPassword));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) { this.id = id; }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) { this.email = email; }

    public String getNome() {
        return nome;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isAdmin() {return isAdmin;}

    public void setAdmin(boolean admin) {isAdmin = admin;}


    public void setNome(String nome) {
        Preconditions.ensure(nome != null && !nome.trim().isEmpty(), "O nome é inválido.");
        this.nome = nome;
    }

    public void setPassword(String plainPassword) {
        Preconditions.ensure(plainPassword != null, "A password não pode ser vazia.");
        Preconditions.ensure(plainPassword.length() >= 10, "A password deve ter pelo menos 10 caracteres.");
        Preconditions.ensure(plainPassword.matches(".*[A-Z].*"), "A password deve conter pelo menos uma letra maiúscula.");
        Preconditions.ensure(plainPassword.matches(".*\\d.*"), "A password deve conter pelo menos um número.");
        Preconditions.ensure(
                plainPassword.matches(specialCharsRegex),
                "A password deve conter pelo menos um caractere especial."
        );

        this.passwordHash = hashPassword(plainPassword);
    }
}

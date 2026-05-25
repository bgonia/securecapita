package io.getarrays.securecapita.query;

public class UserQuery {
    public static final String INSERT_USER_QUERY = "INSERT INTO users (first_name, last_name, email, password) VALUES (:firstName, :lastName, :email, :password)";
    public static final String COUNT_USER_EMAIL_QUERY = "SELECT COUNT(*) FROM users WHERE email = :email";
    public static final String INSERT_ACCOUNT_VERIFICATION_URL_QUERY = "INSERT INTO account_verifications (user_id, url) VALUES (:userId, :url)";
    public static final String SELECT_USER_BY_EMAIL_QUERY = "SELECT * FROM users WHERE email = :email";
    public static final String DELETE_VERIFICATION_CODE_BY_USER_ID = "DELETE FROM two_factor_verifications WHERE user_id = :id";
    public static final String INSERT_VERIFICATION_CODE_QUERY = "INSERT INTO two_factor_verifications (user_id, code, expiration_date) VALUES (:userId, :verificationCode, :expirationDate)";
    public static final String DELETE_PASSWORD_VERIFICATION_BY_USER_ID_QUERY = "DELETE from reset_password_verifications WHERE user_id = :userId";
    public static final String INSERT_PASSWORD_VERIFICATION_QUERY = "INSERT INTO reset_password_verifications (user_id, url, expiration_date) VALUES (:userId, :url, :expirationDate)";
    public static final String SELECT_EXPIRATION_BY_URL = "SELECT expiration_date < Now() AS is_expired FROM reset_password_verifications WHERE url = :url";
    public static final String SELECT_USER_BY_PASSWORD_URL_QUERY = "SELECT * FROM users WHERE id = (SELECT user_id FROM reset_password_verifications WHERE url = :url)";
    public static final String UPDATE_USER_PASSWORD_BY_URL_QUERY = "UPDATE users SET password = :password WHERE id = (SELECT user_id FROM reset_password_verifications WHERE url = :url)";
    public static final String DELETE_VERIFICATION_BY_URL_QUERY = "DELETE from reset_password_verifications WHERE url = :url";
    public static final String SELECT_USER_BY_ACCOUNT_URL_QUERY= "SELECT * FROM users WHERE id = (SELECT user_id FROM account_verifications WHERE url = :url)";
    public static final String UPDATE_USER_ENABLED_QUERY = "UPDATE users SET enabled = :enabled WHERE id = :id";


}

package io.getarrays.securecapita.repository.implementation;

import io.getarrays.securecapita.domain.Role;
import io.getarrays.securecapita.domain.User;
import io.getarrays.securecapita.domain.UserPrincipal;
import io.getarrays.securecapita.dto.UserDTO;
import io.getarrays.securecapita.enumeration.VerificationType;
import io.getarrays.securecapita.exception.ApiException;
import io.getarrays.securecapita.repository.RoleRepository;
import io.getarrays.securecapita.repository.UserRepository;
import io.getarrays.securecapita.rowmapper.UserRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static io.getarrays.securecapita.enumeration.RoleType.ROLE_USER;
import static io.getarrays.securecapita.enumeration.VerificationType.ACCOUNT;
import static io.getarrays.securecapita.enumeration.VerificationType.PASSWORD;
import static io.getarrays.securecapita.query.UserQuery.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl implements UserRepository<User>, UserDetailsService {

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private final NamedParameterJdbcTemplate jdbc;
    private final RoleRepository<Role> roleRepository;
    private final BCryptPasswordEncoder encode;

    @Override
    public User create(User user) {
        //check the email is unique
        if (getEmailCount(user.getEmail().trim().toLowerCase()) > 0)
            throw new ApiException("Email already in user. Please use a different email and try again");
        //save the user
        try {
            KeyHolder holder = new GeneratedKeyHolder();
            SqlParameterSource parameters = getSqlParameterSource(user);
            jdbc.update(INSERT_USER_QUERY, parameters, holder);
            user.setId(Objects.requireNonNull(holder.getKey().longValue()));
            // Add role to user
            roleRepository.addRoleToUser(user.getId(), ROLE_USER.name());
            // Send verification URL
            String verificationUrl = getVerificationUrl(UUID.randomUUID().toString(), ACCOUNT.getType());
            // Save URL in verification table
            jdbc.update(INSERT_ACCOUNT_VERIFICATION_URL_QUERY, Map.of("userId", user.getId(), "url", verificationUrl));
            // Send email to user with the verification URL
            //emailService.sendVerificationUrl(user.getFirstName(), user.getEmail(), verificationUrl, ACCOUNT);
            user.setEnabled(false);
            user.setNotLocked(true);
            // Return the newly created user
            return user;

        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again");
        }

    }


    @Override
    public Collection<User> list(int page, int pageSize) {
        return List.of();
    }

    @Override
    public User get(Long id) {
        return null;
    }

    @Override
    public User update(User data) {
        return null;
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }

    private Integer getEmailCount(String email) {
        return jdbc.queryForObject(COUNT_USER_EMAIL_QUERY, Map.of("email", email), Integer.class);
    }

    private SqlParameterSource getSqlParameterSource(User user) {
        return new MapSqlParameterSource()
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("email", user.getEmail())
                .addValue("password", encode.encode(user.getPassword()));
    }

    private String getVerificationUrl(String key, String type) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/verify/" + type + "/" + key).toUriString();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = getUserByEmail(email);

        if (user == null) {
            log.info("User not found in database");
            throw new UsernameNotFoundException("User not found in database");

        } else {
            log.info("User found in database: {}", email);
            return new UserPrincipal(user, roleRepository.getRoleByUserId(user.getId()));
        }

    }

    @Override
    public User getUserByEmail(String email) {
        try {
            return jdbc.queryForObject(SELECT_USER_BY_EMAIL_QUERY, Map.of("email", email), new UserRowMapper());
        } catch (EmptyResultDataAccessException exception) {
            throw new ApiException("No user found by email: " + email);
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again");
        }

    }

    @Override
    public void sendVerificationCode(UserDTO user) {
        String expirationDate = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern(DATE_FORMAT));
        String verificationCode = RandomStringUtils.secure().nextAlphabetic(8).toUpperCase();
        try {
            jdbc.update(DELETE_VERIFICATION_CODE_BY_USER_ID, Map.of("id", user.getId()));
            jdbc.update(INSERT_VERIFICATION_CODE_QUERY, Map.of("userId", user.getId(), "code", verificationCode, "expirationDate", expirationDate));
            //sendSMS(user.getPhone() + "From: SecureCapita \nVerification code\n" + verificationCode);
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again");
        }
    }

    @Override
    public void resetPassword(String email) {
        if (getEmailCount(email.trim().toLowerCase()) <= 0)
            throw new ApiException("There is not account for this email address");
        try {
            String expirationDate = DateFormatUtils.format(DateUtils.addDays(new Date(), 1), DATE_FORMAT);
            User user = getUserByEmail(email);
            String verificationUrl = getVerificationUrl(UUID.randomUUID().toString(), PASSWORD.getType());
            jdbc.update(DELETE_PASSWORD_VERIFICATION_BY_USER_ID_QUERY, Map.of("userId", user.getId()));
            jdbc.update(INSERT_PASSWORD_VERIFICATION_QUERY, Map.of("userId", user.getId(), "url", verificationUrl, "expirationDate", expirationDate));
            //TODO send email with url to user
            log.info("Verificarion URL: {}", verificationUrl);

        } catch (Exception e) {
            throw new ApiException("An error occurred. Please try again");
        }
    }

    @Override
    public User verifyPasswordKey(String key) {
        if(isLinkExpired(key, PASSWORD)) throw new ApiException("This link has expired. Please reset your password again");
        try{
            User user = jdbc.queryForObject(SELECT_USER_BY_PASSWORD_URL_QUERY, Map.of("url", getVerificationUrl(key, PASSWORD.getType())), new UserRowMapper());
            //jdbc.update("DELETE_USER_FROM_PASSWORD_VERIFICATION_QUERY", Map.of("id", user.getId()));  // depends on requirement
            return user;
        }catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw new ApiException("This link is not valid. Please reset your password again.");
        }catch (Exception exception){
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again");
        }
    }

    private boolean isLinkExpired(String key, VerificationType password) {
        try{
            return jdbc.queryForObject(SELECT_EXPIRATION_BY_URL, Map.of("url", getVerificationUrl(key, password.getType())), Boolean.class);
        }catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw new ApiException("This link is not valid. Please reset your password again.");
        }catch (Exception exception){
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again");
        }
    }

    @Override
    public void renewPassword(String key, String password, String confirmPassword) {
        if(!password.equals(confirmPassword)) throw new ApiException("Passwords don't match. Please try again");
        try{
            jdbc.update(UPDATE_USER_PASSWORD_BY_URL_QUERY, Map.of("password", encode.encode(password), "url", getVerificationUrl(key, PASSWORD.getType())));
            jdbc.update(DELETE_VERIFICATION_BY_URL_QUERY, Map.of("url", getVerificationUrl(key, PASSWORD.getType())));
        }catch (Exception exception){
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again");
        }
    }

    @Override
    public User verifyAccountKey(String key) {
        try{
            User user = jdbc.queryForObject(SELECT_USER_BY_ACCOUNT_URL_QUERY, Map.of("url", getVerificationUrl(key, ACCOUNT.getType())), new UserRowMapper());
            jdbc.update(UPDATE_USER_ENABLED_QUERY, Map.of("enabled", true, "id", user.getId()));
            return user;
        }catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw new ApiException("This link is not valid.");
        }catch (Exception exception){
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again");
        }
    }
}

package io.getarrays.securecapita.repository;

import io.getarrays.securecapita.domain.Role;

import java.util.Collection;

public interface RoleRepository<T extends Role> {

    T create(T user);
    Collection<T> list(int page, int pageSize);
    T get(Long id);
    T update(T data);
    Boolean delete(Long id);

    void addRoleToUser(Long userId, String roleName);

    /* More complex operations */
    Role getRoleByUserId(Long userId);
    Role getRoleByUserEmail(String email);
    void updateUserRole(Long userId, String roleName);


}

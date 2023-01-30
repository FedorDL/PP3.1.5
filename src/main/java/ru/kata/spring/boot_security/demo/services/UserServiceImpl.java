package ru.kata.spring.boot_security.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.dao.RoleDao;
import ru.kata.spring.boot_security.demo.dao.UserDao;
import ru.kata.spring.boot_security.demo.models.Role;
import ru.kata.spring.boot_security.demo.models.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final RoleDao roleDao;


    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserDao userDao, RoleDao roleDao, @Lazy BCryptPasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.roleDao = roleDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public User getById(Integer id) {
        return userDao.getById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> listUsers() {
        return userDao.listUsers();
    }

    @Transactional
    public boolean addRole(Role role) {
        Role userBas = roleDao.findByName(role.getRole());
        if (userBas != null) {
            return false;
        }
        roleDao.add(role);
        return true;
    }

    @Transactional
    public boolean add(User user) {
        User userBas = userDao.findByEmail(user.getUsername());
        if (userBas != null) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        System.out.println(user);
        List<String> list = user.getRoles().stream().map(r -> r.getRole()).collect(Collectors.toList());
        List<Role> roleList = listByRole(list);
        user.setRoles(roleList);
        userDao.add(user);
        return true;
    }

    @Override
    @Transactional
    public void removeUser(Integer id) {
        userDao.removeUser(id);
    }

    @Override
    @Transactional
    public void updateUser(User user) {
        User userBase = getById(user.getId());
        if (!userBase.getPassword().equals(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        List<String> list = user.getRoles().stream().map(r -> r.getRole()).collect(Collectors.toList());
        List<Role> roleList = listByRole(list);
        user.setRoles(roleList);
        userDao.updateUser(user);
    }

    public User findByEmail(String userName) {
        return userDao.findByEmail(userName);
    }

    public List<Role> listByRole(List<String> name) {
        return roleDao.listByName(name);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userDao.findByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException("User is nobody");
        }
        return user;
    }
}




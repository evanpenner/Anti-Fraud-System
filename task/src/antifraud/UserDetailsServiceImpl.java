package antifraud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private UserRepository userRepository;

    public UserDetailsServiceImpl(@Autowired UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        if (userRepository.existsByUsernameIgnoreCase(user.getUsername()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken!");
        if (userRepository.count() == 0) {
            user.setRole(User.Roles.ADMINISTRATOR);
        } else {
            user.setRole(User.Roles.MERCHANT);
            user.setLocked(true);
        }
        return userRepository.save(user);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public User findUser(String username) {
        if (username == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        User user = userRepository.findUserByUsernameIgnoreCase(username);
        if (user == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return user;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll(Sort.by("id").ascending());
    }

    public void deleteUser(String username) {
        if (!userRepository.existsByUsernameIgnoreCase(username))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        //System.out.print("Deleting by username");
        userRepository.deleteByUsernameIgnoreCase(username);
        //System.out.println("is user deleted? " + userRepository.existsByUsernameIgnoreCase(username));
    }

    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsernameIgnoreCase(username);

        if (user == null) {
            throw new UsernameNotFoundException("Not found: " + username);
        }

        return new UserDetailsImpl(user);
    }
}

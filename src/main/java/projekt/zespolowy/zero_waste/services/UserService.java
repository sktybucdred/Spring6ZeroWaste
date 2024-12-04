package projekt.zespolowy.zero_waste.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import projekt.zespolowy.zero_waste.dto.UserRegistrationDto;
import projekt.zespolowy.zero_waste.entity.Task;
import projekt.zespolowy.zero_waste.entity.User;
import projekt.zespolowy.zero_waste.entity.UserTask;
import projekt.zespolowy.zero_waste.entity.enums.AccountType;
import projekt.zespolowy.zero_waste.repository.TaskRepository;
import projekt.zespolowy.zero_waste.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import projekt.zespolowy.zero_waste.repository.UserTaskRepository;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import projekt.zespolowy.zero_waste.security.CustomUser;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserTaskRepository userTaskRepository;

    // Konstruktorowe wstrzykiwanie zależności
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Implementacja metody z UserDetailsService
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Nie znaleziono użytkownika: " + username);
        }
        return new CustomUser(user);
    }

    // Zapisywanie nowego użytkownika po zakodowaniu hasła
    public void registerUser(UserRegistrationDto userDto) {
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setAccountType(userDto.isBusinessAccount() ? AccountType.BUSINESS : AccountType.NORMAL);
        userRepository.save(user);

        // Pobierz wszystkie istniejące zadania
        List<Task> allTasks = taskRepository.findAll();

        // Dla każdego zadania przypisz je do nowego użytkownika
        for (Task task : allTasks) {
            UserTask userTask = new UserTask();
            userTask.setUser(user);
            userTask.setTask(task);
            userTask.setProgress(0);
            userTask.setCompleted(false);
            userTask.setCompletionDate(null);

            // Zapisz obiekt UserTask w bazie
            userTaskRepository.save(userTask);
        }
    }

    public List<UserTask> getUserTasksForUser(User user) {
        return userTaskRepository.findByUser(user);
    }

    public List<User> getRankedAndFilteredUsers(String search, String sortBy) {
        // Pobierz użytkowników
        List<User> allUsers = userRepository.findAll();

        // Przypisz miejsce w rankingu
        AtomicInteger rankCounter = new AtomicInteger(1);
        allUsers.sort(Comparator.comparingInt(User::getTotalPoints).reversed()); // Domyślne sortowanie
        allUsers.forEach(user -> user.setRank(rankCounter.getAndIncrement()));

        // Sortowanie według wybranego kryterium
        if (sortBy != null) {
            switch (sortBy) {
                case "username":
                    allUsers.sort(Comparator.comparing(User::getUsername));
                    break;
                case "name":
                    allUsers.sort(Comparator.comparing(user -> user.getFirstName().toLowerCase()));
                    break;
                case "totalPoints":
                    allUsers.sort(Comparator.comparingInt(User::getTotalPoints).reversed());
                    break;
            }
        }

        // Filtruj według wyszukiwania
        if (search != null && !search.isEmpty()) {
            return allUsers.stream()
                    .filter(user -> user.getUsername().toLowerCase().contains(search.toLowerCase()) ||
                            user.getFirstName().toLowerCase().contains(search.toLowerCase()) ||
                            user.getLastName().toLowerCase().contains(search.toLowerCase()))
                    .collect(Collectors.toList());
        }

        return allUsers;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll(); // Pobiera wszystkich użytkowników z bazy danych
    }

    // Znajdź użytkownika po nazwie użytkownika
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    // Znajdź użytkownika po emailu
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}

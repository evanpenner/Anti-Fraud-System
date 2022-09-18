package antifraud;

import org.hibernate.validator.constraints.CreditCardNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Validated
public class AntiFraudController {
    @Autowired
    UserDetailsServiceImpl userDetailsService;
    @Autowired
    CardService cardService;
    @Autowired
    IPService ipService;
    @Autowired
    PasswordEncoder encoder;

    @PostMapping("/api/antifraud/suspicious-ip")
    public BlacklistedIP addIp(@Valid @RequestBody BlacklistedIP ip) {
        return ipService.addIP(ip);
    }

    @DeleteMapping("/api/antifraud/suspicious-ip/{ip}")
    public ResponseEntity deleteIp(@PathVariable String ip) {
        ipService.deleteIp(ip);
        return ResponseEntity.ok(Map.of("status", "IP " + ip + " successfully removed!"));
    }

    @GetMapping("/api/antifraud/suspicious-ip")
    public List<BlacklistedIP> getIps() {
        return ipService.getIps();
    }

    @PostMapping("/api/antifraud/stolencard")
    @ResponseStatus(HttpStatus.OK)
    public Card addCard(@Valid @RequestBody Card card) {
        return cardService.addCard(card);
    }

    @DeleteMapping("/api/antifraud/stolencard/{card}")
    public ResponseEntity removeCard(@CreditCardNumber @PathVariable String card) {
        cardService.removeCard(card);
        return ResponseEntity.ok(Map.of("status", "Card " + card + " successfully removed!"));
    }

    @GetMapping("/api/antifraud/stolencard")
    public List<Card> getCards() {
        return cardService.getAllCards();
    }

    //@RolesAllowed({User.Roles.SUPPORT, User.Roles.ADMINISTRATOR})
    @PostMapping("/api/antifraud/transaction")
    public ResponseEntity<Map<String, Object>> transaction(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody Transaction transaction) {
        User u = userDetailsService.findUser(userDetails.getUsername());
        if (u.isLocked()) {
            //System.out.print("Locked user " + u.getUsername());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        if (!u.getRole().equals(User.Roles.MERCHANT)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        List<String> reasons = new ArrayList<>();
        if (cardService.existsByNumber(transaction.getNumber())) {
            // return ResponseEntity.ok(Map.of(""))
            reasons.add("card-number");
        }
        if (ipService.existsByIp(transaction.getIp())) {
            reasons.add("ip");
        }

        Long amount = transaction.getAmount();


        if (amount <= 1500) {
            //return ResponseEntity.ok().body(Map.of("result", "MANUAL_PROCESSING"));
        }
        String result = "";
        if (amount > 1500) {
            reasons.add("amount");
            result = "PROHIBITED";
        }

        if (reasons.contains("card-number") || reasons.contains("ip")) {
            result = "PROHIBITED";
        }
        if (!result.equals("PROHIBITED")) {
            if (amount <= 200) {
                return ResponseEntity.ok(Map.of("result", "ALLOWED", "info", "none"));
            }
            return ResponseEntity.ok(Map.of("result", "MANUAL_PROCESSING", "info", "amount"));
        } else {
            return ResponseEntity.ok(Map.of("result", result, "info", String.join(", ", reasons.stream().sorted().collect(Collectors.toList()))));
        }
    }

    @GetMapping("/api/auth/list")
    public List<User> listUsers() {
        return userDetailsService.getAllUsers();
    }

    @PostMapping("/api/auth/user")
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@Valid @RequestBody User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        return userDetailsService.createUser(user);
    }

    //@RolesAllowed({User.Roles.ADMINISTRATOR, User.Roles.SUPPORT})
    @PutMapping("/api/auth/role")
    public User setRole(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody Map<String, String> body) {
        User u = userDetailsService.findUser(userDetails.getUsername());
        if (!u.getRole().equals(User.Roles.ADMINISTRATOR)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        //        if (!body.containsKey("username") || !body.containsKey("role") || body.get("name").length() == 0 || body.get("role").length() == 0)
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        if (body.get("role").equalsIgnoreCase(User.Roles.ADMINISTRATOR))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        String role = body.get("role");
        if (!(role.equals(User.Roles.SUPPORT) || role.equals(User.Roles.MERCHANT)))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        User user = userDetailsService.findUser(body.get("username"));
        if (role.equals(user.getRole())) throw new ResponseStatusException(HttpStatus.CONFLICT);
        user.setRole(body.get("role"));
        return userDetailsService.saveUser(user);
    }

    @DeleteMapping({"/api/auth/user/", "/api/auth/user/{username}"})
    public ResponseEntity delete(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable(required = false) String username) {
        User user = userDetailsService.findUser(userDetails.getUsername());
        if (!user.getRole().equalsIgnoreCase(User.Roles.ADMINISTRATOR))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        userDetailsService.deleteUser(username);
        return ResponseEntity.ok(Map.of("username", username, "status", "Deleted successfully!"));
    }

    // @RolesAllowed(User.Roles.ADMINISTRATOR)
    @PutMapping("/api/auth/access")
    public ResponseEntity access(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody Map<String, String> body) {
        User u = userDetailsService.findUser(userDetails.getUsername());
        if (!u.getRole().equals(User.Roles.ADMINISTRATOR)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        User user = userDetailsService.findUser(body.get("username"));
        if (user.getRole().equalsIgnoreCase(User.Roles.ADMINISTRATOR))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        String operation = body.get("operation");
        if (operation.equals("LOCK")) {
            user.setLocked(true);
            userDetailsService.saveUser(user);
        } else if (operation.equals("UNLOCK")) {
            user.setLocked(false);
            System.out.println("unlocking " + user.getUsername());
            userDetailsService.saveUser(user);
        }
        return ResponseEntity.ok(Map.of("status", "User " + user.getUsername() + " " + (body.get("operation").equals("LOCK") ? "locked" : "unlocked") + "!"));
    }

    @ExceptionHandler({NullPointerException.class, ConstraintViolationException.class})

    public ResponseEntity handleException() {
        return ResponseEntity.badRequest().build();
    }
}

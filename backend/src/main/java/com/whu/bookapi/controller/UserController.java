package com.whu.bookapi.controller;

import com.whu.bookapi.dto.LoginRequest;
import com.whu.bookapi.dto.LoginResponse;
import com.whu.bookapi.model.User;
import com.whu.bookapi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: UserController.java
 * Author: WiseBookPal Team Version: 1.0 Date: 2024-11-20
 * Description: Controller for user-related operations including authentication,
 *              profile management, and address management.
 * Others:
 * Function List:
 * 1. login - Authenticates a user and returns a token.
 * 2. logout - Logs out the current user.
 * 3. info - Retrieves the current user's information.
 * 4. profile - Retrieves the user's profile information.
 * 5. updateProfile - Updates the user's profile information.
 * 6. changePassword - Changes the user's password.
 * 7. listAddresses - Lists all shipping addresses for the user.
 * 8. addAddress - Adds a new shipping address.
 * 9. updateAddress - Updates an existing shipping address.
 * 10. deleteAddress - Deletes a shipping address.
 * 11. applySeller - Submits an application to become a seller.
 * 12. register - Registers a new user.
 * 13. getSellerStats - Retrieves statistics for a seller.
 * History:
 * 1. Date: 2024-11-20
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
 */
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Function: login
     * Description: Authenticates a user and returns a token.
     * Calls: UserService.login
     * Called By: Frontend Login Page
     * Table Accessed: users, user_roles
     * Table Updated: user_token, users
     * Input: req (LoginRequest) - Username and password
     * Output: LoginResponse - Token and user info
     * Return: ResponseEntity<?>
     * Others:
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        LoginResponse resp = userService.login(req);
        if (resp == null) {
            Map<String, Object> m = new HashMap<>();
            m.put("message", "用户名或密码错误");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(m);
        }
        return ResponseEntity.ok(resp);
    }

    /**
     * Function: logout
     * Description: Logs out the current user by invalidating the token.
     * Calls: UserService.logout
     * Called By: Frontend Logout Button
     * Table Accessed: None
     * Table Updated: user_token
     * Input: token (String) - User token
     * Output: Map - Success status
     * Return: ResponseEntity<?>
     * Others:
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "token", required = false) String token) {
        if (token != null) {
            userService.logout(token);
        }
        Map<String, Object> m = new HashMap<>();
        m.put("success", true);
        return ResponseEntity.ok(m);
    }

    /**
     * Function: info
     * Description: Retrieves the current user's information based on the token.
     * Calls: UserService.getByToken
     * Called By: Frontend App Initialization
     * Table Accessed: user_token, users, user_roles
     * Table Updated: None
     * Input: token (String) - User token
     * Output: Map - User details
     * Return: ResponseEntity<?>
     * Others:
     */
    @GetMapping("/info")
    public ResponseEntity<?> info(@RequestHeader(value = "token", required = false) String token) {
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userService.getByToken(token);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Map<String, Object> m = new HashMap<>();
        m.put("username", user.getUsername());
        m.put("role", user.getRole());
        m.put("sellerStatus", user.getSellerStatus());
        m.put("phone", user.getPhone());
        m.put("email", user.getEmail());
        m.put("gender", user.getGender());
        return ResponseEntity.ok(m);
    }

    /**
     * Function: profile
     * Description: Retrieves the user's profile information.
     * Calls: UserService.getByToken
     * Called By: Frontend Profile Page
     * Table Accessed: user_token, users
     * Table Updated: None
     * Input: token (String) - User token
     * Output: Map - Profile details
     * Return: ResponseEntity<?>
     * Others:
     */
    @GetMapping("/profile")
    public ResponseEntity<?> profile(@RequestHeader(value = "token", required = false) String token) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Map<String, Object> m = new HashMap<>();
        m.put("username", user.getUsername());
        m.put("phone", user.getPhone());
        m.put("email", user.getEmail());
        m.put("gender", user.getGender());
        return ResponseEntity.ok(m);
    }

    /**
     * Function: updateProfile
     * Description: Updates the user's profile information.
     * Calls: UserService.getByToken, UserService.updateProfile
     * Called By: Frontend Profile Page
     * Table Accessed: user_token, users
     * Table Updated: users
     * Input: token (String) - User token
     *        body (Map) - Profile data (phone, email, gender)
     * Output: None
     * Return: ResponseEntity<?>
     * Others:
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestHeader(value = "token", required = false) String token,
                                          @RequestBody(required = false) Map<String, Object> body) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String phone = body == null || body.get("phone") == null ? null : body.get("phone").toString();
        String email = body == null || body.get("email") == null ? null : body.get("email").toString();
        String gender = body == null || body.get("gender") == null ? null : body.get("gender").toString();
        boolean ok = userService.updateProfile(user.getUsername(), phone, email, gender);
        if (!ok) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        return ResponseEntity.ok().build();
    }

    /**
     * Function: changePassword
     * Description: Changes the user's password.
     * Calls: UserService.getByToken, UserService.changePassword
     * Called By: Frontend Security Page
     * Table Accessed: user_token, users
     * Table Updated: users
     * Input: token (String) - User token
     *        body (Map) - Old and new passwords
     * Output: Map - Error message if failed
     * Return: ResponseEntity<?>
     * Others:
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestHeader(value = "token", required = false) String token,
                                           @RequestBody(required = false) Map<String, Object> body) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String oldPassword = body == null || body.get("oldPassword") == null ? null : body.get("oldPassword").toString();
        String newPassword = body == null || body.get("newPassword") == null ? null : body.get("newPassword").toString();
        boolean ok = userService.changePassword(user.getUsername(), oldPassword, newPassword);
        if (!ok) {
            Map<String, Object> m = new HashMap<>();
            m.put("message", "旧密码错误或新密码无效");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(m);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Function: listAddresses
     * Description: Lists all shipping addresses for the user.
     * Calls: UserService.getByToken, UserService.listAddresses
     * Called By: Frontend Address Page
     * Table Accessed: user_token, users, user_address
     * Table Updated: None
     * Input: token (String) - User token
     * Output: List<Map> - List of addresses
     * Return: ResponseEntity<?>
     * Others:
     */
    @GetMapping("/addresses")
    public ResponseEntity<?> listAddresses(@RequestHeader(value = "token", required = false) String token) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(userService.listAddresses(user.getUsername()));
    }

    /**
     * Function: addAddress
     * Description: Adds a new shipping address.
     * Calls: UserService.getByToken, UserService.addAddress
     * Called By: Frontend Address Page
     * Table Accessed: user_token, users, user_address
     * Table Updated: user_address
     * Input: token (String) - User token
     *        body (Map) - Address details
     * Output: Map - New address ID
     * Return: ResponseEntity<?>
     * Others:
     */
    @PostMapping("/addresses")
    public ResponseEntity<?> addAddress(@RequestHeader(value = "token", required = false) String token,
                                        @RequestBody(required = false) Map<String, Object> body) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String name = body == null || body.get("name") == null ? null : body.get("name").toString();
        String phone = body == null || body.get("phone") == null ? null : body.get("phone").toString();
        String address = body == null || body.get("address") == null ? null : body.get("address").toString();
        Boolean isDefault = null;
        Object defObj = body == null ? null : body.get("isDefault");
        if (defObj instanceof Boolean) {
            isDefault = (Boolean) defObj;
        } else if (defObj instanceof Number) {
            isDefault = ((Number) defObj).intValue() != 0;
        } else if (defObj instanceof String) {
            String s = ((String) defObj).trim();
            if ("true".equalsIgnoreCase(s) || "1".equals(s)) isDefault = true;
            else if ("false".equalsIgnoreCase(s) || "0".equals(s)) isDefault = false;
        }
        Long id = userService.addAddress(user.getUsername(), name, phone, address, isDefault);
        if (id == null) {
            Map<String, Object> m = new HashMap<>();
            m.put("message", "地址信息不完整");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(m);
        }
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        return ResponseEntity.ok(m);
    }

    /**
     * Function: updateAddress
     * Description: Updates an existing shipping address.
     * Calls: UserService.getByToken, UserService.updateAddress
     * Called By: Frontend Address Page
     * Table Accessed: user_token, users, user_address
     * Table Updated: user_address
     * Input: token (String) - User token
     *        id (Long) - Address ID
     *        body (Map) - Updated address details
     * Output: None
     * Return: ResponseEntity<?>
     * Others:
     */
    @PutMapping("/addresses/{id}")
    public ResponseEntity<?> updateAddress(@RequestHeader(value = "token", required = false) String token,
                                           @PathVariable("id") Long id,
                                           @RequestBody(required = false) Map<String, Object> body) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String name = body == null || body.get("name") == null ? null : body.get("name").toString();
        String phone = body == null || body.get("phone") == null ? null : body.get("phone").toString();
        String address = body == null || body.get("address") == null ? null : body.get("address").toString();
        Boolean isDefault = null;
        Object defObj = body == null ? null : body.get("isDefault");
        if (defObj instanceof Boolean) {
            isDefault = (Boolean) defObj;
        } else if (defObj instanceof Number) {
            isDefault = ((Number) defObj).intValue() != 0;
        } else if (defObj instanceof String) {
            String s = ((String) defObj).trim();
            if ("true".equalsIgnoreCase(s) || "1".equals(s)) isDefault = true;
            else if ("false".equalsIgnoreCase(s) || "0".equals(s)) isDefault = false;
        }
        boolean ok = userService.updateAddress(user.getUsername(), id, name, phone, address, isDefault);
        if (!ok) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        return ResponseEntity.ok().build();
    }

    /**
     * Function: deleteAddress
     * Description: Deletes a shipping address.
     * Calls: UserService.getByToken, UserService.deleteAddress
     * Called By: Frontend Address Page
     * Table Accessed: user_token, users, user_address
     * Table Updated: user_address
     * Input: token (String) - User token
     *        id (Long) - Address ID
     * Output: None
     * Return: ResponseEntity<?>
     * Others:
     */
    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<?> deleteAddress(@RequestHeader(value = "token", required = false) String token,
                                           @PathVariable("id") Long id) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        boolean ok = userService.deleteAddress(user.getUsername(), id);
        if (!ok) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        return ResponseEntity.ok().build();
    }

    /**
     * Function: applySeller
     * Description: Submits an application to become a seller.
     * Calls: UserService.getByToken, UserService.applySeller
     * Input: token (String) - User token
     *        body (Map) - Payment code file ID
     * Output: Map - Error message if failed
     * Return: ResponseEntity<?>
     */
    @PostMapping("/apply-seller")
    public ResponseEntity<?> applySeller(@RequestHeader(value = "token", required = false) String token,
                                         @RequestBody(required = false) Map<String, Object> body) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Object fileIdObj = body == null ? null : body.get("paymentCodeFileId");
        Long paymentCodeFileId = null;
        if (fileIdObj instanceof Number) {
            paymentCodeFileId = ((Number) fileIdObj).longValue();
        } else if (fileIdObj instanceof String) {
            try {
                paymentCodeFileId = Long.parseLong((String) fileIdObj);
            } catch (NumberFormatException ignored) {
            }
        }
        if (paymentCodeFileId == null) {
            Map<String, Object> m = new HashMap<>();
            m.put("message", "缺少收款码图片");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(m);
        }
        boolean ok = userService.applySeller(user.getUsername(), paymentCodeFileId);
        if (!ok) {
            Map<String, Object> m = new HashMap<>();
            m.put("message", "收款码图片无效");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(m);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Function: register
     * Description: Registers a new user in the system.
     * Calls: UserService.register
     * Input: body (Map) - Registration details (username, password, roles)
     * Output: Map - Success status
     * Return: ResponseEntity<?>
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> body) {
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        Object rolesObj = body.get("roles");
        Set<String> roles = null;
        if (rolesObj instanceof java.util.List) {
            roles = new java.util.HashSet<>((java.util.List<String>) rolesObj);
        }
        boolean ok = userService.register(username, password, roles);
        Map<String, Object> m = new HashMap<>();
        m.put("success", ok);
        if (!ok) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(m);
        return ResponseEntity.ok(m);
    }

    /**
     * Function: getSellerStats
     * Description: Retrieves statistics for a seller (score, sold count).
     * Calls: UserService.getSellerStats
     * Called By: Frontend Seller Page
     * Table Accessed: users
     * Table Updated: None
     * Input: username (String) - Seller username
     * Output: Map - Seller statistics
     * Return: ResponseEntity<?>
     * Others:
     */
    @GetMapping("/{username}/stats")
    public ResponseEntity<?> getSellerStats(@PathVariable("username") String username) {
        java.util.Map<String, Object> stats = userService.getSellerStats(username);
        if (stats == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok(stats);
    }
}

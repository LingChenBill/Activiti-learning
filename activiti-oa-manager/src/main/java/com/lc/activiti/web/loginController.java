package com.lc.activiti.web;

import com.lc.activiti.utils.UserUtil;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户登录 controller.
 *
 * @author zyz.
 */
@RestController
@RequestMapping("/user")
public class loginController {

    private static Logger logger = LoggerFactory.getLogger(loginController.class);

    @Autowired
    private IdentityService identityService;

    /**
     * 用户登录.
     *
     * @param username
     * @param password
     * @return
     */
    @GetMapping("/login")
    public ResponseEntity<Map<String,Object>> login(@RequestParam("username") String username,
                                                    @RequestParam("password") String password,
                                                    HttpSession session) {

        logger.info("username = {}, password = {}", username, password);

        boolean checkpwd = identityService.checkPassword(username, password);

        if (checkpwd) {
            // 读取用户信息.
            User user = identityService.createUserQuery().userId(username).singleResult();
            UserUtil.saveUserToSession(session, user);

            // 读取用户角色.
            List<Group> groupList = identityService.createGroupQuery().groupMember(user.getId()).list();

            String[] groupNames = new String[groupList.size()];

            for (int i = 0; i < groupNames.length; i++) {
                groupNames[i] = groupList.get(i).getName();
            }

            session.setAttribute("groupNames", ArrayUtils.toString(groupNames));

            Map<String, Object> map = new HashMap<String,Object>();
            map.put("content", ArrayUtils.toString(groupNames));

            return new ResponseEntity<>(map, HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 用户退出.
     *
     * @param session
     */
    @GetMapping(value = "/logout")
    public ResponseEntity logout(HttpSession session) {
        session.removeAttribute("user");

        Map<String, Object> map = new HashMap<>();
        map.put("status", HttpStatus.OK);
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

}

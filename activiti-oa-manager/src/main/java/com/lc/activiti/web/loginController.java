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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;

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
    @PostMapping("/login")
    public ResponseEntity login(@RequestParam("username") String username,
                        @RequestParam("password") String password) {

        logger.info("username = {}, password = {]", username, password);

        boolean checkpwd = identityService.checkPassword(username, password);

        if (checkpwd) {
            // 读取用户信息.
            User user = identityService.createUserQuery().userId(username).singleResult();
//            UserUtil.saveUserToSession(session, user);

            // 读取用户角色.
            List<Group> groupList = identityService.createGroupQuery().groupMember(user.getId()).list();

            String[] groupNames = new String[groupList.size()];

            for (int i = 0; i < groupNames.length; i++) {
                groupNames[i] = groupList.get(i).getName();
            }

//            session.setAttribute("groupNames", ArrayUtils.toString(groupNames));

            return ResponseEntity.ok().body(ArrayUtils.toString(groupNames));
        } else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }

}

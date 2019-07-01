# 定义角色.
INSERT INTO `ACT_ID_GROUP` VALUES ('admin', null, '管理员', null);
INSERT INTO `ACT_ID_GROUP` VALUES ('generalManager', null, '总经理', null);
INSERT INTO `ACT_ID_GROUP` VALUES ('deptLeader', null, '部门经理', null);
INSERT INTO `ACT_ID_GROUP` VALUES ('hr', null, '人事经理', null);

# 管理员.
INSERT INTO `ACT_ID_USER` VALUES ('henry', null, 'Henry', 'Yan', 'admin@localhost', '000000', null);
INSERT INTO `ACT_ID_MEMBERSHIP` VALUES ('henry', 'admin');

# 总经理.
INSERT INTO `ACT_ID_USER` VALUES ('bill', null, 'Bill', 'Zheng', 'bill@localhost', '000000', null);
INSERT INTO `ACT_ID_MEMBERSHIP` VALUES ('bill', 'generalManager');

# 人事经理.
INSERT INTO `ACT_ID_USER` VALUES ('jenny', null, 'Jenny', 'Luo', 'hr@localhost', '000000', null);
INSERT INTO `ACT_ID_MEMBERSHIP` VALUES ('jenny', 'hr');

# 市场部.
INSERT INTO `ACT_ID_USER` VALUES ('eric', null, 'Eric', 'Li', 'eric@localhost', '000000', null);
INSERT INTO `ACT_ID_USER` VALUES ('tom', null, 'Tom', 'Wang', 'tom@localhost', '000000', null);
INSERT INTO `ACT_ID_USER` VALUES ('kermit', null, 'Kermit', 'Miao', 'kermit@localhost', '000000', null);
INSERT INTO `ACT_ID_MEMBERSHIP` VALUES ('kermit', 'deptLeader');

# 业务部.
INSERT INTO `ACT_ID_USER` VALUES ('amy', null, 'Amy', 'Zhang', 'amy@localhost', '000000', null);
INSERT INTO `ACT_ID_USER` VALUES ('andy', null, 'Andy', 'Zhao', 'andy@localhost', '000000', null);
INSERT INTO `ACT_ID_MEMBERSHIP` VALUES ('andy', 'deptLeader');
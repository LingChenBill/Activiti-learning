-- ----------------------------
-- Table structure for process_template
-- ----------------------------
DROP TABLE IF EXISTS `process_template`;
CREATE TABLE `process_template` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '流程模板ID',
  `deptName` varchar(255) DEFAULT NULL COMMENT '部门名称',
  `processName` varchar(255) DEFAULT NULL COMMENT '流程名称',
  `modelId` varchar(255) DEFAULT NULL COMMENT '流程BPMN文件id',
  `style` text COMMENT '表单样式',
  `createTime` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `flowChartId` varchar(20) DEFAULT NULL COMMENT '流程图Id',
  `isHaveNew` int(11) DEFAULT NULL COMMENT '是否修改过模板',
  `isTemplate` int(11) DEFAULT NULL COMMENT '是否为中债规定模板',
  `alternateField1` varchar(255) DEFAULT NULL COMMENT '备用字段1',
  `alternateField2` varchar(255) DEFAULT NULL COMMENT '备用字段2',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2687 DEFAULT CHARSET=utf8;

INSERT INTO `process_template` VALUES ('1', '部门1', '流程1', '1', null, '2019-05-20 15:23:38', null, null, '1', null, null, null);
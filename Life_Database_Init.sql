-- 浮生卷数据库初始化脚本
-- 创建时间: 2025-09-27
-- 版本: 1.0.0

-- 1. 创建玩家角色表
CREATE TABLE IF NOT EXISTS `life_player` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(50) NOT NULL COMMENT '用户ID',
  `nickname` varchar(21) NOT NULL COMMENT '角色昵称(中文,不超过7个字)',
  `attribute` tinyint NOT NULL COMMENT '角色属性：1金2木3水4火5土',
  `level` int DEFAULT 1 COMMENT '等级',
  `experience` bigint DEFAULT 0 COMMENT '经验值',
  `cultivation` bigint DEFAULT 0 COMMENT '修为',
  `cultivation_speed` int DEFAULT 10 COMMENT '修炼速度(修为/分钟)',
  `last_cultivation_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最后修炼时间',
  
  -- 基础属性
  `speed` int DEFAULT 1 COMMENT '速度',
  `constitution` int DEFAULT 1 COMMENT '体质',
  `spirit_power` int DEFAULT 1 COMMENT '灵力',
  `strength` int DEFAULT 1 COMMENT '力量',
  
  -- 拓展属性(战斗属性)
  `health` int DEFAULT 10 COMMENT '血量',
  `max_health` int DEFAULT 10 COMMENT '最大血量',
  `defense` int DEFAULT 1 COMMENT '防御',
  `critical_rate` decimal(5,3) DEFAULT 0.000 COMMENT '会心率(%)',
  `critical_damage` decimal(6,3) DEFAULT 110.000 COMMENT '会心效果(%)',
  `armor_break` decimal(5,3) DEFAULT 0.000 COMMENT '破防(%)',
  `attack_power` int DEFAULT 6 COMMENT '攻击力',
  
  `stamina` int DEFAULT 100 COMMENT '体力值',
  `max_stamina` int DEFAULT 100 COMMENT '最大体力',
  `last_stamina_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最后体力恢复时间',
  
  `spirit` bigint DEFAULT 1000 COMMENT '灵粹（游戏货币）',
  `last_battle_time` datetime NULL COMMENT '最后战斗时间',
  `last_hp_recovery_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最后血量恢复时间',
  
  `current_map_id` bigint DEFAULT 1 COMMENT '当前所在地图',
  `game_status` tinyint DEFAULT 0 COMMENT '游戏状态：0正常1战斗中2组队中',
  
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  UNIQUE KEY `uk_nickname` (`nickname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='玩家角色表';

-- 2. 创建装备基础表
CREATE TABLE IF NOT EXISTS `life_equipment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '装备名称',
  `type` tinyint NOT NULL COMMENT '装备类型：1功法2心法3神通4法宝',
  `attribute` tinyint DEFAULT 0 COMMENT '装备属性：0无属性1金2木3水4火5土',
  `rarity` tinyint DEFAULT 1 COMMENT '稀有度：1普通2精良3稀有4史诗5传说',
  `description` text COMMENT '装备描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='装备基础表';

-- 3. 创建玩家装备表
CREATE TABLE IF NOT EXISTS `life_player_equipment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `player_id` bigint NOT NULL COMMENT '玩家ID',
  `equipment_id` bigint NOT NULL COMMENT '装备ID',
  `is_equipped` tinyint DEFAULT 0 COMMENT '是否装备：0未装备1已装备',
  `proficiency` int DEFAULT 0 COMMENT '熟练度(仅法宝使用)',
  `level` int DEFAULT 1 COMMENT '等级(仅法宝使用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_player_id` (`player_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='玩家装备表';

-- 4. 创建装备效果表
CREATE TABLE IF NOT EXISTS `life_equipment_effect` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `equipment_id` bigint NOT NULL COMMENT '装备ID',
  `level` int DEFAULT 1 COMMENT '装备等级(法宝分级)',
  `effect_type` tinyint NOT NULL COMMENT '效果类型：1基础属性2拓展属性3修炼速度',
  `effect_target` varchar(20) NOT NULL COMMENT '效果目标字段',
  `effect_value` decimal(10,3) NOT NULL COMMENT '效果数值',
  `is_percentage` tinyint DEFAULT 0 COMMENT '是否百分比：0固定值1百分比',
  PRIMARY KEY (`id`),
  KEY `idx_equipment_level` (`equipment_id`, `level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='装备效果表';

-- 5. 创建技能基础表
CREATE TABLE IF NOT EXISTS `life_skill` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '技能名称',
  `type` tinyint NOT NULL COMMENT '技能类型：1直接伤害2增益3减益',
  `attribute` tinyint DEFAULT 0 COMMENT '技能属性：0无属性1金2木3水4火5土',
  `power` int DEFAULT 0 COMMENT '技能威力',
  `cooldown` int DEFAULT 0 COMMENT '冷却回合数',
  `required_level` int DEFAULT 1 COMMENT '需要等级',
  `required_cultivation` int DEFAULT 0 COMMENT '需要修为',
  `max_level` int DEFAULT 10 COMMENT '最大等级',
  `description` text COMMENT '技能描述',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技能基础表';

-- 6. 创建技能效果表
CREATE TABLE IF NOT EXISTS `life_skill_effect` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `skill_id` bigint NOT NULL COMMENT '技能ID',
  `effect_type` tinyint NOT NULL COMMENT '效果类型：1伤害倍率2属性变化3持续效果',
  `effect_value` decimal(10,3) NOT NULL COMMENT '效果数值',
  `duration` int DEFAULT 0 COMMENT '持续回合数',
  `timing` tinyint DEFAULT 1 COMMENT '生效时机：1回合开始前2回合开始后3回合结束',
  `can_critical` tinyint DEFAULT 1 COMMENT '是否可会心：0不可1可以',
  PRIMARY KEY (`id`),
  KEY `idx_skill_id` (`skill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技能效果表';

-- 7. 创建地图表
CREATE TABLE IF NOT EXISTS `life_map` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '地图名称',
  `type` tinyint NOT NULL COMMENT '地图类型：1可传送2内置地图',
  `min_level` int DEFAULT 1 COMMENT '最低境界要求',
  `description` text COMMENT '地图描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地图表';

-- 8. 创建NPC表
CREATE TABLE IF NOT EXISTS `life_npc` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT 'NPC名称',
  `map_id` bigint COMMENT '所在地图ID(null表示随机地图)',
  `npc_type` tinyint NOT NULL COMMENT 'NPC类型：1对话领取道具2传送3触发奇遇',
  `dialog` text COMMENT '对话内容',
  `target_map_id` bigint COMMENT '传送目标地图ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='NPC表';

-- 9. 创建怪物表
CREATE TABLE IF NOT EXISTS `life_monster` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '怪物名称',
  `map_id` bigint NOT NULL COMMENT '所在地图ID',
  `monster_type` tinyint NOT NULL COMMENT '怪物类型：1普通怪物2副本BOSS3世界BOSS',
  `attribute` tinyint DEFAULT 0 COMMENT '怪物属性：0无属性1金2木3水4火5土',
  
  -- 战斗属性
  `level` int DEFAULT 1 COMMENT '等级',
  `health` int NOT NULL COMMENT '血量',
  `attack_power` int NOT NULL COMMENT '攻击力',
  `defense` int NOT NULL COMMENT '防御',
  `speed` int NOT NULL COMMENT '速度',
  `critical_rate` decimal(5,3) DEFAULT 0.000 COMMENT '会心率',
  `critical_damage` decimal(6,3) DEFAULT 110.000 COMMENT '会心效果',
  `armor_break` decimal(5,3) DEFAULT 0.000 COMMENT '破防',
  
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_map_id` (`map_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='怪物表';

-- 10. 创建怪物掉落配置表
CREATE TABLE IF NOT EXISTS `life_monster_drop` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `monster_id` bigint NOT NULL COMMENT '怪物ID',
  `drop_type` tinyint NOT NULL COMMENT '掉落类型：1道具2灵粹',
  `item_id` bigint DEFAULT NULL COMMENT '道具ID（掉落类型为道具时）',
  `spirit_amount` int DEFAULT NULL COMMENT '灵粹数量（掉落类型为灵粹时）',
  `drop_rate` decimal(5,3) NOT NULL COMMENT '掉落概率(0-1)',
  `min_quantity` int DEFAULT 1 COMMENT '最小掉落数量',
  `max_quantity` int DEFAULT 1 COMMENT '最大掉落数量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_monster_id` (`monster_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='怪物掉落配置表';

-- 11. 创建怪物技能表
CREATE TABLE IF NOT EXISTS `life_monster_skill` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `monster_id` bigint NOT NULL COMMENT '怪物ID',
  `skill_id` bigint NOT NULL COMMENT '技能ID',
  PRIMARY KEY (`id`),
  KEY `idx_monster_id` (`monster_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='怪物技能表';

-- 11. 创建道具表
CREATE TABLE IF NOT EXISTS `life_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '道具名称',
  `type` tinyint NOT NULL COMMENT '道具类型：1修为类2属性类3体力类4升级法宝类5恢复类6技能书',
  `effect_value` int NOT NULL COMMENT '效果数值',
  `effect_attribute` varchar(50) DEFAULT NULL COMMENT '影响的属性（属性类道具使用：speed/constitution/spirit_power/strength）',
  `skill_id` bigint DEFAULT NULL COMMENT '技能书对应的技能ID',
  `max_use_count` int DEFAULT -1 COMMENT '最大使用次数(-1表示无限制)',
  `can_use_in_battle` tinyint DEFAULT 0 COMMENT '是否可战斗中使用：0否1是',
  `description` text COMMENT '道具描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_skill_id` (`skill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='道具表';

-- 12. 创建玩家道具表
CREATE TABLE IF NOT EXISTS `life_player_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `player_id` bigint NOT NULL COMMENT '玩家ID',
  `item_id` bigint NOT NULL COMMENT '道具ID',
  `quantity` int DEFAULT 1 COMMENT '数量',
  `used_count` int DEFAULT 0 COMMENT '已使用次数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_player_id` (`player_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='玩家道具表';

-- 13. 创建成就表
CREATE TABLE IF NOT EXISTS `life_achievement` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '成就名称',
  `description` text COMMENT '成就描述',
  `condition_type` tinyint NOT NULL COMMENT '条件类型：1属性达到2等级达到',
  `condition_target` varchar(20) NOT NULL COMMENT '条件目标字段',
  `condition_value` bigint NOT NULL COMMENT '条件数值',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成就表';

-- 14. 创建奇遇表
CREATE TABLE IF NOT EXISTS `life_adventure` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '奇遇名称',
  `description` text COMMENT '奇遇描述',
  `trigger_type` tinyint NOT NULL COMMENT '触发类型：1通关副本2击杀世界BOSS3添加好友4与NPC对话',
  `trigger_target` varchar(50) COMMENT '触发目标',
  `trigger_rate` decimal(5,3) DEFAULT 10.000 COMMENT '触发概率(%)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='奇遇表';

-- 15. 创建玩家成就表
CREATE TABLE IF NOT EXISTS `life_player_achievement` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `player_id` bigint NOT NULL COMMENT '玩家ID',
  `achievement_id` bigint NOT NULL COMMENT '成就ID',
  `completed_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '完成时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_achievement` (`player_id`, `achievement_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='玩家成就表';

-- 16. 创建好友表
CREATE TABLE IF NOT EXISTS `life_friend` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `player_id` bigint NOT NULL COMMENT '玩家ID',
  `friend_id` bigint NOT NULL COMMENT '好友ID',
  `status` tinyint DEFAULT 0 COMMENT '状态：0待确认1已同意',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_friend` (`player_id`, `friend_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友表';

-- 17. 创建邮件表
CREATE TABLE IF NOT EXISTS `life_mail` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `from_player_id` bigint NOT NULL COMMENT '发送者ID',
  `to_player_id` bigint NOT NULL COMMENT '接收者ID',
  `subject` varchar(100) NOT NULL COMMENT '邮件主题',
  `content` text COMMENT '邮件内容',
  `attachment_type` tinyint COMMENT '附件类型：1道具2装备',
  `attachment_id` bigint COMMENT '附件ID',
  `attachment_quantity` int DEFAULT 1 COMMENT '附件数量',
  `is_read` tinyint DEFAULT 0 COMMENT '是否已读：0未读1已读',
  `is_received` tinyint DEFAULT 0 COMMENT '是否已领取附件：0未领取1已领取',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_to_player` (`to_player_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邮件表';

-- 18. 创建世界BOSS表
CREATE TABLE IF NOT EXISTS `life_world_boss` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `monster_id` bigint NOT NULL COMMENT '怪物ID',
  `map_id` bigint NOT NULL COMMENT '出现地图ID',
  `start_time` time NOT NULL COMMENT '开始时间',
  `end_time` time NOT NULL COMMENT '结束时间',
  `max_challenge_count` int DEFAULT 1 COMMENT '每人最大挑战次数',
  `is_active` tinyint DEFAULT 1 COMMENT '是否激活：0否1是',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='世界BOSS表';

-- 19. 创建世界BOSS奖励表
CREATE TABLE IF NOT EXISTS `life_world_boss_reward` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `world_boss_id` bigint NOT NULL COMMENT '世界BOSS ID',
  `min_damage` bigint NOT NULL COMMENT '最小伤害',
  `max_damage` bigint NOT NULL COMMENT '最大伤害',
  `spirit_reward` int DEFAULT 0 COMMENT '灵粹奖励',
  `item_rewards` text COMMENT 'JSON格式的道具奖励',
  PRIMARY KEY (`id`),
  KEY `idx_world_boss_id` (`world_boss_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='世界BOSS奖励表';

-- 20. 创建队伍表
CREATE TABLE IF NOT EXISTS `life_team` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `leader_id` bigint NOT NULL COMMENT '队长ID',
  `map_id` bigint NOT NULL COMMENT '所在地图ID',
  `status` tinyint DEFAULT 0 COMMENT '状态：0招募中1战斗中2已解散',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='队伍表';

-- 21. 创建队伍成员表
CREATE TABLE IF NOT EXISTS `life_team_member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `team_id` bigint NOT NULL COMMENT '队伍ID',
  `player_id` bigint NOT NULL COMMENT '玩家ID',
  `member_status` tinyint DEFAULT 1 COMMENT '成员状态：0离队1在队',
  `join_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_team_id` (`team_id`),
  KEY `idx_player_id` (`player_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='队伍成员表';

-- 22. 创建副本表
CREATE TABLE IF NOT EXISTS `life_dungeon` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '副本名称',
  `map_id` bigint NOT NULL COMMENT '副本地图ID',
  `boss_id` bigint NOT NULL COMMENT 'BOSS怪物ID',
  `daily_limit` int DEFAULT 1 COMMENT '每日挑战次数限制',
  `min_level` int DEFAULT 1 COMMENT '最低等级要求',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='副本表';

-- 23. 创建商店表
CREATE TABLE IF NOT EXISTS `life_shop` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `item_type` tinyint NOT NULL COMMENT '商品类型：1道具2装备',
  `item_id` bigint NOT NULL COMMENT '商品ID',
  `base_price` int NOT NULL COMMENT '基础价格',
  `current_price` int NOT NULL COMMENT '当前价格',
  `discount` decimal(3,2) DEFAULT 1.00 COMMENT '折扣',
  `in_stock` tinyint NOT NULL DEFAULT 1 COMMENT '是否有库存(1:有库存, 0:无库存)',
  `last_refresh_date` date DEFAULT (CURDATE()) COMMENT '最后刷新日期',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商店表';

-- 24. 创建玩家摊位表
CREATE TABLE IF NOT EXISTS `life_player_stall` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `player_id` bigint NOT NULL COMMENT '摊主ID',
  `stall_name` varchar(50) NOT NULL COMMENT '摊位名称',
  `item_type` tinyint NOT NULL COMMENT '商品类型：1道具2装备',
  `item_id` bigint NOT NULL COMMENT '商品ID',
  `quantity` int NOT NULL COMMENT '数量',
  `unit_price` int NOT NULL COMMENT '单价',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_player_id` (`player_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='玩家摊位表';

-- 25. 创建系统配置表
CREATE TABLE IF NOT EXISTS `life_system_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_key` varchar(50) NOT NULL COMMENT '配置键',
  `config_value` text NOT NULL COMMENT '配置值',
  `description` text COMMENT '配置描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 26. 创建游戏状态表
CREATE TABLE IF NOT EXISTS `life_game_status` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(50) NOT NULL COMMENT '用户ID',
  `game_mode` tinyint DEFAULT 0 COMMENT '游戏模式：0未进入1预备状态2正式游戏',
  `current_menu` varchar(50) COMMENT '当前菜单状态',
  `context_data` text COMMENT 'JSON格式的上下文数据',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏状态表';

-- 插入初始数据

-- 1. 插入默认地图数据
INSERT INTO `life_map` (`id`, `name`, `type`, `min_level`, `description`) VALUES
(1, '新手村', 1, 1, '修仙者的起始之地，这里有最基础的怪物和NPC'),
(2, '青云山', 1, 10, '灵气充沛的修炼圣地，适合筑基期修士'),
(3, '幽冥谷', 1, 20, '阴气森森的危险之地，金丹期才能安全探索'),
(4, '天机阁', 2, 1, '神秘的藏宝阁，需要特殊方式进入'),
(5, '鬼市', 1, 1, '修士们交易的神秘市场');

-- 2. 插入基础怪物数据
INSERT INTO `life_monster` (`id`, `name`, `map_id`, `monster_type`, `attribute`, `level`, `health`, `attack_power`, `defense`, `speed`) VALUES
(1, '野狼', 1, 1, 0, 1, 50, 15, 5, 8),
(2, '山贼', 1, 1, 0, 2, 80, 20, 8, 10),
(3, '青风狼', 2, 1, 2, 10, 300, 60, 25, 35),
(4, '石甲熊', 2, 1, 5, 12, 500, 80, 50, 20),
(5, '幽冥鬼', 3, 1, 0, 20, 800, 150, 60, 50);

-- 3. 插入怪物掉落配置
INSERT INTO `life_monster_drop` (`monster_id`, `drop_type`, `item_id`, `spirit_amount`, `drop_rate`, `min_quantity`, `max_quantity`) VALUES
-- 野狼掉落
(1, 1, 10, NULL, 0.15, 1, 1),    -- 15%概率掉落回春丹
(1, 1, 8, NULL, 0.20, 1, 2),     -- 20%概率掉落1-2个大还丹
(1, 2, NULL, 10, 0.50, 1, 1),    -- 50%概率掉落10灵粹
-- 山贼掉落
(2, 1, 10, NULL, 0.25, 1, 2),    -- 25%概率掉落1-2个回春丹
(2, 1, 1, NULL, 0.10, 1, 1),     -- 10%概率掉落小修为丹
(2, 1, 12, NULL, 0.05, 1, 1),    -- 5%概率掉落金元斩秘籍
(2, 2, NULL, 30, 0.60, 1, 1),    -- 60%概率掉落30灵粹
-- 青风狼掉落
(3, 1, 11, NULL, 0.20, 1, 2),    -- 20%概率掉落1-2个大回春丹
(3, 1, 2, NULL, 0.15, 1, 1),     -- 15%概率掉落中修为丹
(3, 1, 13, NULL, 0.08, 1, 1),    -- 8%概率掉落木灵术秘籍
(3, 2, NULL, 100, 0.70, 1, 1),   -- 70%概率掉落100灵粹
-- 石甲熊掉落
(4, 1, 11, NULL, 0.30, 2, 3),    -- 30%概率掉落2-3个大回春丹
(4, 1, 2, NULL, 0.20, 1, 1),     -- 20%概率掉落中修为丹
(4, 1, 6, NULL, 0.25, 1, 2),     -- 25%概率掉落1-2个固本培元丹
(4, 2, NULL, 150, 0.80, 1, 1),   -- 80%概率掉落150灵粹
-- 幽冥鬼掉落
(5, 1, 3, NULL, 0.10, 1, 1),     -- 10%概率掉落大修为丹
(5, 1, 11, NULL, 0.40, 2, 4),    -- 40%概率掉落2-4个大回春丹
(5, 1, 5, NULL, 0.20, 1, 1),     -- 20%概率掉落迅捷散
(5, 2, NULL, 300, 0.90, 1, 1);   -- 90%概率掉落300灵粹

-- 4. 插入怪物技能配置
INSERT INTO `life_monster_skill` (`monster_id`, `skill_id`) VALUES
(1, 1),  -- 新手村野狼使用金元斩
(2, 2),  -- 青云山竹妖使用木灵术
(3, 3),  -- 幽谷水蛇使用水波功
(4, 4),  -- 烈火峡谷炎魔使用烈焰掌
(5, 5);  -- 鬼市亡灵使用厚土盾

INSERT INTO `life_item` (`id`, `name`, `type`, `effect_value`, `effect_attribute`, `skill_id`, `max_use_count`, `can_use_in_battle`, `description`) VALUES
(1, '小修为丹', 1, 50000, NULL, NULL, -1, 0, '服用立即获得50000修为'),
(2, '中修为丹', 1, 200000, NULL, NULL, -1, 0, '服用立即获得200000修为'),
(3, '大修为丹', 1, 1000000, NULL, NULL, -1, 0, '服用立即获得1000000修为'),
(4, '大力丸', 2, 1, 'strength', NULL, 10, 0, '服用永久增加1点力量，每人限用10颗'),
(5, '迅捷散', 2, 1, 'speed', NULL, 10, 0, '服用永久增加1点速度，每人限用10颗'),
(6, '固本培元丹', 2, 1, 'constitution', NULL, 10, 0, '服用永久增加1点体质，每人限用10颗'),
(7, '聚灵丹', 2, 1, 'spirit_power', NULL, 10, 0, '服用永久增加1点灵力，每人限用10颗'),
(8, '大还丹', 3, 10, NULL, NULL, -1, 0, '服用立即恢复10点体力'),
(9, '灵器合意散', 4, 20, NULL, NULL, -1, 0, '使用后增加当前装备法宝20点熟练度'),
(10, '回春丹', 5, 100, NULL, NULL, -1, 1, '服用恢复100血量，支持在战斗中使用'),
(11, '大回春丹', 5, 300, NULL, NULL, -1, 1, '服用恢复300血量，支持在战斗中使用'),
-- 技能书道具
(12, '金元斩秘籍', 6, 0, NULL, 1, -1, 0, '学习金属性技能『金元斩』'),
(13, '木灵术秘籍', 6, 0, NULL, 2, -1, 0, '学习木属性技能『木灵术』'),
(14, '水波功秘籍', 6, 0, NULL, 3, -1, 0, '学习水属性技能『水波功』'),
(15, '烈焰掌秘籍', 6, 0, NULL, 4, -1, 0, '学习火属性技能『烈焰掌』'),
(16, '厚土盾秘籍', 6, 0, NULL, 5, -1, 0, '学习土属性技能『厚土盾』'),
(17, '金刚护体诀', 6, 0, NULL, 6, -1, 0, '学习金属性增益技能『金刚护体』'),
(18, '生命回春诀', 6, 0, NULL, 7, -1, 0, '学习木属性恢复技能『生命回春』'),
(19, '寒冰护盾诀', 6, 0, NULL, 8, -1, 0, '学习水属性防御技能『寒冰护盾』'),
(20, '炎之狂怒诀', 6, 0, NULL, 9, -1, 0, '学习火属性增益技能『炎之狂怒』'),
(21, '大地之力诀', 6, 0, NULL, 10, -1, 0, '学习土属性增益技能『大地之力』');

-- 4. 插入基础装备数据
INSERT INTO `life_equipment` (`id`, `name`, `type`, `attribute`, `rarity`, `description`) VALUES
(1, '基础吐纳术', 1, 0, 1, '最基础的修炼功法，修炼速度+5'),
(2, '金刚不坏心法', 2, 0, 2, '增加体质的心法，体质+2'),
(3, '烈火掌', 3, 4, 2, '火系攻击神通，造成1.5倍火系伤害'),
(4, '护身玉佩', 4, 0, 1, '基础法宝，防御+10%');

-- 5. 插入装备效果数据
INSERT INTO `life_equipment_effect` (`equipment_id`, `level`, `effect_type`, `effect_target`, `effect_value`, `is_percentage`) VALUES
(1, 1, 3, 'cultivation_speed', 5, 0),
(2, 1, 1, 'constitution', 2, 0),
(4, 1, 2, 'defense', 10, 1);

-- 6. 插入系统配置数据
INSERT INTO `life_system_config` (`config_key`, `config_value`, `description`) VALUES
('speed_armor_break_rate', '0.005', '每点速度增加的破防率'),
('constitution_health_rate', '10', '每点体质增加的血量'),
('constitution_defense_rate', '1', '每点体质增加的防御'),
('spirit_critical_rate', '0.01', '每点灵力增加的会心率'),
('spirit_critical_damage_rate', '0.005', '每点灵力增加的会心效果'),
('strength_attack_rate', '6', '每点力量增加的攻击力'),
('strength_armor_break_rate', '0.01', '每点力量增加的破防率'),
('attribute_restraint_damage_bonus', '20', '属性克制伤害加成百分比'),
('attribute_restrained_defense_penalty', '10', '被属性克制防御减少百分比'),
('max_armor_break_rate', '30', '最大破防率'),
('stamina_recover_interval', '5', '体力恢复间隔(分钟)'),
('stamina_recover_amount', '1', '每次体力恢复量'),
('normal_attack_multiplier', '100', '普通攻击倍率'),
('escape_base_success_rate', '50', '基础逃跑成功率'),
('escape_speed_penalty_rate', '2', '速度差距逃跑成功率惩罚'),
('game_announcement', '欢迎来到浮生卷！这是一个修仙文字游戏，在这里你将体验不同的修仙人生！', '游戏公告');

-- 7. 插入基础成就数据
INSERT INTO `life_achievement` (`id`, `name`, `description`, `condition_type`, `condition_target`, `condition_value`) VALUES
(1, '初入修仙', '成功创建角色', 1, 'level', 1),
(2, '筑基成功', '达到筑基期', 1, 'level', 10),
(3, '金丹上人', '达到金丹期', 1, 'level', 20),
(4, '力拔山兮', '力量达到100点', 1, 'strength', 100),
(5, '身轻如燕', '速度达到100点', 1, 'speed', 100);

-- 8. 插入世界BOSS数据
INSERT INTO `life_world_boss` (`id`, `monster_id`, `map_id`, `start_time`, `end_time`, `max_challenge_count`, `is_active`) VALUES
(1, 5, 3, '11:00:00', '12:00:00', 2, 1),
(2, 5, 3, '19:00:00', '20:00:00', 2, 1);

-- 9. 插入世界BOSS奖励数据
INSERT INTO `life_world_boss_reward` (`id`, `world_boss_id`, `min_damage`, `max_damage`, `spirit_reward`, `item_rewards`) VALUES
(1, 1, 0, 1000, 50, '小修为丹x1'),
(2, 1, 1001, 3000, 100, '小修为丹x2,回春丹x1'),
(3, 1, 3001, 999999, 200, '小修为丹x3,回春丹x2,大力丸x1'),
(4, 2, 0, 1000, 50, '小修为丹x1'),
(5, 2, 1001, 3000, 100, '小修为丹x2,回春丹x1'),
(6, 2, 3001, 999999, 200, '小修为丹x3,回春丹x2,大力丸x1');

-- 10. 插入神秘商人商店数据
INSERT INTO `life_shop` (`id`, `item_type`, `item_id`, `base_price`, `current_price`, `discount`, `last_refresh_date`) VALUES
(1, 1, 1, 100, 80, 0.80, CURDATE()),
(2, 1, 2, 500, 400, 0.80, CURDATE()),
(3, 1, 3, 50, 40, 0.80, CURDATE()),
(4, 1, 4, 200, 160, 0.80, CURDATE()),
(5, 1, 5, 30, 24, 0.80, CURDATE());

-- 11. 插入基础技能数据
INSERT INTO `life_skill` (`id`, `name`, `type`, `attribute`, `power`, `cooldown`, `required_level`, `required_cultivation`, `max_level`, `description`) VALUES
(1, '金元斩', 1, 1, 120, 3, 1, 0, 10, '金属性基础攻击技能，挥洒金元之力对敌人造成伤害。'),
(2, '木灵术', 1, 2, 110, 3, 1, 0, 10, '木属性基础攻击技能，调动木灵之力攻击敌人。'),
(3, '水波功', 1, 3, 100, 3, 1, 0, 10, '水属性基础攻击技能，以水之柔韧对敌造成持续伤害。'),
(4, '烈焰掌', 1, 4, 130, 3, 1, 0, 10, '火属性基础攻击技能，释放炽热火焰灼烧敌人。'),
(5, '厚土盾', 1, 5, 90, 3, 1, 0, 10, '土属性基础攻击技能，凝聚大地之力进行攻击。'),
(6, '金刚护体', 2, 1, 0, 10, 5, 100, 5, '金属性防御技能，提升自身防御力和抗性。'),
(7, '生命回春', 2, 2, 0, 8, 5, 100, 5, '木属性恢复技能，持续恢复自身生命值。'),
(8, '寒冰护盾', 2, 3, 0, 12, 5, 100, 5, '水属性防御技能，形成冰盾减少受到的伤害。'),
(9, '炎之狂怒', 2, 4, 0, 15, 5, 100, 5, '火属性增益技能，大幅提升攻击力但降低防御。'),
(10, '大地之力', 2, 5, 0, 10, 5, 100, 5, '土属性增益技能，提升体质和生命上限。');

-- 12. 插入副本数据
INSERT INTO `life_dungeon` (`id`, `name`, `description`, `min_level`, `max_level`, `required_members`, `difficulty`, `rewards`, `is_active`) VALUES
(1, '幽暗洞穴', '一个充满低级魔兽的洞穴，适合新手探索。', 1, 10, 2, 1, '{"spirit": 200, "items": [{"id": 1, "quantity": 2}]}', 1),
(2, '迷雾森林', '被迷雾笼罩的神秘森林，隐藏着许多秘密。', 5, 15, 2, 2, '{"spirit": 500, "items": [{"id": 2, "quantity": 1}, {"id": 3, "quantity": 3}]}', 1),
(3, '烈焰山谷', '充满火焰元素的危险山谷，只有强者才能征服。', 10, 20, 2, 3, '{"spirit": 1000, "items": [{"id": 4, "quantity": 1}, {"id": 5, "quantity": 2}]}', 1);

-- 13. 插入境界配置数据
INSERT INTO `life_realm_config` (`id`, `realm_name`, `min_level`, `max_level`, `required_cultivation`, `max_cultivation`, `success_rate`, `attribute_bonus`, `special_abilities`) VALUES
(1, '练气期', 1, 9, 0, 50000, 1.000, '{}', '修仙入门境界，可以感知天地灵气'),
(2, '筑基期', 10, 19, 50000, 200000, 0.900, '{"speed": 5, "constitution": 5, "spirit_power": 5, "strength": 5}', '筑基成功，基础属性全面提升，修炼速度加快'),
(3, '金丹期', 20, 29, 200000, 800000, 0.800, '{"speed": 10, "constitution": 10, "spirit_power": 15, "strength": 10, "cultivation_speed": 20}', '结成金丹，灵力大增，修炼速度显著提升'),
(4, '元婴期', 30, 39, 800000, 3000000, 0.700, '{"speed": 20, "constitution": 20, "spirit_power": 30, "strength": 20, "cultivation_speed": 50}', '凝聚元婴，神识大幅增强，可以御空飞行'),
(5, '化神期', 40, 49, 3000000, 10000000, 0.600, '{"speed": 30, "constitution": 30, "spirit_power": 50, "strength": 30, "cultivation_speed": 100}', '神识化形，接近仙人境界，可以操控天地元素'),
(6, '大乘期', 50, 99, 10000000, 50000000, 0.500, '{"speed": 50, "constitution": 50, "spirit_power": 100, "strength": 50, "cultivation_speed": 200}', '最高境界，已臻化境，拥有移山填海之能');

-- 27. 创建世界BOSS挑战记录表
CREATE TABLE IF NOT EXISTS `life_world_boss_challenge` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `player_id` bigint NOT NULL COMMENT '玩家ID',
  `world_boss_id` bigint NOT NULL COMMENT '世界BOSS ID',
  `damage_dealt` bigint NOT NULL COMMENT '造成的伤害',
  `spirit_reward` int DEFAULT 0 COMMENT '获得的灵粹奖励',
  `item_rewards` text COMMENT '获得的道具奖励（JSON格式）',
  `challenge_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '挑战时间',
  PRIMARY KEY (`id`),
  KEY `idx_player_boss_time` (`player_id`, `world_boss_id`, `challenge_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='世界BOSS挑战记录表';

-- 28. 创建玩家技能表
CREATE TABLE IF NOT EXISTS `life_player_skill` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `player_id` bigint NOT NULL COMMENT '玩家ID',
  `skill_id` bigint NOT NULL COMMENT '技能ID',
  `skill_level` int DEFAULT 1 COMMENT '技能等级',
  `current_cooldown` int DEFAULT 0 COMMENT '当前冷却回合数',
  `last_used_time` datetime DEFAULT NULL COMMENT '最后使用时间',
  `learn_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '学习时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_skill` (`player_id`, `skill_id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_skill_id` (`skill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='玩家技能表';

-- 29. 创建签到记录表
CREATE TABLE IF NOT EXISTS `life_player_signin` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `player_id` bigint NOT NULL COMMENT '玩家ID',
  `signin_date` date NOT NULL COMMENT '签到日期',
  `spirit_reward` int NOT NULL COMMENT '获得的灵粹奖励',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '签到时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_date` (`player_id`, `signin_date`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_signin_date` (`signin_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='玩家签到记录表';

-- 31. 创建战斗状态表
CREATE TABLE IF NOT EXISTS `life_battle_state` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `player_id` bigint NOT NULL COMMENT '玩家ID',
  `monster_id` bigint NOT NULL COMMENT '怪物ID',
  `current_turn` int DEFAULT 1 COMMENT '当前回合数',
  `player_hp` int NOT NULL COMMENT '玩家当前血量',
  `monster_hp` int NOT NULL COMMENT '怪物当前血量',
  `monster_max_hp` int NOT NULL COMMENT '怪物最大血量',
  `monster_skill_cooldowns` json COMMENT '怪物技能冷却状态',
  `player_buffs` json COMMENT '玩家buff状态',
  `monster_buffs` json COMMENT '怪物buff状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_battle` (`player_id`),
  KEY `idx_player_id` (`player_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='战斗状态表';

-- 30. 创建境界配置表
CREATE TABLE IF NOT EXISTS `life_realm_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `realm_name` varchar(20) NOT NULL COMMENT '境界名称',
  `min_level` int NOT NULL COMMENT '最低等级',
  `max_level` int NOT NULL COMMENT '最高等级',
  `required_cultivation` bigint NOT NULL COMMENT '突破所需修为',
  `max_cultivation` bigint NOT NULL COMMENT '该境界修为上限',
  `success_rate` decimal(5,3) DEFAULT 1.000 COMMENT '突破成功率',
  `attribute_bonus` json COMMENT '突破奖励属性',
  `special_abilities` text COMMENT '境界特殊能力描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_realm_level` (`min_level`, `max_level`),
  KEY `idx_level_range` (`min_level`, `max_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='境界配置表';

-- 创建临时目录用于存储生成的图片
CREATE TABLE IF NOT EXISTS `life_temp_files` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `file_path` varchar(255) NOT NULL COMMENT '文件路径',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `expire_time` datetime DEFAULT (DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 DAY)) COMMENT '过期时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='临时文件表';

-- 数据库初始化完成
SELECT 'Life module database initialization completed!' as message;

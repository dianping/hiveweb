CREATE TABLE `HW_QueryHistory` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(30) COLLATE utf8_unicode_ci NOT NULL,
  `hql` TEXT COLLATE utf8_unicode_ci NOT NULL,
  `addtime` DATETIME NOT NULL,
  `filename` VARCHAR(500) COLLATE utf8_unicode_ci DEFAULT NULL,
  `mode` VARCHAR(10) COLLATE utf8_unicode_ci DEFAULT NULL,
  `exectime` BIGINT(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `Index_UserName` (`username`)
) ENGINE=MYISAM AUTO_INCREMENT=21812 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci

CREATE TABLE `HW_UserLogin` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(30) COLLATE utf8_unicode_ci NOT NULL,
  `logintime` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `Index_UserName` (`username`)
) ENGINE=MYISAM AUTO_INCREMENT=352 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci

CREATE TABLE `HW_QueryFavorite` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(30) COLLATE utf8_unicode_ci NOT NULL,
  `queryname` varchar(200) COLLATE utf8_unicode_ci NOT NULL,
  `hql` varchar(10000) COLLATE utf8_unicode_ci NOT NULL,
  `addtime` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `IDX_USERNAME_QUERYNAME` (`username`,`queryname`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci

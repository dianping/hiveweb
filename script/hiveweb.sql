CREATE TABLE `HW_QueryHistory` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(30) COLLATE utf8_unicode_ci NOT NULL,
  `hql` varchar(1000) COLLATE utf8_unicode_ci NOT NULL,
  `addtime` datetime NOT NULL,
  `filename` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `Index_UserName` (`username`)
) ENGINE=MyISAM AUTO_INCREMENT=352 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci

CREATE TABLE `HW_UserLogin` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(30) COLLATE utf8_unicode_ci NOT NULL,
  `logintime` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `Index_UserName` (`username`)
) ENGINE=MYISAM AUTO_INCREMENT=352 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci


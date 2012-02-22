-- 
-- These are the schema definitions for the DistrictDB Fabrik implementation.
-- Bruce Schurmann
-- See https://github.com/brucesch/districtdb/wiki
--

CREATE TABLE IF NOT EXISTS `districtdb_orgs` (
  OrgID int(10) unsigned NOT NULL auto_increment,
  OrgType enum('Pack','Troop','Crew','Ship','Post','Varsity','District','Council','CharterOrg','Company','Other') NOT NULL default 'Pack',
  CharterOrgType enum('None','School','Church','Other') NOT NULL default 'None',
  OrgNumber int(10) unsigned NOT NULL default 0,
  OrgName varchar(255) NOT NULL default '',
  CharterOrgID int(10) unsigned NOT NULL default 0,
  Website varchar(255) NOT NULL default '',
  MeetingTime varchar(255) NOT NULL default '',
  CommitteeMeetingTime varchar(255) NOT NULL default '',
  GetInfoFromCharterOrg enum('No','Yes') NOT NULL default 'Yes',
  Location varchar(255) NOT NULL default '',
  Phone varchar(100) NOT NULL default '',
  Address varchar(255) NOT NULL default '',
  City varchar(50) NOT NULL default '',
  State varchar(50) NOT NULL default '',
  Zip varchar(10) NOT NULL default '',
  Longitude decimal(16,14) NOT NULL default 0.0,
  Latitude decimal(16,14) NOT NULL default 0.0,
  OnlineAdvUnitID varchar(50) NOT NULL default '',
  OnlineAdvUnitNum varchar(50) NOT NULL default '',
  GoodTurnAmUnitID varchar(50) NOT NULL default '',
  GoodTurnAmUnitNum varchar(50) NOT NULL default '',
  active tinyint(4) NOT NULL default 1,
  inactivedate date NOT NULL default '0000-00-00',
  PRIMARY KEY  (OrgID),
  UNIQUE KEY id (OrgID)
 ) TYPE=MyISAM;

CREATE TABLE IF NOT EXISTS `districtdb_persons` (
  PersonID int(10) unsigned NOT NULL auto_increment,
  userid int(10) unsigned default 0,
  name varchar(255) NOT NULL default '',
  username varchar(150) NOT NULL default '',
  password varchar(100) NOT NULL default '',
  address1 varchar(255) NOT NULL default '',
  address2 varchar(255) NOT NULL default '',
  city varchar(50) NOT NULL default '',
  state varchar(50) NOT NULL default '',
  zip varchar(10) NOT NULL default '',
  emailtype enum('Home','Work','Unknown') NOT NULL default 'Unknown',
  email varchar(100) NOT NULL default '',
  emailtype_alt1 enum('Home','Work','Unknown') NOT NULL default 'Unknown',
  email_alt1 varchar(100) NOT NULL default '',
  phonetype enum('Home','Work','Mobile','Fax','Pager','Unknown') NOT NULL default 'Unknown',
  phone varchar(100) NOT NULL default '',
  phonetype_alt1 enum('Home','Work','Mobile','Fax','Pager','Unknown') NOT NULL default 'Unknown',
  phone_alt1 varchar(100) NOT NULL default '',
  phonetype_alt2 enum('Home','Work','Mobile','Fax','Pager','Unknown') NOT NULL default 'Unknown',
  phone_alt2 varchar(100) NOT NULL default '',
  bsaid varchar(50) NOT NULL default '',
  occupation varchar(100) NOT NULL default '',
  employer varchar(100) NOT NULL default '',
  eaglescout enum('Yes','No','Unknown') NOT NULL default 'Unknown',
  eagledate varchar(50) NOT NULL default '',
  passkey varchar(200) NOT NULL default '',
  confirmed tinyint(4) NOT NULL default 0,
  req_newemail tinyint(4) default 0,
  req_sendchange tinyint(4) default 0,
  req_date datetime NOT NULL default '0000-00-00 00:00:00',
  last_update timestamp(14),
  newsletter tinyint(4) NOT NULL default 1,
  nomail tinyint(4) NOT NULL default 1,
  active tinyint(4) NOT NULL default 1,
  PRIMARY KEY (PersonID),
  UNIQUE KEY id (PersonID)
 ) TYPE=MyISAM;

CREATE TABLE IF NOT EXISTS `districtdb_service` (
  ServiceID int(10) unsigned NOT NULL auto_increment,
  personid int(10) unsigned default 0,
  orgid int(10) unsigned default 0,
  jobid int(10) unsigned default 0,
  active tinyint(4) NOT NULL default 1,
  activedate date NOT NULL default '0000-00-00',
  inactivedate date NOT NULL default '0000-00-00',
  PRIMARY KEY (ServiceID),
  UNIQUE KEY id (ServiceID)
 ) TYPE=MyISAM;

CREATE TABLE IF NOT EXISTS `districtdb_jobs` (
  JobID int(10) unsigned NOT NULL auto_increment,
  Title varchar(255) NOT NULL default '',
  Description varchar(255) NOT NULL default '',
  jobtype enum('Pack','Troop','Crew','Ship','Post','Varsity','District','Council','CharterOrg','Company','Other') NOT NULL default 'Other',
  singlepositionrole tinyint(4) default 0,
  PRIMARY KEY  (JobID),
  UNIQUE KEY id (JobID)
 ) TYPE=MyISAM;

CREATE VIEW `districtdb_orgs_view` AS SELECT * FROM `districtdb_orgs`;

CREATE OR REPLACE VIEW `districtdb_org_jobs_view` AS SELECT 
  `districtdb_orgs`.`OrgID`*1000+`districtdb_jobs`.`JobID` AS `OJID`,
  `districtdb_orgs`.`OrgID` AS `OrgID`,
  `districtdb_orgs`.`OrgType` AS `OrgType`,
  `districtdb_jobs`.`JobID` AS `JobID`,
  `districtdb_jobs`.`Description` AS `Description`
  FROM `districtdb_orgs`
  LEFT JOIN `districtdb_jobs` ON `districtdb_orgs`.`OrgType` = `districtdb_jobs`.`jobtype`;
